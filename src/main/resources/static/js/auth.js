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

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Login failed');
        }

        return data;
    } catch (error) {
        console.error('Login error:', error);
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
        
        window.location.reload();
    } catch (error) {
        console.error('Logout error:', error);
        throw error;
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
        return await response.json();
    } catch (error) {
        console.error('Get current user error:', error);
        return null;
    }
}

async function register(fullName, email, password) {
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'include',
            body: JSON.stringify({ fullName, email, password })
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
    
    if (loginRegisterLink && logoutLink) {
        if (isAuthenticated) {
            loginRegisterLink.classList.add('hidden');
            logoutLink.classList.remove('hidden');
            if (cartLink) cartLink.classList.remove('hidden');
        } else {
            loginRegisterLink.classList.remove('hidden');
            logoutLink.classList.add('hidden');
            if (cartLink) cartLink.classList.add('hidden');
        }
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
        });
    }

    // Close modals when clicking outside
    [loginModal, registerModal].forEach(modal => {
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    hideModal(modal);
                }
            });
        }
    });

    // Close modals when pressing Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (loginModal && !loginModal.classList.contains('hidden')) {
                hideModal(loginModal);
            }
            if (registerModal && !registerModal.classList.contains('hidden')) {
                hideModal(registerModal);
            }
        }
    });

    // Register button handler
    if (registerBtn) {
        registerBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('fullName').value;
            const email = document.getElementById('registerEmail').value;
            const password = document.getElementById('registerPassword').value;

            try {
                await register(fullName, email, password);
                // Registration successful
                hideModal(registerModal);
                showModal(loginModal);
                // Clear the registration form
                document.getElementById('fullName').value = '';
                document.getElementById('registerEmail').value = '';
                document.getElementById('registerPassword').value = '';
            } catch (error) {
                const errorElement = document.createElement('div');
                errorElement.className = 'bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4';
                errorElement.textContent = error.message || 'An error occurred during registration. Please try again.';
                const registerForm = registerBtn.closest('.space-y-4');
                registerForm.insertBefore(errorElement, registerBtn);
                setTimeout(() => errorElement.remove(), 5000);
            }
        });
    }
}); 