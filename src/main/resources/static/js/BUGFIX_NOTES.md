# Bug Fixes for calculateTotal Function

## Overview
This document describes the bugs found in the `calculateTotal` function and the fixes applied.

## Bugs Identified

### Bug 1: Incorrect Discount Calculation
**Original Code:**
```javascript
if (discountCode === "SUMMER20") {
    total = total - 0.2; // ❌ Subtracts 0.2 dollars instead of 20%
}
```

**Issue:** The code was subtracting `0.2` (20 cents) from the total instead of applying a 20% discount.

**Fixed Code:**
```javascript
if (discountCode === "SUMMER20") {
    total = total * 0.8; // ✅ Applies 20% discount correctly
}
```

**Impact:** 
- For a cart total of $44.98, the original code would calculate $44.78 (only 20 cents off)
- The corrected code calculates $35.98 (20% off = $8.996 discount)

### Bug 2: Incorrect Return Type
**Original Code:**
```javascript
return total.toFixed(2); // ❌ Returns string, not number
```

**Issue:** The `toFixed(2)` method returns a string representation of the number, not a number itself. This can cause issues when the result is used in further mathematical operations.

**Fixed Code:**
```javascript
return parseFloat(total.toFixed(2)); // ✅ Returns number
```

**Impact:**
- The original code would return `"35.98"` (string)
- The corrected code returns `35.98` (number)
- This ensures the result can be used in mathematical operations without type coercion

## Test Results
All test cases pass successfully:
- ✓ Basic calculation without discount
- ✓ Calculation with SUMMER20 discount
- ✓ Discount is calculated as percentage (not fixed amount)
- ✓ Invalid discount codes are ignored
- ✓ Return type is number and supports math operations
- ✓ Decimal precision is handled correctly

## Example Usage
```javascript
const cart = [
    { price: 19.99, quantity: 2 },
    { price: 5.0, quantity: 1 }
];

console.log(calculateTotal(cart, "SUMMER20"));
// Output: 35.98 (number)
// Calculation: (19.99 * 2 + 5.0) * 0.8 = 44.98 * 0.8 = 35.98
```
