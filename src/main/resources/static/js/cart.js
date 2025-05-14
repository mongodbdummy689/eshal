// Cart functionality
let quantity = 1;

// Initialize all functionality when DOM is loaded
document.addEventListener('DOMContentLoaded', async function() {
    // Update cart count when page loads
    await updateCartCount();
    
    // Initialize quantity controls for all items
    document.querySelectorAll('.quantity-decrease').forEach(button => {
        button.addEventListener('click', function() {
            const quantityElement = this.parentElement.querySelector('.quantity-value');
            let quantity = parseInt(quantityElement.textContent);
            if (quantity > 1) {
                quantity--;
                quantityElement.textContent = quantity;
            }
        });
    });

    document.querySelectorAll('.quantity-increase').forEach(button => {
        button.addEventListener('click', function() {
            const quantityElement = this.parentElement.querySelector('.quantity-value');
            let quantity = parseInt(quantityElement.textContent);
            quantity++;
            quantityElement.textContent = quantity;
        });
    });

    // Add to cart functionality for all items
    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', async () => {
            try {
                console.log('Checking authentication status...'); // Debug log
                const isAuthenticated = await checkAuthStatus();
                console.log('Is authenticated:', isAuthenticated); // Debug log

                if (isAuthenticated === true) {
                    // Get product data from card's data attributes
                    const card = button.closest('.card');
                    const productId = card.dataset.productId;
                    const price = parseFloat(card.dataset.price);
                    const quantity = parseInt(card.querySelector('.quantity-value').textContent);
                    
                    if (!productId || isNaN(price)) {
                        throw new Error('Product information is missing or invalid');
                    }

                    const cartData = {
                        productId: productId,
                        quantity: quantity,
                        price: price
                    };

                    console.log('Adding to cart:', cartData); // Debug log
                    const success = await addToCart(cartData.productId, cartData.quantity, cartData.price);
                    if (success) {
                        alert('Item added to cart successfully!');
                    }
                } else {
                    console.log('User not authenticated, showing login modal'); // Debug log
                    // User is not logged in, show login modal
                    const loginModal = document.getElementById('loginModal');
                    if (loginModal) {
                        showModal(loginModal);
                    }
                }
            } catch (error) {
                console.error('Error:', error);
                alert(error.message || 'An error occurred. Please try again.');
            }
        });
    });

    // Initialize modal elements
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    const closeModal = document.getElementById('closeModal');
    const closeRegisterModal = document.getElementById('closeRegisterModal');
    const showRegister = document.getElementById('showRegister');
    const showLogin = document.getElementById('showLogin');

    // Initialize cart UI elements
    const cartBtn = document.getElementById('cartBtn');
    const cartCount = document.getElementById('cartCount');

    // Modal controls
    function showModal(modal) {
        if (modal) {
            modal.classList.remove('hidden');
            modal.classList.add('flex');
        }
    }

    function hideModal(modal) {
        if (modal) {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
        }
    }

    // Close modals
    if (closeModal && loginModal) {
        closeModal.addEventListener('click', () => hideModal(loginModal));
    }

    if (closeRegisterModal && registerModal) {
        closeRegisterModal.addEventListener('click', () => hideModal(registerModal));
    }

    // Switch between login and register
    if (showRegister && loginModal && registerModal) {
        showRegister.addEventListener('click', (e) => {
            e.preventDefault();
            hideModal(loginModal);
            showModal(registerModal);
        });
    }

    if (showLogin && loginModal && registerModal) {
        showLogin.addEventListener('click', (e) => {
            e.preventDefault();
            hideModal(registerModal);
            showModal(loginModal);
        });
    }

    // Cart button functionality
    if (cartBtn) {
        cartBtn.addEventListener('click', function() {
            window.location.href = '/cart';
        });
    }

    // Update cart count if element exists
    if (cartCount) {
        updateCartCount();
    }
});

// Check authentication status
async function checkAuthStatus() {
    try {
        console.log('Making auth check request...'); // Debug log
        const response = await fetch('/api/auth/check', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        console.log('Auth check response status:', response.status); // Debug log
        const data = await response.json();
        console.log('Complete auth check response data:', data); // Debug log
        
        if (!data || typeof data.authenticated !== 'boolean') {
            console.error('Invalid auth response format:', data);
            return false;
        }

        // If authenticated, fetch user data to check role
        if (data.authenticated) {
            try {
                const userResponse = await fetch('/api/auth/user', {
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    credentials: 'include'
                });
                
                if (userResponse.ok) {
                    const userData = await userResponse.json();
                    console.log('User data:', userData); // Debug log
                    
                    // Check for admin role and show/hide admin dashboard link
                    if (userData && userData.roles && userData.roles.includes('ROLE_ADMIN')) {
                        console.log('User is admin, showing admin dashboard link');
                        const adminDashboardLink = document.getElementById('adminDashboardLink');
                        const mobileAdminDashboardLink = document.getElementById('mobileAdminDashboardLink');
                        
                        if (adminDashboardLink) {
                            console.log('Found admin dashboard link, showing it');
                            adminDashboardLink.classList.remove('hidden');
                        }
                        if (mobileAdminDashboardLink) {
                            console.log('Found mobile admin dashboard link, showing it');
                            mobileAdminDashboardLink.classList.remove('hidden');
                        }
                    }
                }
            } catch (error) {
                console.error('Error fetching user data:', error);
            }
        }
        
        return data.authenticated;
    } catch (error) {
        console.error('Error checking auth:', error);
        return false;
    }
}

// Update cart count in the UI
async function updateCartCount() {
    try {
        const isAuthenticated = await checkAuthStatus();
        if (!isAuthenticated) {
            console.log('User not authenticated, skipping cart update');
            return;
        }

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

        // Update cart link visibility
        const desktopCartLink = document.getElementById('cartLink');
        const mobileCartLink = document.getElementById('mobileCartLink');

        if (desktopCartLink) {
            desktopCartLink.style.display = totalItems > 0 ? 'inline-flex' : 'none';
        }
        if (mobileCartLink) {
            mobileCartLink.style.display = totalItems > 0 ? 'block' : 'none';
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
        const isAuthenticated = await checkAuthStatus();
        if (!isAuthenticated) {
            console.log('User not authenticated, skipping cart fetch');
            return [];
        }

        const response = await fetch('/api/cart', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            if (response.status === 401) {
                console.log('User not authenticated, returning empty cart');
                return [];
            }
            throw new Error('Failed to fetch cart');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching cart:', error);
        return [];
    }
}

// Add item to cart
async function addToCart(productId, quantity = 1, price = null) {
    try {
        const isAuthenticated = await checkAuthStatus();
        if (!isAuthenticated) {
            console.log('User not authenticated, redirecting to login');
            // Show login modal
            const loginModal = document.getElementById('loginModal');
            if (loginModal) {
                loginModal.classList.remove('hidden');
                loginModal.classList.add('flex');
            }
            return false;
        }

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
}

// Confirm and remove item from cart
async function confirmRemoveFromCart(itemId, productName) {
    if (confirm(`Are you sure you want to remove ${productName} from your cart?`)) {
        await removeFromCart(itemId);
    }
}

// Update item quantity
async function updateQuantity(itemId, change) {
    try {
        const cartItems = await getCart();
        const item = cartItems.find(item => item.id === itemId);
        
        if (!item) {
            console.error('Item not found in cart');
            return;
        }

        const newQuantity = item.quantity + change;
        
        if (newQuantity < 1) {
            await confirmRemoveFromCart(itemId, item.product?.name || 'this item');
            return;
        }

        const response = await fetch(`/api/cart/${itemId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify({ quantity: newQuantity })
        });

        if (response.ok) {
            await loadCartItems();
            await updateCartCount();
        } else {
            throw new Error('Failed to update quantity');
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
        alert('Failed to update quantity. Please try again.');
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

// Load cart items
async function loadCartItems() {
    try {
        const cartItems = await getCart();
        const cartItemsContainer = document.getElementById('cartItems');
        const cartTotal = document.getElementById('cartTotal');
        let total = 0;

        if (!cartItemsContainer) return;

        if (cartItems.length === 0) {
            cartItemsContainer.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
            if (cartTotal) cartTotal.textContent = '$0.00';
            return;
        }

        cartItemsContainer.innerHTML = cartItems.map(item => {
            total += item.price * item.quantity;
            return `
                <div class="cart-item">
                    <div class="item-details">
                        <h3>${item.productId}</h3>
                        <p>Quantity: ${item.quantity}</p>
                        <p>Price: $${item.price.toFixed(2)}</p>
                    </div>
                    <div class="item-actions">
                        <button onclick="updateQuantity('${item.id}', ${item.quantity - 1})" class="btn-secondary">-</button>
                        <span>${item.quantity}</span>
                        <button onclick="updateQuantity('${item.id}', ${item.quantity + 1})" class="btn-secondary">+</button>
                        <button onclick="removeFromCart('${item.id}')" class="btn-danger">Remove</button>
                    </div>
                </div>
            `;
        }).join('');

        if (cartTotal) cartTotal.textContent = `$${total.toFixed(2)}`;
    } catch (error) {
        console.error('Error loading cart items:', error);
    }
}

// Initialize cart functionality
document.addEventListener('DOMContentLoaded', async function() {
    await updateCartCount();
    await loadCartItems();
}); 