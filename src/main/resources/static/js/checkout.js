document.addEventListener('DOMContentLoaded', function() {
    // Only run checkout functionality if we're on the checkout page
    if (window.location.pathname === '/checkout' || window.location.pathname === '/checkout/') {
        // Get DOM elements
        const orderSummary = document.getElementById('orderSummary');
        const orderTotal = document.getElementById('orderTotal');
        const checkoutForm = document.getElementById('checkoutForm');
        
        // Load cart items and update order summary
        loadOrderSummary();
        
        // Initialize input formatting first
        initializeInputFormatting();
        
        // Pre-populate form with user data if logged in (after input formatting)
        setTimeout(() => {
            populateUserData();
        }, 200);
        
        // Initialize form submission
        if (checkoutForm) {
            checkoutForm.addEventListener('submit', function(event) {
                handlePlaceOrder(event);
            });
            
            // Also add click event to the submit button for debugging
            const submitButton = checkoutForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.addEventListener('click', function(event) {
                    // Submit button clicked
                });
            }
        }

        // Initialize payment method toggle
        initializePaymentMethodToggle();
    }
});

// Pre-populate checkout form with user data
async function populateUserData() {
    try {
        const response = await fetch('/api/auth/user', {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });
        
        if (response.ok) {
            const userData = await response.json();
            
            // Pre-populate form fields with user data
            const fields = {
                'checkoutFullName': userData.fullName || '',
                'checkout-email': userData.email || '',
                'phone': userData.mobileNumber || '',
                'checkoutFlatNo': userData.flatNo || '',
                'checkoutApartmentName': userData.apartmentName || '',
                'checkoutFloor': userData.floor || '',
                'checkoutStreetName': userData.streetName || '',
                'checkoutNearbyLandmark': userData.nearbyLandmark || '',
                'checkoutCity': userData.city || '',
                'checkoutState': userData.state || '',
                'checkoutCountry': userData.country || '',
                'checkoutPincode': userData.pincode || ''
            };
            
            Object.keys(fields).forEach(fieldId => {
                const field = document.getElementById(fieldId);
                if (field) {
                    field.value = fields[fieldId];
                    
                    // Trigger input event to ensure CSS styling is applied
                    field.dispatchEvent(new Event('input', { bubbles: true }));
                    field.dispatchEvent(new Event('change', { bubbles: true }));
                    
                    // Also trigger focus and blur to ensure label positioning
                    field.dispatchEvent(new Event('focus', { bubbles: true }));
                    field.dispatchEvent(new Event('blur', { bubbles: true }));
                    
                    // Force the label to move up if there's a value
                    if (fields[fieldId]) {
                        field.classList.add('has-value');
                        const label = document.querySelector(`label[for="${fieldId}"]`);
                        if (label) {
                            // Force the label to stay up when there's a value
                            label.style.top = '-14px';
                            label.style.fontSize = '14px';
                            label.style.color = '#4B5563'; // text-gray-600
                        }
                    }
                    
                    // Test if the value is actually visible
                    setTimeout(() => {
                        console.log(`Field ${fieldId} final value:`, field.value);
                        console.log(`Field ${fieldId} computed style:`, window.getComputedStyle(field));
                        console.log(`Field ${fieldId} is visible:`, field.offsetParent !== null);
                    }, 100);
                }
            });
        } else {
            console.error('Failed to fetch user data, status:', response.status);
        }
    } catch (error) {
        console.error('Error fetching user data for checkout:', error);
    }
}

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
        const orderSubtotal = document.getElementById('orderSubtotal');
        const orderShipping = document.getElementById('orderShipping');
        const orderTotal = document.getElementById('orderTotal');
        let subtotal = 0;

        if (!orderSummary) {
            return;
        }

        if (!cartItems || cartItems.length === 0) {
            orderSummary.innerHTML = '<p class="text-gray-500">Your cart is empty</p>';
            if (orderSubtotal) orderSubtotal.textContent = '₹0.00';
            if (orderShipping) orderShipping.textContent = '₹0.00';
            if (orderTotal) orderTotal.textContent = '₹0.00';
            return;
        }

        orderSummary.innerHTML = cartItems.map(item => {
            // Calculate unit price and total price based on product type
            let unitPrice = 0, itemTotal = 0, priceUnit = '';
            
            if (item.product) {
                if (item.product.category === 'Janamaz') {
                    // Handle Janamaz products with dynamic pricing based on quantity
                    if (item.quantity > 0 && item.quantity % 12 === 0) {
                        // When quantity is multiple of 12, use dozen pricing
                        unitPrice = item.product.pricePerDozen || 0;
                        const dozens = item.quantity / 12;
                        itemTotal = unitPrice * dozens;
                        priceUnit = '/dozen';
                    } else {
                        // When quantity is not multiple of 12, use per-piece pricing
                        unitPrice = item.product.pricePerPiece || 0;
                        itemTotal = unitPrice * (item.quantity || 1);
                        priceUnit = '/pc';
                    }
                } else {
                    // Handle regular products and products with variants
                    if (item.selectedVariant && item.variantPrice) {
                        unitPrice = item.variantPrice;
                        itemTotal = unitPrice * item.quantity;
                    } else if (item.price) {
                        unitPrice = item.price;
                        itemTotal = unitPrice * item.quantity;
                    } else {
                        unitPrice = item.product.price || 0;
                        itemTotal = unitPrice * item.quantity;
                    }
                }
            }

            const priceString = unitPrice > 0 ? `₹${unitPrice.toFixed(2)} ${priceUnit}`.trim() : 'N/A';
            const totalString = itemTotal > 0 ? `₹${itemTotal.toFixed(2)}` : 'N/A';
            
            let variantText = '';
            if (item.selectedVariant) {
                variantText = `Variant: ${item.selectedVariant}`;
            }

            // Create source indicator for tohfa-e-khulus items
            let sourceText = '';
            if (item.source === 'tohfa-e-khulus') {
                sourceText = `<p class="text-sm text-[#FFD700] font-semibold">🌟 Tohfa-e-Khulus Kit</p>`;
            }

            subtotal += itemTotal;

            return `
                <div class="bg-white rounded-xl shadow p-4 flex items-start space-x-4 animate-fade-in">
                    <img src="${item.product?.imageUrl || ''}" alt="${item.product?.name || 'Product'}" class="w-20 h-20 object-cover rounded-lg flex-shrink-0">
                    <div class="flex-1">
                        <div class="flex justify-between items-start">
                             <div>
                                <h3 class="font-bold text-gray-800">${item.product?.name || 'Product'}</h3>
                                ${variantText ? `<p class="text-sm text-gray-500">${variantText}</p>` : ''}
                                ${sourceText}
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

        // Calculate shipping and total
        const shipping = subtotal > 0 ? 10 : 0; // Same shipping logic as cart
        const total = subtotal + shipping;

        if (orderSubtotal) orderSubtotal.textContent = `₹${subtotal.toFixed(2)}`;
        if (orderShipping) orderShipping.textContent = `₹${shipping.toFixed(2)}`;
        if (orderTotal) orderTotal.textContent = `₹${total.toFixed(2)}`;
    } catch (error) {
        console.error('Error loading order summary:', error);
    }
}

// Renamed from handleCheckout
async function handlePlaceOrder(event) {
    event.preventDefault();

    // 1. Validate form
    if (!validateForm()) {
        return;
    }

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
    
    const cartItems = await getCheckoutCart();
    
    if (!cartItems || cartItems.length === 0) {
        alert('Your cart is empty. Please add items before proceeding to checkout.');
        return;
    }
    
    // Calculate total amount with better error handling
    const subtotalAmount = cartItems.reduce((sum, item) => {
        let itemTotal = 0;
        if (item.product && item.product.category === 'Janamaz') {
            // Handle Janamaz products with dynamic pricing based on quantity
            if (item.quantity > 0 && item.quantity % 12 === 0) {
                // When quantity is multiple of 12, use dozen pricing
                const dozens = item.quantity / 12;
                itemTotal = (item.product.pricePerDozen || 0) * dozens;
            } else {
                // When quantity is not multiple of 12, use per-piece pricing
                itemTotal = (item.product.pricePerPiece || 0) * (item.quantity || 1);
            }
        } else if (item.product) {
            let itemPrice = 0;
            
            // Try to get price from different sources
            if (item.price) {
                itemPrice = item.price;
            } else if (item.variantPrice) {
                itemPrice = item.variantPrice;
            } else if (item.product.price) {
                itemPrice = item.product.price;
            } else {
                console.warn('No price found for item:', item);
                itemPrice = 0;
            }
            
            const quantity = item.quantity || 1;
            itemTotal = itemPrice * quantity;
        }
        return sum + itemTotal;
    }, 0);
    
    // Calculate shipping and total (same logic as cart page)
    const shippingAmount = subtotalAmount > 0 ? 10 : 0;
    const totalAmount = subtotalAmount + shippingAmount;
    
    if (totalAmount <= 0) {
        alert('Unable to calculate order total. Please check your cart items.');
        return;
    }

    // 3. Initiate payment via Razorpay
    if (paymentMethod === 'razorpay') {
        try {
            // Create Razorpay order
            const razorpayOrderResponse = await fetch('/api/payments/razorpay/create-order', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount: totalAmount, currency: 'INR' })
            });
            
            const razorpayOrderData = await razorpayOrderResponse.json();
            
            if (!razorpayOrderResponse.ok || !razorpayOrderData.success) {
                throw new Error(razorpayOrderData.message || 'Failed to create Razorpay order');
            }

            // 4. Open Razorpay payment modal
            const options = {
                key: razorpayOrderData.data.keyId,
                amount: razorpayOrderData.data.amount,
                currency: razorpayOrderData.data.currency,
                name: 'Hajj Umrah',
                description: 'Complete your purchase',
                order_id: razorpayOrderData.data.orderId,
                handler: async function (response) {
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
            alert('Failed to initiate payment. Please try again.');
        }
    } else {
        alert('Unsupported payment method. Please select Razorpay.');
    }
}

// Function to create the final order after successful payment
async function createFinalOrder(fullOrderDetails) {
    try {
        // Calculate shipping and total amounts, and prepare cart items with correct total prices
        const cartItems = fullOrderDetails.cartItems;
        const subtotalAmount = cartItems.reduce((sum, item) => {
            let itemTotal = 0;
            if (item.product && item.product.category === 'Janamaz') {
                // Handle Janamaz products with dynamic pricing based on quantity
                if (item.quantity > 0 && item.quantity % 12 === 0) {
                    // When quantity is multiple of 12, use dozen pricing
                    const dozens = item.quantity / 12;
                    itemTotal = (item.product.pricePerDozen || 0) * dozens;
                } else {
                    // When quantity is not multiple of 12, use per-piece pricing
                    itemTotal = (item.product.pricePerPiece || 0) * (item.quantity || 1);
                }
            } else if (item.product) {
                let itemPrice = 0;
                if (item.price) {
                    itemPrice = item.price;
                } else if (item.variantPrice) {
                    itemPrice = item.variantPrice;
                } else if (item.product.price) {
                    itemPrice = item.product.price;
                }
                const quantity = item.quantity || 1;
                itemTotal = itemPrice * quantity;
            }
            return sum + itemTotal;
        }, 0);
        
        const shippingAmount = subtotalAmount > 0 ? 10 : 0;
        const totalAmount = subtotalAmount + shippingAmount;
        
        // Prepare cart items with correct total prices for each item
        const preparedCartItems = cartItems.map(item => {
            let itemTotalPrice = 0;
            if (item.product && item.product.category === 'Janamaz') {
                // Handle Janamaz products with dynamic pricing based on quantity
                if (item.quantity > 0 && item.quantity % 12 === 0) {
                    // When quantity is multiple of 12, use dozen pricing
                    const dozens = item.quantity / 12;
                    itemTotalPrice = (item.product.pricePerDozen || 0) * dozens;
                } else {
                    // When quantity is not multiple of 12, use per-piece pricing
                    itemTotalPrice = (item.product.pricePerPiece || 0) * (item.quantity || 1);
                }
            } else if (item.product) {
                let itemPrice = 0;
                if (item.price) {
                    itemPrice = item.price;
                } else if (item.variantPrice) {
                    itemPrice = item.variantPrice;
                } else if (item.product.price) {
                    itemPrice = item.product.price;
                }
                const quantity = item.quantity || 1;
                itemTotalPrice = itemPrice * quantity;
            }
            
            // Return item with the total price as the 'price' field
            return {
                ...item,
                price: itemTotalPrice // This is now the total price for this item
            };
        });
        
        // Add shipping information to the order details
        const orderDataWithShipping = {
            ...fullOrderDetails,
            cartItems: preparedCartItems, // Use the prepared cart items with correct total prices
            subtotalAmount: subtotalAmount,
            shippingAmount: shippingAmount,
            totalAmount: totalAmount
        };
        
        const response = await fetch('/api/orders/place', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderDataWithShipping)
        });

        const orderData = await response.json();
        if (!response.ok) {
            throw new Error(orderData.error || 'Failed to place final order');
        }
        
        // 6. Redirect to order confirmation page
        window.location.href = `/order-confirmation?orderId=${orderData.orderId}`;

    } catch (error) {
        alert('Your payment was successful, but we failed to record your order. Please contact support.');
    }
}

// Validate form
function validateForm() {
    const form = document.getElementById('checkoutForm');
    if (!form) {
        return false;
    }
    
    if (!form.checkValidity()) {
        form.reportValidity();
        return false;
    }
    return true;
}

// Get cart items for checkout
async function getCheckoutCart() {
    try {
        const response = await fetch('/api/cart', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            return [];
        }

        const cartData = await response.json();
        return cartData;
    } catch (error) {
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

    const pincodeInput = document.getElementById('checkoutPincode');
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