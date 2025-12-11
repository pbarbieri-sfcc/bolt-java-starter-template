def multiply(a, b):
    # Fixed BUG 1: returns multiplication instead of addition
    return a * b

def get_full_name(first, last):
    # Fixed BUG 2: added space between names
    return first + " " + last

def divide(x, y):
    # Fixed BUG 3: handles division by zero
    if y == 0:
        raise ValueError("Cannot divide by zero")
    return x / y

def find_item(items, target):
    # Fixed BUG 4: returns the correct boolean
    return target in items
