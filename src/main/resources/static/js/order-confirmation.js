document.addEventListener('DOMContentLoaded', function() {
    // Get order ID from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId');

    if (orderId) {
        loadOrderDetails(orderId);
    }
});

async function loadOrderDetails(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load order details');
        }

        const order = await response.json();
        displayOrderDetails(order);
        sendMobileNotification(order);
    } catch (error) {
        console.error('Error loading order details:', error);
        alert('Failed to load order details. Please try again later.');
    }
}

function displayOrderDetails(order) {
    // Display order details
    document.getElementById('orderId').textContent = order.id;
    document.getElementById('orderDate').textContent = new Date(order.orderDate).toLocaleDateString();
    document.getElementById('orderTotal').textContent = `$${order.totalAmount.toFixed(2)}`;

    // Display shipping information
    document.getElementById('shippingAddress').textContent = formatAddress(order.shippingAddress);
    document.getElementById('contactNumber').textContent = order.contactInfo.phone;
    document.getElementById('email').textContent = order.contactInfo.email;
}

function formatAddress(address) {
    return `${address.street}, ${address.city}, ${address.state}, ${address.country} ${address.zipCode}`;
}

async function sendMobileNotification(order) {
    try {
        const response = await fetch('/api/notifications/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                phoneNumber: '8369737670',
                message: `Your order #${order.id} has been confirmed. Total amount: $${order.totalAmount.toFixed(2)}. Your product will be shipped within 24 hours and delivered within 5-7 days.`
            })
        });

        if (!response.ok) {
            throw new Error('Failed to send notification');
        }

        console.log('Mobile notification sent successfully');
    } catch (error) {
        console.error('Error sending mobile notification:', error);
    }
} 