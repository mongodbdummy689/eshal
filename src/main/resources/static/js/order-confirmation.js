// This file is now intentionally left blank.
// All order details are rendered server-side via Thymeleaf.
// If you want to send notifications, you can add that logic here.

// Order confirmation page JavaScript
// Notifications are now handled server-side for better reliability

document.addEventListener('DOMContentLoaded', function() {
    console.log('Order confirmation page loaded successfully');
    
    // Clear cart after successful order placement
    clearCartAfterOrder();
});

// Function to clear cart after successful order placement
async function clearCartAfterOrder() {
    try {
        // Check if we're on a successful order confirmation page
        const successIndicator = document.querySelector('.text-green-500'); // Success checkmark icon
        if (successIndicator) {
            console.log('Order confirmed successfully, clearing cart...');
            
            // Clear the cart
            const success = await clearCart();
            if (success) {
                console.log('Cart cleared successfully after order placement');
            } else {
                console.warn('Failed to clear cart after order placement');
            }
        } else {
            console.log('Not a successful order confirmation, skipping cart clear');
        }
    } catch (error) {
        console.error('Error clearing cart after order placement:', error);
    }
} 