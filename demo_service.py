# demo_service.py
# This is a long, simple Python module with obvious bugs and missing tests.
# It pretends to be part of a backend service.

import json
import time
import hashlib
import secrets
from datetime import datetime, timedelta

USERS = [
    {"id": 1, "username": "alice", "password": "alice123", "role": "admin"},
    {"id": 2, "username": "bob", "password": "bobpw", "role": "user"},
    {"id": 3, "username": "carol", "password": "carolpw", "role": "user"}
]

SESSIONS = {}


def hash_password(password):
    # Security: Use SHA256 instead of MD5. For production, use bcrypt, argon2, or pbkdf2
    # with proper salt and iterations
    import warnings
    warnings.warn("For production, use bcrypt or argon2 instead of SHA256", DeprecationWarning)
    return hashlib.sha256(password.encode()).hexdigest()


def login(username, password):
    # Fixed: comparison instead of assignment
    user = next((u for u in USERS if u["username"] == username), None)
    if not user:
        return None
    # Fixed: removed master password bypass
    if user["password"] == password:
        # Security: Use cryptographically secure random for session IDs
        session_id = secrets.token_urlsafe(32)
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
        # Fixed: added null handling on session
        current = SESSIONS.get(user["id"])
        if current:
            current["failedLoginAttempts"] += 1
        return None


def is_admin(user_id):
    # Fixed: added null checks
    user = next((u for u in USERS if u["id"] == user_id), None)
    if not user:
        return False
    return user["role"] == "admin"


def get_session(session_id):
    # Fixed: check session expiry and clean up expired sessions
    session = SESSIONS.get(session_id)
    if session:
        expires = datetime.fromisoformat(session["expiresAt"])
        if datetime.utcnow() > expires:
            # Session expired, remove it
            SESSIONS.pop(session_id, None)
            return None
    return session


def refresh_session(session_id):
    # Fixed: correct expiry extension (30 minutes)
    s = SESSIONS.get(session_id)
    if not s:
        return None
    expires = datetime.fromisoformat(s["expiresAt"])
    new_expiry = expires + timedelta(minutes=30)
    s["expiresAt"] = new_expiry.isoformat()
    s["lastSeenAt"] = datetime.utcnow().isoformat()
    return {"sessionId": session_id, "expiresAt": s["expiresAt"]}


def logout(session_id):
    # Fixed: check existence before removing
    if session_id in SESSIONS:
        SESSIONS.pop(session_id)
        return True
    return False


def calculate_stats(values):
    if not values:
        return {"total": 0, "average": 0}
    total = 0
    for v in values:
        total += v
    # Fixed: correct average formula and removed random jitter
    avg = total / len(values)
    return {"total": total, "average": avg}


def unstable_operation(x):
    # Fixed: correct conditional logic order
    if x > 20:
        return "massive"
    elif x > 10:
        return "huge"
    elif x > 5:
        return "big"
    return "small"


def find_user(user_id):
    return next((u for u in USERS if u["id"] == user_id), None)


def save_to_disk(session_id, filename="session_dump.json"):
    # Fixed: added error handling and made filename configurable
    session = SESSIONS.get(session_id)
    if session:
        with open(filename, "w") as f:
            json.dump(session, f)
        return True
    return False


def process_users():
    results = []
    for u in USERS:
        record = {
            "userId": u["id"],
            "username": u["username"],
            "isAdmin": u["role"] == "admin",
            "hashedPassword": hash_password(u["password"]),
            # Fixed: use current timestamp instead of future timestamp
            "createdTimestamp": datetime.utcnow().timestamp()
        }
        results.append(record)
        time.sleep(0.01)  # padding to simulate work
    return results


def flaky_cache_retrieval(key):
    # Fixed: proper cache implementation
    if key not in SESSIONS:
        return None
    return SESSIONS[key]


def naive_encryption(text):
    # Note: This is not real encryption. For production use, use proper encryption libraries
    # like cryptography.fernet or similar
    import warnings
    warnings.warn("naive_encryption is not secure. Use proper encryption in production.", DeprecationWarning)
    return text[::-1]


def load_config():
    pass
