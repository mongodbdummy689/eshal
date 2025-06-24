// Authentication functions
async function login(email, password) {
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify({ email, password })
        });

        console.log('Login response status:', response.status);
        console.log('Login response headers:', response.headers);

        // Check if response has content
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            console.error('Non-JSON response received:', contentType);
            throw new Error('Server returned an invalid response format');
        }

        const data = await response.json();
        console.log('Login response data:', data);

        if (!response.ok) {
            throw new Error(data.message || data.error || `Login failed with status ${response.status}`);
        }

        return data;
    } catch (error) {
        console.error('Login error:', error);
        if (error.name === 'SyntaxError') {
            throw new Error('Server returned an invalid response. Please try again.');
        }
        throw error;
    }
}

async function logout() {
    try {
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });
        
        if (!response.ok) {
            throw new Error('Logout failed');
        }
        
        // Try to parse JSON response first
        try {
            const data = await response.json();
            if (data.success && data.redirect) {
                window.location.href = data.redirect;
            } else {
                window.location.href = '/';
            }
        } catch (e) {
            // If not JSON, just redirect to home
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Logout error:', error);
        // Even if there's an error, redirect to home to clear the session
        window.location.href = '/';
    }
}

async function getCurrentUser() {
    try {
        const response = await fetch('/api/auth/user', {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });
        if (!response.ok) {
            if (response.status === 401) {
                return null;
            }
            throw new Error('Failed to get current user');
        }
        const user = await response.json();
        console.log('Current user data:', user); // Debug log
        return user;
    } catch (error) {
        console.error('Get current user error:', error);
        return null;
    }
}

// Add new function to check if user is admin
async function isAdmin() {
    try {
        const user = await getCurrentUser();
        console.log('Checking admin status for user:', user); // Debug log
        const isAdminUser = user && user.role === 'ADMIN';
        console.log('Is admin?', isAdminUser); // Debug log
        return isAdminUser;
    } catch (error) {
        console.error('Error checking admin status:', error);
        return false;
    }
}

async function register(userData) {
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify(userData)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Registration failed');
        }

        return data;
    } catch (error) {
        console.error('Registration error:', error);
        throw error;
    }
}

// Login form handler
async function handleLogin(event) {
    event.preventDefault();
    
    const errorElement = document.getElementById('loginError');
    if (errorElement) {
        errorElement.classList.add('hidden');
    }
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    try {
        await login(email, password);
        
        // Login successful
        const loginForm = document.getElementById('loginForm');
        const loginModal = document.getElementById('loginModal');
        
        if (loginForm) {
            loginForm.reset();
        }
        
        if (loginModal) {
            hideModal(loginModal);
        }
        
        await checkAuthStatus();
        window.location.reload();
    } catch (error) {
        if (errorElement) {
            errorElement.textContent = error.message || 'An error occurred. Please try again.';
            errorElement.classList.remove('hidden');
        }
    }
    
    return false;
}

// Check authentication status
async function checkAuthStatus() {
    try {
        const response = await fetch('/api/auth/check', {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            return false;
        }

        const data = await response.json();
        console.log('Auth check data:', data); // Debug log
        
        // If we have user data, check for admin role
        if (data.user && data.user.role === 'ADMIN') {
            console.log('User is admin, showing admin dropdown');
            const adminDropdown = document.getElementById('adminDropdown');
            const mobileAdminMenu = document.getElementById('mobileAdminMenu');
            if (adminDropdown) {
                console.log('Found admin dropdown, showing it');
                adminDropdown.classList.remove('hidden');
            }
            if (mobileAdminMenu) {
                console.log('Found mobile admin menu, showing it');
                mobileAdminMenu.classList.remove('hidden');
            }
        }
        
        updateAuthUI(data.authenticated);
        return data.authenticated;
    } catch (error) {
        console.error('Error checking auth:', error);
        return false;
    }
}

// Update UI based on authentication status
function updateAuthUI(isAuthenticated) {
    const loginRegisterLink = document.getElementById('loginRegisterLink');
    const logoutLink = document.getElementById('logoutLink');
    const cartLink = document.getElementById('cartLink');
    const adminDropdown = document.getElementById('adminDropdown');
    const mobileAdminMenu = document.getElementById('mobileAdminMenu');
    
    if (loginRegisterLink && logoutLink) {
        if (isAuthenticated) {
            loginRegisterLink.classList.add('hidden');
            logoutLink.classList.remove('hidden');
        } else {
            loginRegisterLink.classList.remove('hidden');
            logoutLink.classList.add('hidden');
            if (adminDropdown) adminDropdown.classList.add('hidden');
            if (mobileAdminMenu) mobileAdminMenu.classList.add('hidden');
        }
    }

    // Always show cart link regardless of authentication status
    if (cartLink) {
        cartLink.classList.remove('hidden');
    }
}

// Modal functions
function showModal(modal) {
    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function hideModal(modal) {
    if (modal) {
        modal.classList.remove('flex');
        modal.classList.add('hidden');
    }
}

// Add this function after the hideModal function
function clearMessages(modal) {
    if (modal) {
        const messages = modal.querySelectorAll('.bg-green-100, .bg-red-100');
        messages.forEach(msg => msg.remove());
    }
}

// Initialize auth functionality
document.addEventListener('DOMContentLoaded', async function() {
    await checkAuthStatus();

    // Add modal event listeners
    const loginRegisterLink = document.getElementById('loginRegisterLink');
    const showLoginLink = document.getElementById('showLogin');
    const showRegisterLink = document.getElementById('showRegister');
    const closeLoginModal = document.getElementById('closeLoginModal');
    const closeRegisterModal = document.getElementById('closeRegisterModal');
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    const logoutLink = document.getElementById('logoutLink');
    const registerBtn = document.getElementById('registerBtn');

    // Logout handler
    if (logoutLink) {
        logoutLink.addEventListener('click', async (e) => {
            e.preventDefault();
            await logout();
        });
    }

    if (loginRegisterLink) {
        loginRegisterLink.addEventListener('click', (e) => {
            e.preventDefault();
            showModal(loginModal);
        });
    }

    if (showLoginLink) {
        showLoginLink.addEventListener('click', (e) => {
            e.preventDefault();
            hideModal(registerModal);
            showModal(loginModal);
        });
    }

    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', (e) => {
            e.preventDefault();
            hideModal(loginModal);
            showModal(registerModal);
        });
    }

    if (closeLoginModal) {
        closeLoginModal.addEventListener('click', () => {
            hideModal(loginModal);
        });
    }

    if (closeRegisterModal) {
        closeRegisterModal.addEventListener('click', () => {
            hideModal(registerModal);
            clearMessages(registerModal);
        });
    }

    // Close modals when clicking outside
    [loginModal, registerModal].forEach(modal => {
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    hideModal(modal);
                    clearMessages(modal);
                }
            });
        }
    });

    // Close modals when pressing Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (loginModal && !loginModal.classList.contains('hidden')) {
                hideModal(loginModal);
                clearMessages(loginModal);
            }
            if (registerModal && !registerModal.classList.contains('hidden')) {
                hideModal(registerModal);
                clearMessages(registerModal);
            }
        }
    });

    // Register button handler
    if (registerBtn) {
        registerBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            
            // Remove any existing messages
            clearMessages(registerModal);
            
            // Disable register button and show loading state
            registerBtn.disabled = true;
            registerBtn.textContent = 'Registering...';
            
            // Get all form data
            const formData = {
                fullName: document.getElementById('fullName').value,
                email: document.getElementById('registerEmail').value,
                mobileNumber: document.getElementById('mobileNumber').value,
                password: document.getElementById('registerPassword').value,
                flatNo: document.getElementById('flatNo').value,
                apartmentName: document.getElementById('apartmentName').value,
                floor: document.getElementById('floor').value,
                streetName: document.getElementById('streetName').value,
                nearbyLandmark: document.getElementById('nearbyLandmark').value,
                city: document.getElementById('city').value,
                state: document.getElementById('state').value,
                country: document.getElementById('country').value,
                pincode: document.getElementById('pincode').value
            };

            // Validate mobile number format
            if (!/^[0-9]{10}$/.test(formData.mobileNumber)) {
                const errorMessage = document.createElement('div');
                errorMessage.className = 'bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4';
                errorMessage.textContent = 'Please enter a valid 10-digit mobile number';
                
                const formContainer = registerBtn.closest('form');
                formContainer.insertBefore(errorMessage, formContainer.firstChild);
                
                registerBtn.disabled = false;
                registerBtn.textContent = 'Register';
                return;
            }

            // Validate pincode format
            if (!/^[0-9]{6}$/.test(formData.pincode)) {
                const errorMessage = document.createElement('div');
                errorMessage.className = 'bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4';
                errorMessage.textContent = 'Please enter a valid 6-digit pincode';
                
                const formContainer = registerBtn.closest('form');
                formContainer.insertBefore(errorMessage, formContainer.firstChild);
                
                registerBtn.disabled = false;
                registerBtn.textContent = 'Register';
                return;
            }
            
            try {
                await register(formData);
                
                // Show success message
                const successMessage = document.createElement('div');
                successMessage.className = 'bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4';
                successMessage.textContent = 'Successfully registered. Please login.';
                
                // Insert success message before the form
                const formContainer = registerBtn.closest('form');
                formContainer.insertBefore(successMessage, formContainer.firstChild);
                
                // Clear form
                document.getElementById('registerForm').reset();
                
                // Hide register modal and show login modal after a short delay
                setTimeout(() => {
                    hideModal(registerModal);
                    clearMessages(registerModal);
                    showModal(loginModal);
                    // Reset button state
                    registerBtn.disabled = false;
                    registerBtn.textContent = 'Register';
                }, 1500);
                
            } catch (error) {
                // Show error message
                const errorMessage = document.createElement('div');
                errorMessage.className = 'bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4';
                errorMessage.textContent = error.message || 'Registration failed. Please try again.';
                
                // Insert error message before the form
                const formContainer = registerBtn.closest('form');
                formContainer.insertBefore(errorMessage, formContainer.firstChild);
                
                // Re-enable register button
                registerBtn.disabled = false;
                registerBtn.textContent = 'Register';
            }
        });
    }

    // Add form submit handler for registration
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            registerBtn.click(); // Trigger the same logic as the button click
        });
    }
}); 