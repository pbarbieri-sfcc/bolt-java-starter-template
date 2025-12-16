// Test file for checkout.js calculateTotal function

// Simple test framework for Node.js environment
function assert(condition, message) {
    if (!condition) {
        throw new Error(message || "Assertion failed");
    }
}

function assertEquals(actual, expected, message) {
    if (actual !== expected) {
        throw new Error(`${message || 'Assertion failed'}: expected ${expected}, got ${actual}`);
    }
}

// Import the function (for Node.js)
const path = require('path');
const checkoutPath = path.join(__dirname, '../../main/resources/static/js/checkout.js');
const { calculateTotal } = require(checkoutPath);

// Test 1: Basic calculation without discount
function testBasicCalculation() {
    const cart = [
        { price: 10.0, quantity: 2 },
        { price: 5.0, quantity: 1 }
    ];
    const result = calculateTotal(cart, null);
    assertEquals(result, 25.0, "Basic calculation without discount should return 25.0");
    assertEquals(typeof result, 'number', "Result should be a number, not a string");
    console.log("✓ Test 1 passed: Basic calculation without discount");
}

// Test 2: Calculation with SUMMER20 discount (20% off)
function testDiscountCalculation() {
    const cart = [
        { price: 19.99, quantity: 2 },
        { price: 5.0, quantity: 1 }
    ];
    const result = calculateTotal(cart, "SUMMER20");
    // Total before discount: (19.99 * 2) + 5.0 = 44.98
    // After 20% discount: 44.98 * 0.8 = 35.984, rounded to 35.98
    assertEquals(result, 35.98, "Calculation with SUMMER20 discount should return 35.98");
    assertEquals(typeof result, 'number', "Result should be a number, not a string");
    console.log("✓ Test 2 passed: Calculation with SUMMER20 discount");
}

// Test 3: Discount should reduce total by 20%, not by 0.2 dollars
function testDiscountIsPercentage() {
    const cart = [
        { price: 100.0, quantity: 1 }
    ];
    const result = calculateTotal(cart, "SUMMER20");
    // Should be 100 * 0.8 = 80, not 100 - 0.2 = 99.8
    assertEquals(result, 80.0, "Discount should be 20% of total, not 0.2 dollars");
    console.log("✓ Test 3 passed: Discount is calculated as percentage");
}

// Test 4: Invalid discount code should not apply discount
function testInvalidDiscountCode() {
    const cart = [
        { price: 10.0, quantity: 1 }
    ];
    const result = calculateTotal(cart, "INVALID");
    assertEquals(result, 10.0, "Invalid discount code should not apply discount");
    console.log("✓ Test 4 passed: Invalid discount code ignored");
}

// Test 5: Return type should be number for mathematical operations
function testReturnTypeIsNumber() {
    const cart = [
        { price: 10.5, quantity: 2 }
    ];
    const result = calculateTotal(cart, null);
    assert(typeof result === 'number', "Result type should be number");
    // Verify we can do math operations on the result
    const doubled = result * 2;
    assertEquals(doubled, 42.0, "Result should support mathematical operations");
    console.log("✓ Test 5 passed: Return type is number and supports math operations");
}

// Test 6: Decimal precision (toFixed should round correctly)
function testDecimalPrecision() {
    const cart = [
        { price: 10.999, quantity: 1 }
    ];
    const result = calculateTotal(cart, null);
    assertEquals(result, 11.0, "Result should be rounded to 2 decimal places");
    console.log("✓ Test 6 passed: Decimal precision handled correctly");
}

// Run all tests
console.log("Running calculateTotal tests...\n");
try {
    testBasicCalculation();
    testDiscountCalculation();
    testDiscountIsPercentage();
    testInvalidDiscountCode();
    testReturnTypeIsNumber();
    testDecimalPrecision();
    console.log("\n✓ All tests passed!");
} catch (error) {
    console.error("\n✗ Test failed:", error.message);
    process.exit(1);
}
