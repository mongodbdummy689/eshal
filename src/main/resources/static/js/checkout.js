console.log('Checkout.js loaded successfully!');

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded - Checkout.js');
    console.log('Current pathname:', window.location.pathname);
    
    // Only run checkout functionality if we're on the checkout page
    if (window.location.pathname === '/checkout' || window.location.pathname === '/checkout/') {
        console.log('On checkout page, initializing checkout functionality');
        
        // Get DOM elements
        const orderSummary = document.getElementById('orderSummary');
        const orderTotal = document.getElementById('orderTotal');
        const checkoutForm = document.getElementById('checkoutForm');
        
        console.log('Found elements:', {
            orderSummary: !!orderSummary,
            orderTotal: !!orderTotal,
            checkoutForm: !!checkoutForm
        });
        
        // Load cart items and update order summary
        loadOrderSummary();
        
        // Initialize form submission
        if (checkoutForm) {
            console.log('Adding submit event listener to checkout form');
            checkoutForm.addEventListener('submit', function(event) {
                console.log('Form submit event triggered!');
                handlePlaceOrder(event);
            });
            
            // Also add click event to the submit button for debugging
            const submitButton = checkoutForm.querySelector('button[type="submit"]');
            if (submitButton) {
                console.log('Found submit button, adding click listener');
                submitButton.addEventListener('click', function(event) {
                    console.log('Submit button clicked!');
                });
            } else {
                console.error('Submit button not found!');
            }
        } else {
            console.error('Checkout form not found!');
        }

        // Initialize input formatting
        initializeInputFormatting();

        // Initialize payment method toggle
        initializePaymentMethodToggle();
    } else {
        console.log('Not on checkout page, skipping checkout initialization');
    }
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
        const cartItems = await getCheckoutCart();
        
        const orderSummary = document.getElementById('orderSummary');
        const orderTotal = document.getElementById('orderTotal');
        let total = 0;

        if (!orderSummary) {
            return;
        }

        if (!cartItems || cartItems.length === 0) {
            orderSummary.innerHTML = '<p class="text-gray-500">Your cart is empty</p>';
            if (orderTotal) orderTotal.textContent = '₹0.00';
            return;
        }

        orderSummary.innerHTML = cartItems.map(item => {
            // Calculate prices based on product type
            let displayPrice = 0, itemTotal = 0;
            
            if (item.product) {
                if (item.product.category === 'Janamaz') {
                    // Handle Janamaz products
                    if (item.quantity === 12) {
                        displayPrice = item.product.pricePerDozen || 0;
                        itemTotal = displayPrice;
                    } else {
                        displayPrice = item.product.pricePerPiece || 0;
                        itemTotal = displayPrice * item.quantity;
                    }
                } else {
                    // Handle regular products and products with variants
                    if (item.selectedVariant && item.variantPrice) {
                        displayPrice = item.variantPrice;
                        itemTotal = displayPrice * item.quantity;
                    } else if (item.price) {
                        displayPrice = item.price;
                        itemTotal = displayPrice * item.quantity;
                    } else {
                        displayPrice = item.product.price || 0;
                        itemTotal = displayPrice * item.quantity;
                    }
                }
            }

            const priceString = displayPrice > 0 ? `₹${displayPrice.toFixed(2)} ${item.product?.category === 'Janamaz' && item.quantity === 12 ? '/dozen' : ''}` : 'N/A';
            const totalString = itemTotal > 0 ? `₹${itemTotal.toFixed(2)}` : 'N/A';
            
            let variantText = '';
            if (item.selectedVariant) {
                variantText = `Variant: ${item.selectedVariant}`;
            }

            total += itemTotal;

            return `
                <div class="bg-white rounded-xl shadow p-4 flex items-start space-x-4 animate-fade-in">
                    <img src="${item.product?.imageUrl || ''}" alt="${item.product?.name || 'Product'}" class="w-20 h-20 object-cover rounded-lg flex-shrink-0">
                    <div class="flex-1">
                        <div class="flex justify-between items-start">
                             <div>
                                <h3 class="font-bold text-gray-800">${item.product?.name || 'Product'}</h3>
                                ${variantText ? `<p class="text-sm text-gray-500">${variantText}</p>` : ''}
                             </div>
                             <p class="font-bold text-lg text-gray-800">${totalString}</p>
                        </div>
                        <div class="text-sm text-gray-600 mt-2">
                           <p><span>Quantity:</span> <span class="font-semibold">${item.quantity || 1}</span></p>
                           <p><span>Unit Price:</span> <span class="font-semibold">${priceString}</span></p>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        if (orderTotal) orderTotal.textContent = `₹${total.toFixed(2)}`;
    } catch (error) {
        console.error('Error loading order summary:', error);
    }
}

// Renamed from handleCheckout
async function handlePlaceOrder(event) {
    event.preventDefault();
    console.log('handlePlaceOrder called - starting checkout process...');

    // 1. Validate form
    if (!validateForm()) {
        console.log('Form validation failed');
        return;
    }
    console.log('Form validation passed');

    // 2. Get form data and cart items
    const formData = new FormData(event.target);
    const customerDetails = {
        fullName: formData.get('fullName'),
        email: formData.get('email') || 'customer@example.com', // Fallback email
        contactNumber: formData.get('phone'),
        flatNo: formData.get('flatNo'),
        apartmentName: formData.get('apartmentName'),
        floor: formData.get('floor'),
        streetName: formData.get('streetName'),
        nearbyLandmark: formData.get('nearbyLandmark'),
        city: formData.get('city'),
        pincode: formData.get('pincode'),
        state: formData.get('state'),
        country: formData.get('country'),
        // Legacy field for backward compatibility
        shippingAddress: `${formData.get('flatNo')}, ${formData.get('apartmentName')}, Floor ${formData.get('floor')}, ${formData.get('streetName')}, Near ${formData.get('nearbyLandmark')}, ${formData.get('city')}, ${formData.get('state')}, ${formData.get('country')} - ${formData.get('pincode')}`
    };
    const paymentMethod = formData.get('paymentMethod');
    
    console.log('Getting cart items...');
    const cartItems = await getCheckoutCart();
    console.log('Cart items:', cartItems);
    
    if (!cartItems || cartItems.length === 0) {
        console.error('Cart is empty!');
        alert('Your cart is empty. Please add items before proceeding to checkout.');
        return;
    }
    
    // Calculate total amount with better error handling
    const totalAmount = cartItems.reduce((sum, item) => {
        let itemPrice = 0;
        
        // Try to get price from different sources
        if (item.price) {
            itemPrice = item.price;
        } else if (item.variantPrice) {
            itemPrice = item.variantPrice;
        } else if (item.product && item.product.price) {
            itemPrice = item.product.price;
        } else {
            console.warn('No price found for item:', item);
            itemPrice = 0;
        }
        
        const quantity = item.quantity || 1;
        return sum + (itemPrice * quantity);
    }, 0);
    
    console.log('Calculated total amount:', totalAmount);
    
    if (totalAmount <= 0) {
        console.error('Total amount is zero or negative!');
        alert('Unable to calculate order total. Please check your cart items.');
        return;
    }

    console.log('Customer details:', customerDetails);
    console.log('Cart total:', totalAmount);
    console.log('Payment method:', paymentMethod);

    // 3. Initiate payment via Razorpay
    if (paymentMethod === 'razorpay') {
        console.log('Initiating Razorpay payment...');
        try {
            // Create Razorpay order
            console.log('Creating Razorpay order with amount:', totalAmount);
            const razorpayOrderResponse = await fetch('/api/payments/razorpay/create-order', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount: totalAmount, currency: 'INR' })
            });
            console.log('Razorpay order response status:', razorpayOrderResponse.status);
            
            const razorpayOrderData = await razorpayOrderResponse.json();
            console.log('Razorpay order data:', razorpayOrderData);
            
            if (!razorpayOrderResponse.ok || !razorpayOrderData.success) {
                throw new Error(razorpayOrderData.message || 'Failed to create Razorpay order');
            }

            // 4. Open Razorpay payment modal
            console.log('Opening Razorpay payment modal...');
            const options = {
                key: razorpayOrderData.data.keyId,
                amount: razorpayOrderData.data.amount,
                currency: razorpayOrderData.data.currency,
                name: 'Hajj Umrah',
                description: 'Complete your purchase',
                order_id: razorpayOrderData.data.orderId,
                handler: async function (response) {
                    console.log('Razorpay payment successful:', response);
                    
                    // 5. On successful payment, create the order in our database
                    await createFinalOrder({
                        customerDetails,
                        cartItems,
                        paymentMethod,
                        paymentDetails: response // Includes razorpay_payment_id etc.
                    });
                },
                prefill: {
                    name: (customerDetails.email || 'customer@example.com').split('@')[0] || 'Customer',
                    email: customerDetails.email || 'customer@example.com',
                    contact: customerDetails.contactNumber
                },
                theme: { color: '#1f2937' },
                modal: { ondismiss: () => console.log('Payment modal dismissed') }
            };
            const rzp = new Razorpay(options);
            rzp.open();

        } catch (error) {
            console.error('Razorpay payment error:', error);
            alert('Failed to initiate payment. Please try again.');
        }
    } else {
        console.error('Unsupported payment method:', paymentMethod);
        alert('Unsupported payment method. Please select Razorpay.');
    }
}

// Function to create the final order after successful payment
async function createFinalOrder(fullOrderDetails) {
    try {
        console.log('Creating final order with details:', fullOrderDetails);
        const response = await fetch('/api/orders/place', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(fullOrderDetails)
        });

        const orderData = await response.json();
        if (!response.ok) {
            throw new Error(orderData.error || 'Failed to place final order');
        }
        
        console.log('Order created successfully in database:', orderData);
        
        // 6. Redirect to order confirmation page
        window.location.href = `/order-confirmation?orderId=${orderData.orderId}`;

    } catch (error) {
        console.error('Error creating final order:', error);
        alert('Your payment was successful, but we failed to record your order. Please contact support.');
    }
}

// Validate form
function validateForm() {
    console.log('validateForm called');
    const form = document.getElementById('checkoutForm');
    if (!form) {
        console.error('Checkout form not found');
        return false;
    }
    console.log('Form found, checking validity...');
    console.log('Form validity:', form.checkValidity());
    
    if (!form.checkValidity()) {
        console.log('Form validation failed, reporting validity...');
        form.reportValidity();
        return false;
    }
    console.log('Form validation passed');
    return true;
}

// Get cart items for checkout
async function getCheckoutCart() {
    console.log('getCheckoutCart called');
    try {
        const response = await fetch('/api/cart', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        console.log('Cart API response status:', response.status);

        if (!response.ok) {
            console.error('Cart API response not ok');
            return [];
        }

        const cartData = await response.json();
        console.log('Cart data retrieved:', cartData);
        return cartData;
    } catch (error) {
        console.error('Error fetching cart:', error);
        return [];
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

    const pincodeInput = document.getElementById('pincode');
    if (pincodeInput) {
        pincodeInput.addEventListener('input', function(e) {
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