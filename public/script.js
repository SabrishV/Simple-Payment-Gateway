// Store user data (In a real application, this would be handled by a backend)
let currentUser = null;
const users = [];
const products = [
    { name: 'Laptop', price: 1000.0 },
    { name: 'Smartphone', price: 500.0 },
    { name: 'Subscription', price: 100.0 }
];

// UI Elements
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const dashboard = document.getElementById('dashboard');
const userWelcome = document.getElementById('userWelcome');
const productsGrid = document.getElementById('productsGrid');
const transactionsList = document.getElementById('transactionsList');

// Show/Hide Functions
function showLogin() {
    loginForm.classList.remove('hidden');
    registerForm.classList.add('hidden');
    dashboard.classList.add('hidden');
}

function showRegister() {
    loginForm.classList.add('hidden');
    registerForm.classList.remove('hidden');
    dashboard.classList.add('hidden');
}

function showDashboard() {
    loginForm.classList.add('hidden');
    registerForm.classList.add('hidden');
    dashboard.classList.remove('hidden');
    updateDashboard();
}

// Handle Login
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch('http://localhost:8080/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();
        if (data.success) {
            currentUser = data.user;
            showDashboard();
        } else {
            alert(data.message || 'Login failed');
        }
    } catch (error) {
        alert('Error during login. Please try again.');
    }
    return false;
}

// Handle Register
async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        alert('Passwords do not match');
        return false;
    }

    try {
        const response = await fetch('http://localhost:8080/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();
        if (data.success) {
            alert('Registration successful! Please login.');
            // Clear the registration form
            document.getElementById('registerUsername').value = '';
            document.getElementById('registerPassword').value = '';
            document.getElementById('confirmPassword').value = '';
            // Show login form instead of dashboard
            showLogin();
        } else {
            alert(data.message || 'Registration failed');
        }
    } catch (error) {
        alert('Error during registration. Please try again.');
    }
    return false;
}

// Handle Logout
function handleLogout() {
    currentUser = null;
    showLogin();
}

// Update Payment Method
async function updatePaymentMethod() {
    const paymentMethod = document.getElementById('paymentMethod').value;
    if (!currentUser) return;
    
    if (!paymentMethod) {
        alert('Please select a payment method');
        return;
    }

    try {
        // Collect payment details based on the selected method
        let paymentDetails = {};
        
        if (paymentMethod === 'Credit Card') {
            const cardNumber = prompt('Enter your card number (16 digits):');
            if (!cardNumber || !cardNumber.replace(/\s/g, '').match(/^\d{16}$/)) {
                alert('Invalid card number. Please enter a 16-digit number.');
                return;
            }
            
            const cardExpiry = prompt('Enter card expiry date (MM/YY):');
            if (!cardExpiry || !cardExpiry.match(/^\d{2}\/\d{2}$/)) {
                alert('Invalid expiry date. Please use MM/YY format.');
                return;
            }
            
            const cardCVV = prompt('Enter CVV (3 digits):');
            if (!cardCVV || !cardCVV.match(/^\d{3}$/)) {
                alert('Invalid CVV. Please enter a 3-digit number.');
                return;
            }
            
            paymentDetails = { cardNumber, cardExpiry, cardCVV };
        } 
        else if (paymentMethod === 'Bank Account') {
            const bankAccount = prompt('Enter your bank account number (8-12 digits):');
            if (!bankAccount || !bankAccount.replace(/\s/g, '').match(/^\d{8,12}$/)) {
                alert('Invalid bank account number. Please enter 8-12 digits.');
                return;
            }
            
            paymentDetails = { bankAccount };
        } 
        else if (paymentMethod === 'UPI') {
            const upiId = prompt('Enter your UPI ID (username@provider):');
            if (!upiId || !upiId.match(/^[a-zA-Z0-9.]+@[a-zA-Z0-9]+$/)) {
                alert('Invalid UPI ID. Please use format: username@provider');
                return;
            }
            
            paymentDetails = { upiId };
        }
        
        // Send payment method update to server
        const response = await fetch('http://localhost:8080/api/payment-method', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: currentUser.username,
                password: prompt('Enter your password to confirm:'),
                paymentMethod: paymentMethod,
                ...paymentDetails
            })
        });

        const data = await response.json();
        if (data.success) {
            // Store payment method in current user
            currentUser.paymentMethod = paymentMethod;
            // Store masked payment details if available
            if (paymentDetails.cardNumber) currentUser.cardNumber = 'xxxx-xxxx-xxxx-' + paymentDetails.cardNumber.slice(-4);
            if (paymentDetails.bankAccount) currentUser.bankAccount = 'xxxxxxxx' + paymentDetails.bankAccount.slice(-4);
            if (paymentDetails.upiId) currentUser.upiId = paymentDetails.upiId;
            
            alert('Payment method updated successfully');
            updateDashboard();
        } else {
            alert(data.message || 'Failed to update payment method');
        }
    } catch (error) {
        console.error('Error updating payment method:', error);
        alert('Error updating payment method. Please try again.');
    }
}

// Load Products
async function loadProducts() {
    try {
        const response = await fetch('http://localhost:8080/api/products');
        const data = await response.json();
        if (data.success) {
            return data.products;
        }
        return [];
    } catch (error) {
        console.error('Error loading products:', error);
        return [];
    }
}

// Update Dashboard
async function updateDashboard() {
    if (!currentUser) return;

    // Update welcome message
    userWelcome.textContent = currentUser.username;

    // Update payment method select
    document.getElementById('paymentMethod').value = currentUser.paymentMethod || '';

    // Update payment details display
    const paymentDetailsDiv = document.getElementById('paymentDetails');
    if (currentUser.paymentMethod) {
        let detailsHTML = `<h4>${currentUser.paymentMethod} Details</h4>`;
        
        if (currentUser.paymentMethod === 'Credit Card' && currentUser.cardNumber) {
            detailsHTML += `
                <div class="payment-detail-item">
                    <span class="payment-detail-label">Card Number:</span>
                    <span>${currentUser.cardNumber}</span>
                </div>
                <div class="payment-detail-item">
                    <span class="payment-detail-label">Expiry:</span>
                    <span>${currentUser.cardExpiry}</span>
                </div>
            `;
        } else if (currentUser.paymentMethod === 'Bank Account' && currentUser.bankAccount) {
            detailsHTML += `
                <div class="payment-detail-item">
                    <span class="payment-detail-label">Account Number:</span>
                    <span>${currentUser.bankAccount}</span>
                </div>
            `;
        } else if (currentUser.paymentMethod === 'UPI' && currentUser.upiId) {
            detailsHTML += `
                <div class="payment-detail-item">
                    <span class="payment-detail-label">UPI ID:</span>
                    <span>${currentUser.upiId}</span>
                </div>
            `;
        } else if (currentUser.paymentMethod === 'PayPal') {
            detailsHTML += `
                <div class="payment-detail-item">
                    <span class="payment-detail-label">PayPal:</span>
                    <span>Connected</span>
                </div>
            `;
        }
        
        paymentDetailsDiv.innerHTML = detailsHTML;
        paymentDetailsDiv.classList.remove('hidden');
    } else {
        paymentDetailsDiv.classList.add('hidden');
    }

    // Load and update products grid
    const products = await loadProducts();
    productsGrid.innerHTML = products.map(product => `
        <div class="product-card">
            <h4>${product.name}</h4>
            <div class="price">$${product.price}</div>
            <button class="btn" onclick='handlePurchase(${JSON.stringify(product)})'>
                Purchase
            </button>
        </div>
    `).join('');

    // Update transactions list (if available)
    if (currentUser.transactions) {
        transactionsList.innerHTML = currentUser.transactions.length === 0 
            ? '<div class="transaction-item">No transactions yet</div>'
            : currentUser.transactions.map(transaction => `
                <div class="transaction-item">
                    <div>
                        ${transaction.product.name} - $${transaction.product.price}
                    </div>
                    <div class="${transaction.successful ? 'success' : 'failed'}">
                        ${transaction.successful ? 'Successful' : 'Failed'}
                    </div>
                </div>
            `).join('');
    } else {
        transactionsList.innerHTML = '<div class="transaction-item">No transactions yet</div>';
    }
}

// Handle Purchase
async function handlePurchase(product) {
    if (!currentUser.paymentMethod) {
        alert('Please select a payment method first');
        return;
    }

    // Prompt for password
    const password = prompt("Please enter your password to confirm the purchase:");
    if (!password) {
        alert('Password is required to proceed with the purchase.');
        return;
    }

    try {
        console.log('Sending purchase request:', {
            username: currentUser.username,
            password: password, // Use the password entered by the user
            productName: product.name,
            productPrice: product.price.toString()
        });

        const response = await fetch('http://localhost:8080/api/payment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: currentUser.username,
                password: password, // Send the password entered by the user
                productName: product.name,
                productPrice: product.price.toString()
            })
        });

        const data = await response.json();
        console.log('Purchase response:', data); // Debug log
        
        if (data.success) {
            alert('Purchase successful!');
            // Update current user data from response
            if (data.user) {
                currentUser = {
                    ...currentUser,
                    ...data.user,
                    transactions: currentUser.transactions || []
                };
            }
            // Add the new transaction
            currentUser.transactions.push({
                product: product,
                successful: true
            });
        } else {
            alert(`Purchase failed: ${data.message}`);
            currentUser.transactions = currentUser.transactions || [];
            currentUser.transactions.push({
                product: product,
                successful: false
            });
        }

        // Refresh the dashboard
        await updateDashboard();
    } catch (error) {
        console.error('Purchase error:', error);
        alert('Error processing payment. Please try again.');
    }
}

// Initialize the application
showLogin(); 