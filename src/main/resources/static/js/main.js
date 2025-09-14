// Auth Demo - Main JavaScript

// Global variables
let accessToken = null;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Add fade-in animation to main content
    const mainContent = document.querySelector('main');
    if (mainContent) {
        mainContent.classList.add('fade-in');
    }
    
    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 5000);
    });
});

// API Helper Functions
const API = {
    // Base API call function
    call: async function(endpoint, options = {}) {
        const defaultOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        };
        
        // Add Authorization header if token is available and not a public endpoint
        if (accessToken && !endpoint.startsWith('/api/public')) {
            defaultOptions.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        
        try {
            const response = await fetch(endpoint, { ...defaultOptions, ...options });
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            return {
                success: true,
                status: response.status,
                data: data
            };
        } catch (error) {
            return {
                success: false,
                error: error.message
            };
        }
    },
    
    // Specific API endpoints
    public: {
        health: () => API.call('/api/public/health'),
        info: () => API.call('/api/public/info')
    },
    
    user: {
        profile: () => API.call('/api/user/profile'),
        session: () => API.call('/api/user/session'),
        dashboard: () => API.call('/api/user/dashboard'),
        sync: () => API.call('/api/user/sync', { method: 'POST' })
    },
    
    manager: {
        users: () => API.call('/api/manager/users'),
        reports: () => API.call('/api/manager/reports'),
        dashboard: () => API.call('/api/manager/dashboard'),
        assignRole: (userId, role) => API.call(`/api/manager/users/${userId}/roles`, {
            method: 'POST',
            body: JSON.stringify({ role: role })
        }),
        removeRole: (userId, role) => API.call(`/api/manager/users/${userId}/roles`, {
            method: 'DELETE',
            body: JSON.stringify({ role: role })
        })
    },
    
    admin: {
        systemInfo: () => API.call('/api/admin/system/info'),
        roles: () => API.call('/api/admin/roles'),
        dashboard: () => API.call('/api/admin/dashboard'),
        auditSessions: () => API.call('/api/admin/audit/sessions'),
        enableUser: (userId) => API.call(`/api/admin/users/${userId}/enable`, { method: 'POST' }),
        disableUser: (userId) => API.call(`/api/admin/users/${userId}/disable`, { method: 'POST' })
    }
};

// UI Helper Functions
const UI = {
    // Show loading state
    showLoading: function(element, text = 'Loading...') {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) {
            element.innerHTML = `
                <div class="d-flex align-items-center">
                    <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                    <span>${text}</span>
                </div>
            `;
        }
    },
    
    // Show success message
    showSuccess: function(element, data, title = 'Success') {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) {
            element.innerHTML = `
                <div class="alert alert-success py-2 slide-up">
                    <small><strong>✓ ${title}</strong></small>
                    <pre class="mb-0 mt-1" style="font-size: 0.75em; max-height: 120px; overflow-y: auto;">${JSON.stringify(data, null, 2)}</pre>
                </div>
            `;
        }
    },
    
    // Show error message
    showError: function(element, error, title = 'Error') {
        if (typeof element === 'string') {
            element = document.getElementById(element);
        }
        if (element) {
            element.innerHTML = `
                <div class="alert alert-danger py-2 slide-up">
                    <small><strong>✗ ${title}:</strong> ${error}</small>
                </div>
            `;
        }
    },
    
    // Show toast notification
    showToast: function(message, type = 'info') {
        const toastContainer = document.getElementById('toast-container');
        if (!toastContainer) {
            // Create toast container if it doesn't exist
            const container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            container.style.zIndex = '1055';
            document.body.appendChild(container);
        }
        
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="toast-header">
                <i class="fas fa-${type === 'success' ? 'check-circle text-success' : type === 'error' ? 'exclamation-circle text-danger' : 'info-circle text-info'} me-2"></i>
                <strong class="me-auto">Notification</strong>
                <small class="text-muted">now</small>
                <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        `;
        
        document.getElementById('toast-container').appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
        
        // Remove toast element after it's hidden
        toast.addEventListener('hidden.bs.toast', function() {
            toast.remove();
        });
    },
    
    // Copy text to clipboard
    copyToClipboard: async function(text, button = null) {
        try {
            await navigator.clipboard.writeText(text);
            
            if (button) {
                const originalHtml = button.innerHTML;
                const originalClass = button.className;
                
                button.innerHTML = '<i class="fas fa-check"></i> Copied!';
                button.className = button.className.replace(/btn-outline-\w+/, 'btn-success');
                
                setTimeout(() => {
                    button.innerHTML = originalHtml;
                    button.className = originalClass;
                }, 2000);
            }
            
            UI.showToast('Copied to clipboard!', 'success');
            return true;
        } catch (err) {
            console.error('Failed to copy text: ', err);
            UI.showToast('Failed to copy to clipboard', 'error');
            return false;
        }
    }
};

// Global API testing function (used in templates)
window.testApiEndpoint = async function(endpoint, resultId) {
    const resultElement = document.getElementById(resultId);
    if (!resultElement) return;
    
    UI.showLoading(resultElement, 'Testing...');
    
    try {
        const result = await API.call(endpoint);
        
        if (result.success) {
            UI.showSuccess(resultElement, result.data);
        } else {
            UI.showError(resultElement, result.error);
        }
    } catch (error) {
        UI.showError(resultElement, error.message);
    }
};

// Global copy function (used in templates)
window.copyToClipboard = function(elementId, button) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    const text = element.textContent || element.innerText;
    UI.copyToClipboard(text, button);
};

// Set access token for API calls (called from templates)
window.setAccessToken = function(token) {
    accessToken = token;
};

// Utility functions
const Utils = {
    // Format timestamp
    formatTimestamp: function(timestamp) {
        return new Date(timestamp).toLocaleString();
    },
    
    // Format time remaining
    formatTimeRemaining: function(seconds) {
        if (seconds <= 0) return 'Expired';
        
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;
        
        if (hours > 0) {
            return `${hours}h ${minutes}m ${secs}s`;
        } else if (minutes > 0) {
            return `${minutes}m ${secs}s`;
        } else {
            return `${secs}s`;
        }
    },
    
    // Decode JWT token (for display purposes only)
    decodeJWT: function(token) {
        try {
            const parts = token.split('.');
            const header = JSON.parse(atob(parts[0]));
            const payload = JSON.parse(atob(parts[1]));
            
            return {
                header: header,
                payload: payload,
                signature: parts[2]
            };
        } catch (error) {
            console.error('Failed to decode JWT:', error);
            return null;
        }
    },
    
    // Check if token is expired
    isTokenExpired: function(token) {
        const decoded = Utils.decodeJWT(token);
        if (!decoded || !decoded.payload.exp) return true;
        
        const currentTime = Math.floor(Date.now() / 1000);
        return decoded.payload.exp < currentTime;
    }
};

// Export for use in other scripts
window.API = API;
window.UI = UI;
window.Utils = Utils;
