// Initialize cart items cache
let cartItemsCache = [];

// Make addToCart available globally
window.addToCart = async function(productId, quantity = 1, price = null, selectedVariant = null, variantPrice = null, source = null) {
    try {
        const requestBody = { productId, quantity, price, selectedVariant, variantPrice, source };

        const response = await fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify(requestBody)
        });

        const responseText = await response.text();

        if (!response.ok) {
            let errorMessage = 'Failed to add item to cart';
            try {
                const errorData = JSON.parse(responseText);
                errorMessage = errorData.message || errorMessage;
            } catch (e) {
                console.error('Error parsing error response:', e);
            }
            throw new Error(errorMessage);
        }

        await updateCartCount();
        return true;
    } catch (error) {
        console.error('Error adding item to cart:', error);
        throw error;
    }
};

// Initialize cart UI elements
document.addEventListener('DOMContentLoaded', function() {
    // Load cart items
    loadCartItems();

    // Initialize checkout button
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', proceedToCheckout);
    }
});

// Load cart items
async function loadCartItems() {
    try {
        const response = await fetch('/api/cart');
        if (!response.ok) {
            throw new Error('Failed to load cart items');
        }

        const cartItems = await response.json();
        cartItemsCache = cartItems;
        updateCartUI(cartItems);
        await updateCartCount();
    } catch (error) {
        console.error('Error loading cart items:', error);
        showError('Failed to load cart items. Please try again.');
    }
}

// Update cart UI
async function updateCartUI(cartItems) {
    const cartItemsContainer = document.getElementById('cartItems');
    const emptyCartDiv = document.getElementById('emptyCart');
    const cartSummaryDiv = document.getElementById('cartSummary');
    const subtotalElement = document.getElementById('subtotal');
    const totalElement = document.getElementById('total');

    if (!cartItemsContainer || !emptyCartDiv || !cartSummaryDiv || !subtotalElement || !totalElement) {
        return;
    }

    if (!cartItems || cartItems.length === 0) {
        cartItemsContainer.innerHTML = '';
        emptyCartDiv.classList.remove('hidden');
        cartSummaryDiv.classList.add('hidden');
        subtotalElement.textContent = '₹0.00';
        totalElement.textContent = '₹0.00';
        return;
    }

    emptyCartDiv.classList.add('hidden');
    cartSummaryDiv.classList.remove('hidden');
    cartItemsContainer.innerHTML = cartItems.map(item => {
        // Calculate unit price and total price based on product type
        let unitPrice = 0, itemTotal = 0, priceUnit = '', gstAmount = 0, itemTotalWithGst = 0;
        
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
                    itemTotal = unitPrice * item.quantity;
                    priceUnit = '/pc';
                }
            } else {
                // Handle regular products and products with variants
                if (item.selectedVariant && item.variantPrice) {
                    // Use variant price if available
                    unitPrice = item.variantPrice;
                    itemTotal = unitPrice * item.quantity;
                } else if (item.price) {
                    // Use stored price (which should be unit price)
                    unitPrice = item.price;
                    itemTotal = unitPrice * item.quantity;
                } else {
                    // Use product price as fallback
                    unitPrice = item.product.price || 0;
                    itemTotal = unitPrice * item.quantity;
                }
            }
            
            // Calculate GST using product's GST rate
            const gstRate = item.product.gstRate || 5.0; // Default to 5% if not set
            gstAmount = (itemTotal * gstRate) / 100;
            itemTotalWithGst = itemTotal + gstAmount;
        }

        const priceDisplay = unitPrice > 0 ? `₹${unitPrice.toFixed(2)} ${priceUnit}`.trim() : '';
        const totalDisplay = itemTotal > 0 ? `₹${itemTotal.toFixed(2)}` : '';
        const gstRate = item.product.gstRate || 5.0; // Default to 5% if not set
        const gstDisplay = gstAmount > 0 ? `₹${gstAmount.toFixed(2)}` : '';
        const totalWithGstDisplay = itemTotalWithGst > 0 ? `₹${itemTotalWithGst.toFixed(2)}` : '';
        
        // Create variant display text
        let variantDisplay = '';
        if (item.selectedVariant) {
            variantDisplay = `<p class="text-sm text-gray-500">Variant: ${item.selectedVariant}</p>`;
        }

        // Create source indicator for tohfa-e-khulus items
        let sourceDisplay = '';
        if (item.source === 'tohfa-e-khulus') {
            sourceDisplay = `<p class="text-sm text-[#FFD700] font-semibold">🌟 Tohfa-e-Khulus Kit</p>`;
        }

        return `
        <div class="bg-white rounded-lg shadow-md p-4 flex items-center space-x-4" data-item-id="${item.id}">
            <img src="${item.product?.imageUrl || ''}" alt="${item.product?.name || 'Product'}" class="w-24 h-24 object-cover rounded-lg">
            <div class="flex-1">
                <h3 class="font-semibold">${item.product?.name || 'Product'}</h3>
                ${variantDisplay}
                ${sourceDisplay}
                <div class="flex justify-between items-center">
                    <div class="text-gray-600">
                        ${priceDisplay ? `<p>${priceDisplay}</p>` : ''}
                        ${totalDisplay ? `<p>Subtotal: ${totalDisplay}</p>` : ''}
                        ${gstDisplay ? `<p class="text-sm text-green-600">GST: ${gstDisplay}</p>` : ''}
                        ${totalWithGstDisplay ? `<p class="font-semibold text-primary">Total: ${totalWithGstDisplay}</p>` : ''}
                    </div>
                    <div class="flex items-center space-x-4">
                        <div class="flex items-center space-x-2">
                            <button class="quantity-btn" onclick="updateQuantity('${item.id}', ${Math.max(1, item.quantity - 1)})">-</button>
                            <span class="quantity">${item.quantity}</span>
                            <button class="quantity-btn" onclick="updateQuantity('${item.id}', ${item.quantity + 1})">+</button>
                        </div>
                        <button class="text-red-500 hover:text-red-700" onclick="removeItem('${item.id}')">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `}).join('');

    // Calculate subtotal and GST
    let subtotal = 0;
    let totalGst = 0;
    
    cartItems.forEach(item => {
        let itemTotal = 0;
        if (item.product && item.product.category === 'Janamaz') {
            if (item.quantity > 0 && item.quantity % 12 === 0) {
                const dozens = item.quantity / 12;
                itemTotal = (item.product.pricePerDozen || 0) * dozens;
            } else {
                itemTotal = (item.product.pricePerPiece || 0) * item.quantity;
            }
        } else if (item.product) {
            let itemPrice = 0;
            if (item.selectedVariant && item.variantPrice) {
                itemPrice = item.variantPrice;
            } else if (item.price) {
                itemPrice = item.price;
            } else {
                itemPrice = item.product.price || 0;
            }
            itemTotal = itemPrice * item.quantity;
        }
        subtotal += itemTotal;
        // Calculate GST using product's GST rate
        const gstRate = item.product.gstRate || 5.0; // Default to 5% if not set
        totalGst += (itemTotal * gstRate) / 100;
    });

    const totalWithGst = subtotal + totalGst;

    // Update summary display
    subtotalElement.textContent = `₹${subtotal.toFixed(2)}`;
    totalElement.textContent = `₹${totalWithGst.toFixed(2)}`;
    
    // Add GST breakdown to summary if not already present
    const cartSummary = document.querySelector('#cartSummary .bg-white');
    if (cartSummary) {
        let gstRow = cartSummary.querySelector('.gst-row');
        if (!gstRow) {
            const subtotalRow = cartSummary.querySelector('.space-y-4');
            if (subtotalRow) {
                const gstHtml = `
                    <div class="flex justify-between gst-row">
                        <span>GST</span>
                        <span id="gstAmount">₹${totalGst.toFixed(2)}</span>
                    </div>
                `;
                subtotalRow.insertAdjacentHTML('beforeend', gstHtml);
            }
        } else {
            // Update existing GST row with correct amount
            gstRow.querySelector('#gstAmount').textContent = `₹${totalGst.toFixed(2)}`;
        }
    }
}

// Update item quantity
async function updateQuantity(itemId, newQuantity) {
    try {
        const item = cartItemsCache.find(item => item.id === itemId);
        if (!item) {
            return;
        }

        // Calculate unit price (not total price) based on product type and variant
        let unitPrice;
        if (item.product.category === 'Janamaz') {
            // For Janamaz products, determine unit price based on new quantity
            if (newQuantity > 0 && newQuantity % 12 === 0) {
                // When quantity is multiple of 12, use dozen pricing
                unitPrice = item.product.pricePerDozen || 0;
            } else {
                // When quantity is not multiple of 12, use per-piece pricing
                unitPrice = item.product.pricePerPiece || 0;
            }
        } else {
            // For products with variants, use variant price
            if (item.selectedVariant && item.variantPrice) {
                unitPrice = item.variantPrice;
            } else if (item.price) {
                // Use stored price (which should be unit price)
                unitPrice = item.price;
            } else {
                // For regular products, use product price
                unitPrice = item.product.price || 0;
            }
        }

        const response = await fetch(`/api/cart/${itemId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify({ quantity: newQuantity, price: unitPrice })
        });

        if (!response.ok) {
            throw new Error('Failed to update quantity');
        }

        // Reload cart items to reflect changes
        await loadCartItems();
        await updateCartCount();
    } catch (error) {
        console.error('Error updating quantity:', error);
        showError('Failed to update quantity. Please try again.');
    }
}

// Remove item from cart
async function removeItem(itemId) {
    if (!confirm('Are you sure you want to remove this item?')) {
        return;
    }

    try {
        const response = await fetch(`/api/cart/${itemId}`, {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Failed to remove item');
        }

        // Update local cache
        cartItemsCache = cartItemsCache.filter(item => item.id !== itemId);
        updateCartUI(cartItemsCache);
        await updateCartCount();
    } catch (error) {
        console.error('Error removing item:', error);
        alert('Failed to remove item. Please try again.');
    }
}

// Renamed from handleCheckout to avoid conflicts
async function proceedToCheckout() {
    // This function's purpose is to navigate the user to the checkout page.
    window.location.href = '/checkout';
}

// Function to display a generic error message
function showError(message) {
    alert(message);
}

// Update cart count in the UI
async function updateCartCount() {
    try {
        const cartItems = await getCart();
        const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
        
        // Update desktop cart count
        const desktopCartCount = document.getElementById('cartCount');
        if (desktopCartCount) {
            desktopCartCount.textContent = totalItems;
        }

        // Update mobile cart count
        const mobileCartCount = document.getElementById('mobileCartCount');
        if (mobileCartCount) {
            mobileCartCount.textContent = totalItems;
        }

        // Always show cart links regardless of authentication status
        const desktopCartLink = document.getElementById('cartLink');
        const mobileCartLink = document.getElementById('mobileCartLink');

        if (desktopCartLink) {
            desktopCartLink.style.display = 'inline-flex';
        }
        if (mobileCartLink) {
            mobileCartLink.style.display = 'block';
        }

        // Dispatch event for other components
        document.dispatchEvent(new Event('cartCountUpdated'));
    } catch (error) {
        console.error('Error updating cart count:', error);
    }
}

// Make updateCartCount available globally
window.updateCartCount = updateCartCount;

// Get cart items from the API
async function getCart() {
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

        return await response.json();
    } catch (error) {
        console.error('Error fetching cart:', error);
        return [];
    }
}

// Confirm and remove item from cart
async function confirmRemoveFromCart(itemId, productName) {
    if (confirm(`Are you sure you want to remove ${productName} from your cart?`)) {
        await removeFromCart(itemId);
    }
}

// Remove item from cart
async function removeFromCart(itemId) {
    try {
        const response = await fetch(`/api/cart/${itemId}`, {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (response.ok) {
            await loadCartItems();
            await updateCartCount();
        }
    } catch (error) {
        console.error('Error removing item:', error);
    }
}

// Convert guest cart to database cart
async function convertGuestCartToDatabaseCart() {
    try {
        const response = await fetch('/api/cart/convert-guest-cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to convert cart');
        }

        // Refresh cart display
        await loadCartItems();
        await updateCartCount();
        return true;
    } catch (error) {
        console.error('Error converting cart:', error);
        throw error;
    }
}

// Make convertGuestCartToDatabaseCart available globally
window.convertGuestCartToDatabaseCart = convertGuestCartToDatabaseCart;

// Place order
async function placeOrder(orderDetails) {
    try {
        const response = await fetch('/api/orders/place', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify(orderDetails)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to place order');
        }

        const result = await response.json();
        
        // Clear cart and update UI
        await loadCartItems();
        await updateCartCount();

        // Show success message
        alert('Order placed successfully! Order ID: ' + result.orderId);
        
        // Redirect to order confirmation page
        window.location.href = `/order-confirmation/${result.orderId}`;
        
        return true;
    } catch (error) {
        console.error('Error placing order:', error);
        alert(error.message || 'Failed to place order. Please try again.');
        return false;
    }
}

// Make placeOrder available globally
window.placeOrder = placeOrder;

// View guest order details
async function viewGuestOrder(orderId, email, phone) {
    try {
        // Build query parameters
        const params = new URLSearchParams();
        if (email) params.append('email', email);
        if (phone) params.append('phone', phone);

        const response = await fetch(`/api/orders/${orderId}?${params.toString()}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to fetch order details');
        }

        const orderDetails = await response.json();
        return orderDetails;
    } catch (error) {
        console.error('Error viewing guest order:', error);
        throw error;
    }
}

// Make viewGuestOrder available globally
window.viewGuestOrder = viewGuestOrder;

// Clear entire cart
async function clearCart() {
    try {
        const response = await fetch('/api/cart/clear', {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (response.ok) {
            console.log('Cart cleared successfully');
            
            // Clear local cache
            cartItemsCache = [];
            
            // Update UI
            updateCartUI([]);
            await updateCartCount();
            
            // Clear localStorage cart data if it exists
            if (localStorage.getItem('guestCart')) {
                localStorage.removeItem('guestCart');
            }
            
            return true;
        } else {
            console.error('Failed to clear cart');
            return false;
        }
    } catch (error) {
        console.error('Error clearing cart:', error);
        return false;
    }
}

// Make clearCart available globally
window.clearCart = clearCart; 