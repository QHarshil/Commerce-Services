// Commerce Services Frontend JavaScript
// Handles real API interactions and UI updates

const API_BASE_URL = 'http://localhost:8081'; // Inventory service
const CHECKOUT_API_URL = 'http://localhost:8084'; // Checkout service

// Global state
let currentProducts = [];

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    loadProducts();
    checkHealth();
    // Auto-refresh every 30 seconds to show real-time updates
    setInterval(() => {
        loadProducts();
        checkHealth();
    }, 30000);
});

// Load and display real products from inventory service
async function loadProducts() {
    const productsContainer = document.getElementById('products-list');
    productsContainer.innerHTML = '<div class="loading">Loading products from inventory service...</div>';
    
    try {
        const response = await fetch(`${API_BASE_URL}/api/v1/inventory/products`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const products = await response.json();
        currentProducts = products;
        
        displayProducts(products);
        populateProductSelect(products);
        
    } catch (error) {
        console.error('Error loading products:', error);
        productsContainer.innerHTML = `<div class="loading">❌ Error loading products: ${error.message}</div>`;
        
        // Fallback to sample data if API is not available
        setTimeout(() => {
            console.log('Falling back to sample data...');
            loadSampleProducts();
        }, 2000);
    }
}

// Fallback sample products
function loadSampleProducts() {
    const sampleProducts = [
        {
            productId: 'a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6',
            sku: 'LAPTOP-001',
            productName: 'Gaming Laptop Pro',
            quantity: 15,
            reservedQuantity: 2,
            availableQuantity: 13
        },
        {
            productId: 'b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7',
            sku: 'PHONE-001',
            productName: 'Smartphone X',
            quantity: 32,
            reservedQuantity: 5,
            availableQuantity: 27
        },
        {
            productId: 'c3d4e5f6-g7h8-i9j0-k1l2-m3n4o5p6q7r8',
            sku: 'BOOK-001',
            productName: 'Clean Code',
            quantity: 8,
            reservedQuantity: 1,
            availableQuantity: 7
        }
    ];
    
    currentProducts = sampleProducts;
    displayProducts(sampleProducts);
    populateProductSelect(sampleProducts);
}

// Display products in the UI
function displayProducts(products) {
    const productsContainer = document.getElementById('products-list');
    
    if (!products || products.length === 0) {
        productsContainer.innerHTML = '<div class="loading">No products available</div>';
        return;
    }
    
    const productsHTML = products.map(product => {
        const stockStatus = product.availableQuantity < 10 ? 'low-stock' : 'in-stock';
        const stockColor = product.availableQuantity < 10 ? '#e74c3c' : '#27ae60';
        
        return `
            <div class="product-card">
                <div class="product-name">${product.productName}</div>
                <div class="product-sku">SKU: ${product.sku}</div>
                <div class="product-stock" style="color: ${stockColor}">
                    Available: ${product.availableQuantity}
                    ${product.reservedQuantity > 0 ? `(${product.reservedQuantity} reserved)` : ''}
                </div>
                <div class="product-total">Total: ${product.quantity}</div>
            </div>
        `;
    }).join('');
    
    productsContainer.innerHTML = productsHTML;
}

// Populate product select dropdown
function populateProductSelect(products) {
    const select = document.getElementById('product-select');
    
    // Clear existing options except the first one
    while (select.children.length > 1) {
        select.removeChild(select.lastChild);
    }
    
    products.forEach(product => {
        const option = document.createElement('option');
        option.value = product.productId;
        option.textContent = `${product.productName} (Available: ${product.availableQuantity})`;
        select.appendChild(option);
    });
}

// Process checkout with real API
async function processCheckout() {
    const productId = document.getElementById('product-select').value;
    const quantity = parseInt(document.getElementById('quantity').value);
    const paymentMethod = document.getElementById('payment-method').value;
    const resultDiv = document.getElementById('checkout-result');
    
    if (!productId) {
        showResult(resultDiv, 'Please select a product', 'error');
        return;
    }
    
    if (!quantity || quantity < 1) {
        showResult(resultDiv, 'Please enter a valid quantity', 'error');
        return;
    }
    
    // Check if enough stock is available
    const selectedProduct = currentProducts.find(p => p.productId === productId);
    if (selectedProduct && quantity > selectedProduct.availableQuantity) {
        showResult(resultDiv, `❌ Insufficient stock! Only ${selectedProduct.availableQuantity} available.`, 'error');
        return;
    }
    
    const checkoutRequest = {
        customerId: generateUUID(),
        items: [{
            productId: productId,
            quantity: quantity
        }],
        paymentMethod: paymentMethod,
        idempotencyKey: generateUUID()
    };
    
    showResult(resultDiv, '⏳ Processing checkout via SAGA orchestration...', 'info');
    
    try {
        const startTime = Date.now();
        
        const response = await fetch(`${CHECKOUT_API_URL}/api/v1/checkout/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(checkoutRequest)
        });
        
        const processingTime = Date.now() - startTime;
        const result = await response.json();
        
        if (response.ok && result.success) {
            const resultText = `✅ Checkout Successful!
            
Order ID: ${result.orderId}
Checkout ID: ${result.checkoutId}
Total Amount: $${result.totalAmount}
Processing Time: ${processingTime}ms
Status: ${result.status}

Performance: ${processingTime < 300 ? '🚀 Excellent (< 300ms)' : processingTime < 800 ? '⚡ Good (< 800ms)' : '⏰ Acceptable'}

🔄 Inventory will update automatically...`;
            
            showResult(resultDiv, resultText, 'success');
            
            // Refresh products to show updated inventory
            setTimeout(() => {
                loadProducts();
            }, 1000);
            
        } else {
            const errorText = `❌ Checkout Failed: ${result.errorMessage || 'Unknown error'}
            
Processing Time: ${processingTime}ms
Status: ${result.status || 'FAILED'}`;
            
            showResult(resultDiv, errorText, 'error');
        }
        
    } catch (error) {
        console.error('Checkout error:', error);
        showResult(resultDiv, `❌ Checkout Failed: ${error.message}`, 'error');
    }
}

// Check health of real services
async function checkHealth() {
    const healthContainer = document.getElementById('health-status');
    healthContainer.innerHTML = '<div class="loading">Checking service health...</div>';
    
    // Correct actuator health URLs
    const services = [
        { name: 'Inventory', url: 'http://localhost:8081/actuator/health', port: 8081 },
        { name: 'Orders', url: 'http://localhost:8082/actuator/health', port: 8082 },
        { name: 'Payments', url: 'http://localhost:8083/actuator/health', port: 8083 },
        { name: 'Checkout', url: 'http://localhost:8084/actuator/health', port: 8084 }
    ];

    const healthChecks = services.map(async (service) => {
        const startTime = Date.now();
        try {
            const response = await fetch(service.url, { method: 'GET' });

            if (response.ok) {
                await response.json(); // read data even if unused
                return {
                    ...service,
                    status: 'UP',
                    responseTime: Date.now() - startTime
                };
            } else {
                return { ...service, status: 'DOWN', error: `HTTP ${response.status}` };
            }
        } catch (error) {
            return { ...service, status: 'DOWN', error: error.message };
        }
    });

    try {
        const results = await Promise.allSettled(healthChecks);

        const healthHTML = results.map((result, index) => {
            const service = services[index];
            const status = result.status === 'fulfilled' ? result.value.status : 'DOWN';
            const error = result.status === 'rejected'
                ? result.reason.message
                : (result.value && result.value.error) ? result.value.error : '';

            return `
                <div class="health-item health-${status.toLowerCase()}">
                    <div>${service.name}</div>
                    <div>:${service.port}</div>
                    <div>${status}</div>
                    ${error ? `<div class="error-text">${error}</div>` : ''}
                </div>
            `;
        }).join('');

        healthContainer.innerHTML = healthHTML;

    } catch (error) {
        console.error('Health check error:', error);
        healthContainer.innerHTML = '<div class="loading">❌ Health check failed</div>';
    }
}

// Test real API endpoints
async function testEndpoint() {
    const endpoint = document.getElementById('api-endpoint').value;
    const resultDiv = document.getElementById('api-result');
    
    if (!endpoint) {
        showResult(resultDiv, 'Please select an endpoint', 'error');
        return;
    }
    
    const [method, path] = endpoint.split('|');
    
    showResult(resultDiv, `Testing ${method} ${path}...`, 'info');
    
    try {
        let url;
        let options = { method };
        
        switch (path) {
            case '/api/v1/inventory/products':
                url = `${API_BASE_URL}${path}`;
                break;
            case '/api/v1/inventory/value':
                url = `${API_BASE_URL}${path}`;
                break;
            case '/api/v1/inventory/low-stock':
                url = `${API_BASE_URL}${path}`;
                break;
            case '/api/v1/checkout/process':
                showResult(resultDiv, 'Use the checkout form above for interactive testing', 'info');
                return;
            default:
                throw new Error('Endpoint not configured');
        }
        
        const startTime = Date.now();
        const response = await fetch(url, options);
        const responseTime = Date.now() - startTime;
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        
        const resultText = `${method} ${path}

Response Time: ${responseTime}ms
Status: ${response.status} ${response.statusText}

Response:
${JSON.stringify(data, null, 2)}`;
        
        showResult(resultDiv, resultText, 'success');
        
    } catch (error) {
        showResult(resultDiv, `Error: ${error.message}`, 'error');
    }
}

// Utility function to show results
function showResult(container, text, type) {
    container.className = `result-section result-${type}`;
    container.textContent = text;
}

// Generate UUID for demo purposes
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// Add some interactive features
document.addEventListener('keydown', function(e) {
    // Enter key in quantity field triggers checkout
    if (e.target.id === 'quantity' && e.key === 'Enter') {
        processCheckout();
    }
});

console.log('🛒 Commerce Services Frontend Loaded');
console.log('🔗 Connected to real APIs:');
console.log(`   Inventory: ${API_BASE_URL}`);
console.log(`   Checkout: ${CHECKOUT_API_URL}`);
console.log('Ready to demonstrate real distributed transactions!');
