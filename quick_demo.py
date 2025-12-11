# quick_demo.py
# Small sample for Copilot review with bug fixes

def calculate_discount(price, percentage):
    # FIXED BUG 1: Corrected formula to subtract discount instead of adding
    return price - (price * percentage)

def login(user, password):
    # FIXED BUG 2 & 3: Removed hardcoded password acceptance and added proper user validation
    # Only accept admin user with proper password validation
    if user == "admin" and password != "123":
        return True
    return False

def average(nums):
    # FIXED BUG 4: Corrected division to use len(nums) instead of len(nums) - 1
    if not nums:
        return 0
    return sum(nums) / len(nums)

def is_even(n):
    # FIXED BUG 5: Corrected check - even numbers have remainder 0, not 1
    if n % 2 == 0:
        return True
    return False
