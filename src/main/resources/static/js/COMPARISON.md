# Bug Fix Comparison

## Before (Buggy Code)

```javascript
// Suspected offending code from checkout flow
function calculateTotal(items, discountCode) {
    let total = 0;
    
    items.forEach(item => {
        total += item.price * item.quantity;
    });
    
    if (discountCode) {
        if (discountCode === "SUMMER20") {
            total = total - 0.2;  // ❌ BUG 1: Subtracts 0.2 dollars, not 20%
        }
    }
    
    return total.toFixed(2);  // ❌ BUG 2: Returns string, not number
}

// Example usage
const cart = [
    { price: 19.99, quantity: 2 },
    { price: 5.0, quantity: 1 }
];

console.log(calculateTotal(cart, "SUMMER20"));
// Output: "44.78" (string) - WRONG! Only 20 cents off instead of 20% off
```

## After (Fixed Code)

```javascript
// Checkout flow function with bug fixes
function calculateTotal(items, discountCode) {
    let total = 0;
    
    items.forEach(item => {
        total += item.price * item.quantity;
    });
    
    if (discountCode) {
        if (discountCode === "SUMMER20") {
            // ✅ FIX 1: Apply 20% discount correctly
            total = total * 0.8;
        }
    }
    
    // ✅ FIX 2: Return a number instead of a string
    return parseFloat(total.toFixed(2));
}

// Example usage
const cart = [
    { price: 19.99, quantity: 2 },
    { price: 5.0, quantity: 1 }
];

console.log(calculateTotal(cart, "SUMMER20"));
// Output: 35.98 (number) - CORRECT! 20% discount applied properly
```

## Impact Analysis

### Bug 1: Discount Calculation
| Cart Total | Buggy Output | Fixed Output | Expected Discount |
|------------|--------------|--------------|-------------------|
| $44.98     | $44.78       | $35.98       | 20% = $8.996      |
| $100.00    | $99.80       | $80.00       | 20% = $20.00      |
| $10.00     | $9.80        | $8.00        | 20% = $2.00       |

**Conclusion:** The buggy code was only giving a $0.20 discount regardless of cart total, while the fixed code correctly applies a 20% discount.

### Bug 2: Return Type
| Operation | Buggy Behavior | Fixed Behavior |
|-----------|----------------|----------------|
| `typeof result` | `"string"` | `"number"` |
| `result + 5` | `"44.785"` (concatenation) | `49.78` (addition) |
| `result * 2` | `89.56` (implicit conversion) | `71.96` (direct multiplication) |

**Conclusion:** The buggy code returns a string, which can cause unexpected behavior in mathematical operations and comparisons. The fixed code returns a proper number type.

## Test Coverage

All edge cases are now covered with comprehensive tests:
- ✅ Basic calculation without discount
- ✅ Calculation with SUMMER20 discount (20% off)
- ✅ Verification that discount is percentage-based
- ✅ Invalid discount codes are properly ignored
- ✅ Return type is number and supports math operations
- ✅ Decimal precision is handled correctly
