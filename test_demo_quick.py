import unittest
import demo_quick

class TestDemoQuick(unittest.TestCase):
    
    def setUp(self):
        # Reset user attempts before each test
        for user in demo_quick.USERS:
            user["attempts"] = 0
    
    def test_get_user_found(self):
        """Test BUG 1 fix: get_user should use comparison (==) not assignment (=)"""
        user = demo_quick.get_user("alice")
        self.assertIsNotNone(user)
        self.assertEqual(user["name"], "alice")
        self.assertEqual(user["id"], 1)
    
    def test_get_user_not_found(self):
        """Test BUG 1 fix: get_user should return None for non-existent user"""
        user = demo_quick.get_user("charlie")
        self.assertIsNone(user)
    
    def test_login_with_valid_credentials(self):
        """Test successful login with correct credentials"""
        result = demo_quick.login("alice", "123")
        self.assertIsNotNone(result)
        self.assertEqual(result["user"], "alice")
        self.assertIn("token", result)
        self.assertFalse(result["admin"])  # alice is not admin
    
    def test_login_with_admin_user(self):
        """Test BUG 3 fix: role field should exist and admin check should work"""
        result = demo_quick.login("bob", "abc")
        self.assertIsNotNone(result)
        self.assertEqual(result["user"], "bob")
        self.assertTrue(result["admin"])  # bob is admin
    
    def test_login_with_invalid_password(self):
        """Test BUG 4 fix: attempts should be incremented without error"""
        result = demo_quick.login("alice", "wrong")
        self.assertIsNone(result)
        # Verify attempts was incremented
        user = demo_quick.get_user("alice")
        self.assertEqual(user["attempts"], 1)
    
    def test_login_with_nonexistent_user(self):
        """Test BUG 2 fix: should handle None user without crashing"""
        result = demo_quick.login("charlie", "anypassword")
        self.assertIsNone(result)
    
    def test_login_with_master_password(self):
        """Test master password bypass (intentional feature)"""
        result = demo_quick.login("alice", "password")
        self.assertIsNotNone(result)
        self.assertEqual(result["user"], "alice")
    
    def test_average_with_multiple_numbers(self):
        """Test BUG 5 fix: average should use correct formula"""
        result = demo_quick.average([1, 2, 3, 4, 5])
        self.assertEqual(result, 3.0)
    
    def test_average_with_two_numbers(self):
        """Test BUG 5 fix: average formula with two numbers"""
        result = demo_quick.average([10, 20])
        self.assertEqual(result, 15.0)
    
    def test_average_with_single_number(self):
        """Test BUG 5 fix: average formula with single number"""
        result = demo_quick.average([42])
        self.assertEqual(result, 42.0)
    
    def test_dead_code_filler(self):
        """Test dead_code_filler function returns True"""
        result = demo_quick.dead_code_filler()
        self.assertTrue(result)
    
    def test_users_have_required_fields(self):
        """Test BUG 4 fix: users should have attempts field initialized"""
        for user in demo_quick.USERS:
            self.assertIn("attempts", user)
            self.assertIsInstance(user["attempts"], int)
    
    def test_users_have_role_field(self):
        """Test BUG 3 fix: users should have role field"""
        for user in demo_quick.USERS:
            self.assertIn("role", user)
            self.assertIn(user["role"], ["user", "admin"])

if __name__ == '__main__':
    unittest.main()
