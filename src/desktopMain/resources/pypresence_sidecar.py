#!/usr/bin/env python3
import json
import sys

try:
    from pypresence import Presence, ActivityType
except Exception as exc:
    err = {"ok": False, "error": f"pypresence import failed: {exc}"}
    print(json.dumps(err), flush=True)
    sys.exit(1)


RPC = None


def ok(extra=None):
    payload = {"ok": True}
    if extra:
        payload.update(extra)
    print(json.dumps(payload), flush=True)


def fail(message):
    print(json.dumps({"ok": False, "error": str(message)}), flush=True)


def clean_dead_sockets():
    if sys.platform not in ("linux", "darwin"):
        return
    import os, socket, tempfile
    tempdir = os.environ.get("XDG_RUNTIME_DIR") or (
        f"/run/user/{os.getuid()}" if os.path.exists(f"/run/user/{os.getuid()}") else tempfile.gettempdir()
    )
    paths = [".", "..", "snap.discord", "app/com.discordapp.Discord", "app/com.discordapp.DiscordCanary"]
    for path in paths:
        full_path = os.path.abspath(os.path.join(tempdir, path))
        if os.path.isdir(full_path):
            for entry in os.scandir(full_path):
                if entry.name.startswith("discord-ipc-") and os.path.exists(entry):
                    try:
                        with socket.socket(socket.AF_UNIX, socket.SOCK_STREAM) as client:
                            client.connect(entry.path)
                    except ConnectionRefusedError:
                        try:
                            os.remove(entry.path)
                        except Exception:
                            pass

def cmd_connect(data):
    global RPC
    client_id = data.get("client_id")
    if not client_id:
        fail("client_id is required")
        return

    clean_dead_sockets()

    RPC = Presence(str(client_id))
    RPC.connect()
    ok()


def cmd_update(data):
    if RPC is None:
        fail("not connected")
        return

    raw_activity_type = data.get("activity_type", 0)
    try:
        mapped_type = ActivityType(raw_activity_type)
    except ValueError:
        mapped_type = ActivityType.PLAYING

    payload = {
        "name": data.get("name"),
        "activity_type": mapped_type,
        "details": data.get("details"),
        "state": data.get("state"),
        "start": data.get("start"),
        "end": data.get("end"),
        "large_image": data.get("large_image"),
        "large_text": data.get("large_text"),
        "small_image": data.get("small_image"),
        "small_text": data.get("small_text"),
    }

    buttons = data.get("buttons")
    if isinstance(buttons, list) and buttons:
        payload["buttons"] = buttons

    payload = {k: v for k, v in payload.items() if v not in (None, "")}
    RPC.update(**payload)
    ok()


def cmd_disconnect():
    global RPC
    if RPC is not None:
        try:
            RPC.close()
        except Exception:
            pass
    RPC = None
    ok()


def main():
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue

        try:
            data = json.loads(line)
            command = data.get("cmd")
            if command == "connect":
                cmd_connect(data)
            elif command == "update":
                cmd_update(data)
            elif command == "disconnect":
                cmd_disconnect()
                break
            else:
                fail(f"unknown command: {command}")
        except Exception as exc:
            fail(exc)


if __name__ == "__main__":
    main()
