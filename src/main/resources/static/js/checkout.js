document.addEventListener('DOMContentLoaded', function() {
    // Load cart items and update order summary
    loadOrderSummary();
    
    // Initialize form submission
    const checkoutForm = document.getElementById('checkoutForm');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', handleCheckout);
    }

    // Initialize input formatting
    initializeInputFormatting();

    // Initialize payment method toggle
    initializePaymentMethodToggle();
});

// Initialize payment method toggle
function initializePaymentMethodToggle() {
    const paymentMethodInputs = document.querySelectorAll('input[name="paymentMethod"]');
    const cardFields = document.getElementById('cardPaymentFields');
    const upiFields = document.getElementById('upiPaymentFields');

    if (cardFields && upiFields) {
        paymentMethodInputs.forEach(input => {
            input.addEventListener('change', function() {
                if (this.value === 'card') {
                    cardFields.classList.remove('hidden');
                    upiFields.classList.add('hidden');
                } else {
                    cardFields.classList.add('hidden');
                    upiFields.classList.remove('hidden');
                }
            });
        });
    }
}

// Load order summary from cart
async function loadOrderSummary() {
    try {
        const cartItems = await getCart();
        const orderSummary = document.getElementById('orderSummary');
        const orderTotal = document.getElementById('orderTotal');
        let total = 0;

        if (!orderSummary) return;

        if (cartItems.length === 0) {
            orderSummary.innerHTML = '<p class="text-gray-500">Your cart is empty</p>';
            if (orderTotal) orderTotal.textContent = '$0.00';
            return;
        }

        orderSummary.innerHTML = cartItems.map(item => {
            const itemTotal = item.price * item.quantity;
            total += itemTotal;
            return `
                <div class="flex justify-between items-center">
                    <div>
                        <h3 class="font-medium">${item.productId}</h3>
                        <p class="text-sm text-gray-500">Quantity: ${item.quantity}</p>
                    </div>
                    <p class="font-medium">$${itemTotal.toFixed(2)}</p>
                </div>
            `;
        }).join('');

        if (orderTotal) orderTotal.textContent = `$${total.toFixed(2)}`;
    } catch (error) {
        console.error('Error loading order summary:', error);
    }
}

// Handle checkout form submission
async function handleCheckout(event) {
    event.preventDefault();

    try {
        console.log('Starting checkout process...');

        // Validate form
        if (!validateForm()) {
            console.log('Form validation failed');
            return;
        }

        // Get form data
        const formData = new FormData(event.target);
        const orderDetails = {
            email: formData.get('email'),
            contactNumber: formData.get('phone'),
            shippingAddress: `${formData.get('address')}, ${formData.get('city')}, ${formData.get('state')}, ${formData.get('country')} ${formData.get('zipCode')}`,
            paymentMethod: formData.get('paymentMethod')
        };

        console.log('Order details:', orderDetails);

        // Get cart total
        const cartItems = await getCart();
        const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        orderDetails.totalAmount = total;

        console.log('Cart total:', total);

        // Create order first
        console.log('Creating order...');
        const orderResponse = await fetch('/api/orders/place', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderDetails)
        });

        if (!orderResponse.ok) {
            const errorData = await orderResponse.json();
            console.error('Order creation failed:', errorData);
            throw new Error(errorData.error || 'Failed to create order');
        }

        const orderData = await orderResponse.json();
        const orderId = orderData.orderId;
        console.log('Order created successfully:', orderId);

        // Get selected payment method
        const paymentMethod = formData.get('paymentMethod');
        console.log('Selected payment method:', paymentMethod);

        if (paymentMethod === 'phonepe') {
            // Initiate PhonePe payment
            console.log('Initiating PhonePe payment...');
            const paymentRequest = {
                orderId: orderId,
                amount: total,
                customerPhone: orderDetails.contactNumber.replace(/\D/g, '') // Remove non-digits
            };
            console.log('PhonePe payment request:', paymentRequest);

            try {
                const paymentResponse = await fetch('/api/payments/phonepe/initiate', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(paymentRequest)
                });

                const paymentData = await paymentResponse.json();
                console.log('PhonePe payment response:', paymentData);
                
                if (!paymentResponse.ok) {
                    console.error('PhonePe payment initiation failed:', paymentData);
                    throw new Error(paymentData.message || 'Failed to initiate payment');
                }

                if (!paymentData.success || !paymentData.paymentUrl) {
                    console.error('Invalid PhonePe response:', paymentData);
                    throw new Error('Invalid payment response from server');
                }

                console.log('Redirecting to PhonePe payment page:', paymentData.paymentUrl);
                // Redirect to PhonePe payment page
                window.location.href = paymentData.paymentUrl;
            } catch (error) {
                console.error('PhonePe payment error:', error);
                alert('Failed to initiate PhonePe payment. Please try again or choose a different payment method.');
                return;
            }
        } else {
            // For other payment methods, redirect to order confirmation
            console.log('Redirecting to order confirmation page');
            window.location.href = `/api/orders/${orderId}`;
        }

    } catch (error) {
        console.error('Error during checkout:', error);
        alert(error.message || 'An error occurred during checkout. Please try again.');
    }
}

// Validate form
function validateForm() {
    const form = document.getElementById('checkoutForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return false;
    }
    return true;
}

// Get cart items
async function getCart() {
    try {
        const response = await fetch('/api/cart');
        if (!response.ok) {
            throw new Error('Failed to fetch cart items');
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching cart:', error);
        throw error;
    }
}

// Initialize input formatting
function initializeInputFormatting() {
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 10) value = value.slice(0, 10);
            e.target.value = value;
        });
    }

    const zipCodeInput = document.getElementById('zipCode');
    if (zipCodeInput) {
        zipCodeInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 6) value = value.slice(0, 6);
            e.target.value = value;
        });
    }

    // Format card number with spaces
    const cardNumberInput = document.getElementById('cardNumber');
    if (cardNumberInput) {
        cardNumberInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\s/g, '');
            if (value.length > 16) value = value.slice(0, 16);
            e.target.value = value.replace(/(\d{4})/g, '$1 ').trim();
        });
    }

    // Format expiry date
    const expiryDateInput = document.getElementById('expiryDate');
    if (expiryDateInput) {
        expiryDateInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length > 2) {
                value = value.slice(0, 2) + '/' + value.slice(2, 4);
            }
            e.target.value = value;
        });
    }

    // Format CVV
    const cvvInput = document.getElementById('cvv');
    if (cvvInput) {
        cvvInput.addEventListener('input', function(e) {
            e.target.value = e.target.value.replace(/\D/g, '').slice(0, 3);
        });
    }
}

// Validation helpers
function validateCardNumber(number) {
    return /^\d{16}$/.test(number.replace(/\s/g, ''));
}

function validateExpiryDate(date) {
    if (!/^\d{2}\/\d{2}$/.test(date)) return false;
    
    const [month, year] = date.split('/');
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear() % 100;
    const currentMonth = currentDate.getMonth() + 1;

    const monthNum = parseInt(month);
    const yearNum = parseInt(year);

    if (monthNum < 1 || monthNum > 12) return false;
    if (yearNum < currentYear || (yearNum === currentYear && monthNum < currentMonth)) return false;

    return true;
}

function validateCVV(cvv) {
    return /^\d{3}$/.test(cvv);
}

function validateUPIId(upiId) {
    return /^[a-zA-Z0-9._-]+@[a-zA-Z]{3,}$/.test(upiId);
} 