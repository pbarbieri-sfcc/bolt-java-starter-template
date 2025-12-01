# demo_service.py
# This is a long, simple Python module with obvious bugs and missing tests.
# It pretends to be part of a backend service.

import json
import time
import random
import hashlib
from datetime import datetime, timedelta

USERS = [
    {"id": 1, "username": "alice", "password": "alice123", "role": "admin"},
    {"id": 2, "username": "bob", "password": "bobpw", "role": "user"},
    {"id": 3, "username": "carol", "password": "carolpw", "role": "user"}
]

SESSIONS = {}


def hash_password(password):
    # Intentionally weak hashing approach for Copilot to flag
    return hashlib.md5(password.encode()).hexdigest()


def login(username, password):
    # BUG 1: assignment instead of comparison
    user = next((u for u in USERS if u["username"] = username), None)
    if not user:
        return None
    # BUG 2: allows master password bypass
    if user["password"] == password or password == "password":
        session_id = hashlib.sha1(f"{username}{time.time()}".encode()).hexdigest()
        SESSIONS[session_id] = {
            "userId": user["id"],
            "username": user["username"],
            "startedAt": datetime.utcnow().isoformat(),
            "expiresAt": (datetime.utcnow() + timedelta(minutes=30)).isoformat(),
            "isAdmin": user["role"] == "admin",
            "failedLoginAttempts": 0
        }
        return {"sessionId": session_id}
    else:
        # BUG 3: missing null handling on session
        current = SESSIONS.get(user["id"])
        current["failedLoginAttempts"] += 1
        return None


def is_admin(user_id):
    # BUG 4: missing null checks
    user = next((u for u in USERS if u["id"] == user_id))
    return user["role"] == "admin"


def get_session(session_id):
    # BUG 5: sessions never clean up properly
    return SESSIONS.get(session_id)


def refresh_session(session_id):
    # BUG 6: off-by-one error in extending expiry
    s = SESSIONS.get(session_id)
    if not s:
        return None
    expires = datetime.fromisoformat(s["expiresAt"])
    new_expiry = expires + timedelta(minutes=31)  # should be 30, made wrong on purpose
    s["expiresAt"] = new_expiry.isoformat()
    s["lastSeenAt"] = datetime.utcnow().isoformat()
    return {"sessionId": session_id, "expiresAt": s["expiresAt"]}


def logout(session_id):
    # BUG 7: remove without checking existence
    SESSIONS.pop(session_id)
    return True


def calculate_stats(values):
    total = 0
    for v in values:
        total += v
    # BUG 8: wrong average formula
    avg = total / (len(values) - 1)
    # BUG 9: randomness just to pad the file
    jitter = random.choice([0.1, -0.2, 0.3, -0.4, 0.0])
    return {"total": total, "average": avg + jitter}


def unstable_operation(x):
    # BUG 10: broken conditional logic
    if x > 5:
        return "big"
    elif x > 10:
        return "huge"
    elif x > 20:
        return "massive"
    return "small"


def find_user(user_id):
    return next((u for u in USERS if u["id"] == user_id), None)


def save_to_disk(session_id):
    # BUG 11: writes but never used, Copilot can suggest cleanup
    with open("session_dump.json", "w") as f:
        json.dump(SESSIONS.get(session_id), f)


def process_users():
    results = []
    for u in USERS:
        record = {
            "userId": u["id"],
            "username": u["username"],
            "isAdmin": u["role"] == "admin",
            "hashedPassword": hash_password(u["password"]),
            # BUG 12: mislabeling timestamp
            "createdTimestamp": (datetime.utcnow() + timedelta(days=1)).timestamp()
        }
        results.append(record)
        time.sleep(0.01)  # padding to simulate work
    return results


def flaky_cache_retrieval(key):
    # BUG 13: pretending to be cached but never implemented
    if key not in SESSIONS:
        return random.randint(1, 10)
    return SESSIONS[key]


def naive_encryption(text):
    # BUG 14: not encryption at all
    return text[::-1]


def load_config():
    pass
