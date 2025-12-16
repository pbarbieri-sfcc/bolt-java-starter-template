// Checkout flow function with bug fixes
function calculateTotal(items, discountCode) {
    let total = 0;
    
    items.forEach(item => {
        total += item.price * item.quantity;
    });
    
    if (discountCode) {
        if (discountCode === "SUMMER20") {
            // Fixed: Apply 20% discount correctly (multiply by 0.8 or subtract 20% of total)
            total = total * 0.8;
        }
    }
    
    // Fixed: Return a number instead of a string
    return parseFloat(total.toFixed(2));
}

// Example usage
const cart = [
    { price: 19.99, quantity: 2 },
    { price: 5.0, quantity: 1 }
];

console.log(calculateTotal(cart, "SUMMER20"));

// Export for use in other modules or tests
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { calculateTotal };
}
