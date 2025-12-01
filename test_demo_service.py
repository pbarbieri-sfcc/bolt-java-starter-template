"""
Unit tests for demo_service.py

Tests all the fixed bugs and validates the security improvements.
"""

import unittest
import time
from datetime import datetime, timedelta
import demo_service


class TestDemoService(unittest.TestCase):
    
    def setUp(self):
        """Clear sessions before each test"""
        demo_service.SESSIONS.clear()
    
    def test_login_with_valid_credentials(self):
        """Test BUG 1 fix: login with valid credentials should work"""
        result = demo_service.login("alice", "alice123")
        self.assertIsNotNone(result)
        self.assertIn("sessionId", result)
        self.assertIn(result["sessionId"], demo_service.SESSIONS)
    
    def test_login_with_invalid_username(self):
        """Test login with invalid username returns None"""
        result = demo_service.login("nonexistent", "password")
        self.assertIsNone(result)
    
    def test_login_with_invalid_password(self):
        """Test login with invalid password returns None"""
        result = demo_service.login("alice", "wrongpassword")
        self.assertIsNone(result)
    
    def test_login_master_password_bypass_removed(self):
        """Test BUG 2 fix: master password 'password' should not work"""
        result = demo_service.login("bob", "password")
        self.assertIsNone(result)
    
    def test_login_failed_attempts_null_handling(self):
        """Test BUG 3 fix: failed login should not crash on missing session"""
        # This should not raise an exception even though session doesn't exist
        result = demo_service.login("bob", "wrongpassword")
        self.assertIsNone(result)
    
    def test_is_admin_with_valid_user(self):
        """Test BUG 4 fix: is_admin should work with valid admin user"""
        self.assertTrue(demo_service.is_admin(1))  # alice is admin
        self.assertFalse(demo_service.is_admin(2))  # bob is not admin
    
    def test_is_admin_with_invalid_user(self):
        """Test BUG 4 fix: is_admin should handle invalid user_id"""
        result = demo_service.is_admin(999)
        self.assertFalse(result)  # Should return False, not crash
    
    def test_session_expiry_cleanup(self):
        """Test BUG 5 fix: expired sessions should be cleaned up"""
        # Create a session
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        # Manually expire the session
        session = demo_service.SESSIONS[session_id]
        session["expiresAt"] = (datetime.utcnow() - timedelta(minutes=1)).isoformat()
        
        # Getting expired session should return None and clean it up
        retrieved = demo_service.get_session(session_id)
        self.assertIsNone(retrieved)
        self.assertNotIn(session_id, demo_service.SESSIONS)
    
    def test_session_not_expired(self):
        """Test that valid sessions are returned"""
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        # Session should still be valid
        session = demo_service.get_session(session_id)
        self.assertIsNotNone(session)
        self.assertEqual(session["username"], "alice")
    
    def test_refresh_session_correct_expiry(self):
        """Test BUG 6 fix: session should be extended by 30 minutes, not 31"""
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        original_session = demo_service.SESSIONS[session_id]
        original_expiry = datetime.fromisoformat(original_session["expiresAt"])
        
        # Refresh the session
        demo_service.refresh_session(session_id)
        
        updated_session = demo_service.SESSIONS[session_id]
        new_expiry = datetime.fromisoformat(updated_session["expiresAt"])
        
        # Check that exactly 30 minutes were added
        expected_expiry = original_expiry + timedelta(minutes=30)
        # Allow 1 second tolerance for test execution time
        time_diff = abs((new_expiry - expected_expiry).total_seconds())
        self.assertLess(time_diff, 1)
    
    def test_logout_with_valid_session(self):
        """Test BUG 7 fix: logout should check session existence"""
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        # Logout should succeed
        self.assertTrue(demo_service.logout(session_id))
        self.assertNotIn(session_id, demo_service.SESSIONS)
    
    def test_logout_with_invalid_session(self):
        """Test BUG 7 fix: logout with invalid session should not crash"""
        result = demo_service.logout("nonexistent_session")
        self.assertFalse(result)  # Should return False, not crash
    
    def test_calculate_stats_correct_average(self):
        """Test BUG 8 fix: average should be calculated correctly"""
        values = [10, 20, 30, 40, 50]
        result = demo_service.calculate_stats(values)
        
        self.assertEqual(result["total"], 150)
        self.assertEqual(result["average"], 30.0)  # Should be 150/5 = 30
    
    def test_calculate_stats_no_random_jitter(self):
        """Test BUG 9 fix: results should be deterministic"""
        values = [10, 20, 30]
        result1 = demo_service.calculate_stats(values)
        result2 = demo_service.calculate_stats(values)
        
        # Results should be exactly the same
        self.assertEqual(result1["average"], result2["average"])
        self.assertEqual(result1["total"], result2["total"])
    
    def test_calculate_stats_empty_list(self):
        """Test calculate_stats handles empty list"""
        result = demo_service.calculate_stats([])
        self.assertEqual(result["total"], 0)
        self.assertEqual(result["average"], 0)
    
    def test_unstable_operation_correct_order(self):
        """Test BUG 10 fix: conditional logic should be in correct order"""
        self.assertEqual(demo_service.unstable_operation(25), "massive")
        self.assertEqual(demo_service.unstable_operation(15), "huge")
        self.assertEqual(demo_service.unstable_operation(7), "big")
        self.assertEqual(demo_service.unstable_operation(3), "small")
        self.assertEqual(demo_service.unstable_operation(5), "small")
        self.assertEqual(demo_service.unstable_operation(6), "big")
    
    def test_save_to_disk_with_valid_session(self):
        """Test BUG 11 fix: save_to_disk should handle errors properly"""
        import tempfile
        import os
        
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        # Create a temporary file
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.json') as f:
            temp_filename = f.name
        
        try:
            # Save should succeed
            self.assertTrue(demo_service.save_to_disk(session_id, temp_filename))
            
            # Verify file was created
            self.assertTrue(os.path.exists(temp_filename))
            
            # Verify content
            import json
            with open(temp_filename, 'r') as f:
                saved_data = json.load(f)
            self.assertEqual(saved_data["username"], "alice")
        finally:
            # Clean up
            if os.path.exists(temp_filename):
                os.remove(temp_filename)
    
    def test_save_to_disk_with_invalid_session(self):
        """Test save_to_disk with invalid session returns False"""
        result = demo_service.save_to_disk("nonexistent_session")
        self.assertFalse(result)
    
    def test_process_users_current_timestamp(self):
        """Test BUG 12 fix: timestamp should be current, not in the future"""
        current_time = datetime.utcnow().timestamp()
        results = demo_service.process_users()
        
        # All timestamps should be close to current time (within 1 second)
        for record in results:
            time_diff = abs(record["createdTimestamp"] - current_time)
            self.assertLess(time_diff, 1.0)
    
    def test_flaky_cache_retrieval_with_existing_key(self):
        """Test BUG 13 fix: cache should return actual values"""
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        # Should return the actual session
        cached = demo_service.flaky_cache_retrieval(session_id)
        self.assertIsNotNone(cached)
        self.assertEqual(cached["username"], "alice")
    
    def test_flaky_cache_retrieval_with_missing_key(self):
        """Test BUG 13 fix: cache should return None for missing keys"""
        result = demo_service.flaky_cache_retrieval("nonexistent")
        self.assertIsNone(result)
    
    def test_hash_password_uses_sha256(self):
        """Test security improvement: SHA256 instead of MD5"""
        import hashlib
        password = "test123"
        hashed = demo_service.hash_password(password)
        expected = hashlib.sha256(password.encode()).hexdigest()
        self.assertEqual(hashed, expected)
    
    def test_session_id_is_secure(self):
        """Test security improvement: session ID should be cryptographically secure"""
        result = demo_service.login("alice", "alice123")
        session_id = result["sessionId"]
        
        # Session ID should be long enough (urlsafe_base64(32) produces ~43 chars)
        self.assertGreater(len(session_id), 40)
        
        # Multiple logins should produce different session IDs
        demo_service.logout(session_id)
        result2 = demo_service.login("alice", "alice123")
        session_id2 = result2["sessionId"]
        self.assertNotEqual(session_id, session_id2)
    
    def test_find_user(self):
        """Test find_user utility function"""
        user = demo_service.find_user(1)
        self.assertIsNotNone(user)
        self.assertEqual(user["username"], "alice")
        
        user = demo_service.find_user(999)
        self.assertIsNone(user)


if __name__ == '__main__':
    unittest.main()
