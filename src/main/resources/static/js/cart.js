// Initialize cart items cache
let cartItemsCache = [];

// Make addToCart available globally
window.addToCart = async function(productId, quantity = 1, price = null) {
    try {
        const requestBody = { productId, quantity, price };
        console.log('Sending request to /api/cart/add with body:', requestBody);

        const response = await fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify(requestBody)
        });

        console.log('Response status:', response.status);
        const responseText = await response.text();
        console.log('Response text:', responseText);

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
        checkoutBtn.addEventListener('click', handleCheckout);
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
    } catch (error) {
        console.error('Error loading cart items:', error);
        showError('Failed to load cart items. Please try again.');
    }
}

// Update cart UI
function updateCartUI(cartItems) {
    const cartItemsContainer = document.getElementById('cartItems');
    const emptyCartDiv = document.getElementById('emptyCart');
    const cartSummaryDiv = document.getElementById('cartSummary');
    const subtotalElement = document.getElementById('subtotal');
    const shippingElement = document.getElementById('shipping');
    const totalElement = document.getElementById('total');

    if (!cartItemsContainer || !emptyCartDiv || !cartSummaryDiv || !subtotalElement || !shippingElement || !totalElement) {
        return;
    }

    if (cartItems.length === 0) {
        cartItemsContainer.innerHTML = '';
        emptyCartDiv.classList.remove('hidden');
        cartSummaryDiv.classList.add('hidden');
        subtotalElement.textContent = '$0.00';
        shippingElement.textContent = '$0.00';
        totalElement.textContent = '$0.00';
        return;
    }

    emptyCartDiv.classList.add('hidden');
    cartSummaryDiv.classList.remove('hidden');
    cartItemsContainer.innerHTML = cartItems.map(item => `
        <div class="bg-white rounded-lg shadow-md p-4 flex items-center space-x-4" data-item-id="${item.id}">
            <img src="${item.product.imageUrl}" alt="${item.product.name}" class="w-24 h-24 object-cover rounded-lg">
            <div class="flex-1">
                <h3 class="font-semibold">${item.product.name}</h3>
                <div class="flex justify-between items-center">
                    <p class="text-gray-600">$${item.price.toFixed(2)}</p>
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
    `).join('');

    // Update totals
    const subtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const shipping = subtotal > 0 ? 10 : 0; // Example shipping cost
    const total = subtotal + shipping;

    subtotalElement.textContent = `$${subtotal.toFixed(2)}`;
    shippingElement.textContent = `$${shipping.toFixed(2)}`;
    totalElement.textContent = `$${total.toFixed(2)}`;
}

// Update item quantity
async function updateQuantity(itemId, newQuantity) {
    try {
        const response = await fetch(`/api/cart/${itemId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify({ quantity: newQuantity })
        });

        if (!response.ok) {
            throw new Error('Failed to update quantity');
        }

        // Update local cache
        const itemIndex = cartItemsCache.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
            cartItemsCache[itemIndex].quantity = newQuantity;
            updateCartUI(cartItemsCache);
            await updateCartCount();
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
        alert('Failed to update quantity. Please try again.');
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

// Handle checkout
async function handleCheckout() {
    try {
        // Check if user is authenticated
        const response = await fetch('/api/auth/status', {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Failed to check authentication status');
        }

        const data = await response.json();
        console.log('Auth status:', data);

        if (!data.authenticated) {
            // Show login modal
            const loginModal = document.getElementById('loginModal');
            if (loginModal) {
                loginModal.classList.remove('hidden');
            } else {
                window.location.href = '/login';
            }
            return;
        }

        // Proceed to checkout
        window.location.href = '/checkout';
    } catch (error) {
        console.error('Error handling checkout:', error);
        alert('Failed to proceed to checkout. Please try again.');
    }
}

// Show error message
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