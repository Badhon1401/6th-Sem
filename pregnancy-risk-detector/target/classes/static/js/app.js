// AI Pregnancy Risk Detection System - Frontend JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

// Application initialization
function initializeApp() {
    console.log('🤖 AI Pregnancy Risk Detection System - Frontend Initialized');

    // Load system information
    loadSystemInfo();

    // Load quick stats
    loadQuickStats();

    // Initialize form validation
    initializeFormValidation();

    // Initialize tooltips
    initializeTooltips();

    // Set up real-time form feedback
    setupRealTimeFeedback();

    // Check service status periodically
    setInterval(checkServiceStatus, 30000); // Every 30 seconds
}

// Load system information
async function loadSystemInfo() {
    try {
        const response = await fetch('/api/models');
        const data = await response.json();

        const systemInfoDiv = document.getElementById('systemInfo');
        if (systemInfoDiv) {
            systemInfoDiv.innerHTML = generateSystemInfoHTML(data);
        }
    } catch (error) {
        console.error('Error loading system info:', error);
        const systemInfoDiv = document.getElementById('systemInfo');
        if (systemInfoDiv) {
            systemInfoDiv.innerHTML = `
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    Unable to load system information
                </div>
            `;
        }
    }
}

// Generate system info HTML
function generateSystemInfoHTML(data) {
    const totalModels = data.totalModels || 0;
    const availableModels = data.availableModels || [];

    let html = `
        <div class="fade-in">
            <div class="row g-2 mb-3">
                <div class="col-6 text-center">
                    <div class="bg-primary text-white p-2 rounded">
                        <i class="fas fa-brain fa-lg"></i>
                        <div class="small">AI Models</div>
                        <div class="fw-bold">${totalModels}</div>
                    </div>
                </div>
                <div class="col-6 text-center">
                    <div class="bg-success text-white p-2 rounded">
                        <i class="fas fa-check-circle fa-lg"></i>
                        <div class="small">Status</div>
                        <div class="fw-bold">Ready</div>
                    </div>
                </div>
            </div>
    `;

    if (availableModels.length > 0) {
        html += `
            <div class="mb-3">
                <h6 class="mb-2"><i class="fas fa-list me-2"></i>Available Models:</h6>
                <div class="small">
        `;

        availableModels.forEach(model => {
            const accuracy = data.modelAccuracies && data.modelAccuracies[model]
                ? (data.modelAccuracies[model] * 100).toFixed(1) + '%'
                : 'N/A';
            html += `
                <div class="d-flex justify-content-between mb-1">
                    <span class="badge bg-outline-primary">${formatModelName(model)}</span>
                    <small class="text-muted">${accuracy}</small>
                </div>
            `;
        });

        html += `
                </div>
            </div>
        `;
    }

    html += `
            <div class="text-center">
                <button class="btn btn-sm btn-outline-primary" onclick="loadSystemInfo()">
                    <i class="fas fa-sync-alt me-1"></i>Refresh
                </button>
            </div>
        </div>
    `;

    return html;
}

// Load quick statistics
async function loadQuickStats() {
    try {
        const response = await fetch('/api/stats');
        const data = await response.json();

        const quickStatsDiv = document.getElementById('quickStats');
        if (quickStatsDiv) {
            quickStatsDiv.innerHTML = generateQuickStatsHTML(data);
        }
    } catch (error) {
        console.error('Error loading quick stats:', error);
        const quickStatsDiv = document.getElementById('quickStats');
        if (quickStatsDiv) {
            quickStatsDiv.innerHTML = `
                <div class="text-muted small">
                    <i class="fas fa-info-circle me-2"></i>
                    Stats temporarily unavailable
                </div>
            `;
        }
    }
}

// Generate quick stats HTML
function generateQuickStatsHTML(data) {
    const uptime = data.serviceUptime ? formatUptime(data.serviceUptime) : '< 1 min';

    return `
        <div class="fade-in">
            <div class="row g-2 text-center">
                <div class="col-12 mb-2">
                    <div class="bg-light p-2 rounded">
                        <i class="fas fa-clock text-primary"></i>
                        <div class="small text-muted">System Uptime</div>
                        <div class="fw-bold">${uptime}</div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="bg-light p-2 rounded">
                        <i class="fas fa-shield-alt text-success"></i>
                        <div class="small text-muted">Security</div>
                        <div class="fw-bold">Active</div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="bg-light p-2 rounded">
                        <i class="fas fa-tachometer-alt text-info"></i>
                        <div class="small text-muted">Performance</div>
                        <div class="fw-bold">Optimal</div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Format model name for display
function formatModelName(modelKey) {
    const names = {
        'full_model': 'Full Analysis',
        'vitals_model': 'Vital Signs',
        'diabetes_model': 'Diabetes Focus',
        'cardio_model': 'Cardiovascular',
        'minimal_model': 'Basic',
        'temp_model': 'Temperature',
        'mental_model': 'Mental Health'
    };
    return names[modelKey] || modelKey.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase());
}

// Format uptime
function formatUptime(timestamp) {
    const now = Date.now();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days}d ${hours % 24}h`;
    if (hours > 0) return `${hours}h ${minutes % 60}m`;
    if (minutes > 0) return `${minutes}m`;
    return '< 1m';
}

// Initialize form validation
function initializeFormValidation() {
    const form = document.getElementById('healthForm');
    if (!form) return;

    // Add custom validation
    form.addEventListener('submit', function(event) {
        if (!validateForm()) {
            event.preventDefault();
            event.stopPropagation();
        } else {
            // Show loading state
            showLoadingState();
        }
    });

    // Add input event listeners for real-time validation
    const inputs = form.querySelectorAll('input[type="number"]');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            validateInput(this);
        });

        input.addEventListener('blur', function() {
            validateInput(this);
        });
    });
}

// Validate entire form
function validateForm() {
    const form = document.getElementById('healthForm');
    if (!form) return false;

    let isValid = true;
    let filledFields = 0;

    // Check numeric inputs
    const numericInputs = form.querySelectorAll('input[type="number"]');
    numericInputs.forEach(input => {
        if (input.value && input.value.trim() !== '') {
            filledFields++;
            if (!validateInput(input)) {
                isValid = false;
            }
        }
    });

    // Require at least 3 fields
    if (filledFields < 3) {
        showAlert('Please fill in at least 3 health parameters for accurate analysis.', 'warning');
        isValid = false;
    }

    return isValid;
}

// Validate individual input
function validateInput(input) {
    const value = parseFloat(input.value);
    const fieldName = input.id;
    let isValid = true;
    let message = '';

    // Clear previous validation state
    input.classList.remove('is-valid', 'is-invalid');
    const feedback = input.parentNode.querySelector('.invalid-feedback');
    if (feedback) feedback.remove();

    if (input.value && input.value.trim() !== '') {
        // Field-specific validation
        switch(fieldName) {
            case 'age':
                if (value < 15 || value > 50) {
                    message = 'Age should be between 15-50 years';
                    isValid = false;
                } else if (value < 18 || value > 35) {
                    message = 'Age outside optimal range (18-35 years)';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
            case 'systolicBP':
                if (value < 70 || value > 200) {
                    message = 'Systolic BP should be between 70-200 mmHg';
                    isValid = false;
                } else if (value > 140) {
                    message = 'High blood pressure detected';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
            case 'diastolicBP':
                if (value < 40 || value > 120) {
                    message = 'Diastolic BP should be between 40-120 mmHg';
                    isValid = false;
                } else if (value > 90) {
                    message = 'High diastolic pressure detected';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
            case 'bloodSugar':
                if (value < 2 || value > 25) {
                    message = 'Blood sugar should be between 2-25 mmol/L';
                    isValid = false;
                } else if (value > 7.0) {
                    message = 'Elevated blood sugar level';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
            case 'bodyTemp':
                if (value < 95 || value > 105) {
                    message = 'Body temperature should be between 95-105°F';
                    isValid = false;
                } else if (value > 100.4 || value < 97.0) {
                    message = 'Temperature outside normal range';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
            case 'bmi':
                if (value < 10 || value > 50) {
                    message = 'BMI should be between 10-50';
                    isValid = false;
                } else if (value < 18.5 || value > 29.9) {
                    message = value < 18.5 ? 'Underweight BMI' : 'Overweight/Obese BMI';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
            case 'heartRate':
                if (value < 40 || value > 150) {
                    message = 'Heart rate should be between 40-150 bpm';
                    isValid = false;
                } else if (value > 100 || value < 60) {
                    message = value > 100 ? 'Elevated heart rate' : 'Low heart rate';
                    input.classList.add('is-valid');
                    addWarningFeedback(input, message);
                    return true;
                }
                break;
        }
    }

    if (isValid && input.value && input.value.trim() !== '') {
        input.classList.add('is-valid');
    } else if (!isValid) {
        input.classList.add('is-invalid');
        addInvalidFeedback(input, message);
    }

    return isValid;
}

// Add invalid feedback
function addInvalidFeedback(input, message) {
    const feedback = document.createElement('div');
    feedback.className = 'invalid-feedback';
    feedback.textContent = message;
    input.parentNode.appendChild(feedback);
}

// Add warning feedback
function addWarningFeedback(input, message) {
    const feedback = document.createElement('div');
    feedback.className = 'valid-feedback text-warning';
    feedback.innerHTML = `<i class="fas fa-exclamation-triangle me-1"></i>${message}`;
    input.parentNode.appendChild(feedback);
}

// Show loading state
function showLoadingState() {
    const submitBtn = document.getElementById('predictBtn');
    if (submitBtn) {
        submitBtn.innerHTML = `
            <span class="spinner-border spinner-border-sm me-2" role="status">
                <span class="visually-hidden">Loading...</span>
            </span>
            Analyzing with AI...
        `;
        submitBtn.disabled = true;
    }

    // Add loading class to form
    const form = document.getElementById('healthForm');
    if (form) {
        form.classList.add('loading');
    }
}

// Initialize tooltips
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Setup real-time form feedback
function setupRealTimeFeedback() {
    const form = document.getElementById('healthForm');
    if (!form) return;

    const inputs = form.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('input', updateFormProgress);
        input.addEventListener('change', updateFormProgress);
    });
}

// Update form progress
function updateFormProgress() {
    const form = document.getElementById('healthForm');
    if (!form) return;

    const allInputs = form.querySelectorAll('input[type="number"], input[type="checkbox"]:checked');
    let filledInputs = 0;

    allInputs.forEach(input => {
        if (input.type === 'checkbox' && input.checked) {
            filledInputs++;
        } else if (input.type === 'number' && input.value && input.value.trim() !== '') {
            filledInputs++;
        }
    });

    const totalInputs = form.querySelectorAll('input[type="number"]').length +
                       form.querySelectorAll('input[type="checkbox"]').length;
    const progress = (filledInputs / totalInputs) * 100;

    // Update progress indicator if exists
    updateProgressIndicator(progress, filledInputs);
}

// Update progress indicator
function updateProgressIndicator(progress, filledCount) {
    // You can add a progress bar to the UI if needed
    console.log(`Form completion: ${progress.toFixed(1)}% (${filledCount} fields)`);
}

// Check service status
async function checkServiceStatus() {
    try {
        const response = await fetch('/api/health');
        const data = await response.json();

        const statusAlert = document.querySelector('.alert');
        if (statusAlert && data.status === 'healthy') {
            statusAlert.className = 'alert alert-success';
            statusAlert.innerHTML = `
                <i class="fas fa-check-circle"></i>
                AI Models Ready - System Online
                <button class="btn btn-sm btn-outline-success ms-2" onclick="checkServiceStatus()">
                    <i class="fas fa-sync-alt"></i> Refresh
                </button>
            `;
        }
    } catch (error) {
        console.error('Service status check failed:', error);
    }
}

// Show alert message
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    // Insert at top of container
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);

        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }
}

// API interaction functions
async function makeApiRequest(endpoint, data = null, method = 'GET') {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        }
    };

    if (data && method !== 'GET') {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(endpoint, options);
        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || 'API request failed');
        }

        return result;
    } catch (error) {
        console.error('API request error:', error);
        throw error;
    }
}

// Get model recommendations
async function getModelRecommendations() {
    const form = document.getElementById('healthForm');
    if (!form) return;

    const healthData = extractFormData(form);

    try {
        const recommendations = await makeApiRequest('/api/models/recommend', healthData, 'POST');
        console.log('Model recommendations:', recommendations);
        return recommendations;
    } catch (error) {
        console.error('Error getting model recommendations:', error);
        return null;
    }
}

// Extract form data
function extractFormData(form) {
    const formData = new FormData(form);
    const data = {};

    for (let [key, value] of formData.entries()) {
        if (value && value.trim() !== '') {
            // Convert numeric fields
            if (['age', 'systolicBP', 'diastolicBP', 'bloodSugar', 'bodyTemp', 'bmi', 'heartRate'].includes(key)) {
                data[key] = parseFloat(value);
            }
            // Convert checkbox fields
            else if (['previousComplications', 'preexistingDiabetes', 'gestationalDiabetes', 'mentalHealth'].includes(key)) {
                data[key] = parseInt(value);
            }
            else {
                data[key] = value;
            }
        }
    });

    return data;
}

// Batch prediction functionality
async function performBatchPrediction(batchData, algorithm = 'ensemble') {
    try {
        const result = await makeApiRequest('/api/predict/batch', {
            data: batchData,
            algorithm: algorithm
        }, 'POST');

        return result;
    } catch (error) {
        console.error('Batch prediction error:', error);
        throw error;
    }
}

// Export functions for external use
window.PregnancyRiskApp = {
    checkServiceStatus,
    makeApiRequest,
    getModelRecommendations,
    performBatchPrediction,
    showAlert,
    validateForm,
    extractFormData
};

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    }
}

// Performance monitoring
function logPerformance(label, startTime) {
    const endTime = performance.now();
    console.log(`${label}: ${(endTime - startTime).toFixed(2)}ms`);
}

// Error reporting
function reportError(error, context) {
    console.error(`Error in ${context}:`, error);

    // You can implement error reporting service here
    // Example: send to analytics or error tracking service
}

// Accessibility helpers
function announceToScreenReader(message) {
    const announcement = document.createElement('div');
    announcement.setAttribute('aria-live', 'polite');
    announcement.setAttribute('aria-atomic', 'true');
    announcement.className = 'sr-only';
    announcement.textContent = message;

    document.body.appendChild(announcement);

    setTimeout(() => {
        document.body.removeChild(announcement);
    }, 1000);
}

// Initialize performance observer
if ('PerformanceObserver' in window) {
    const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
            if (entry.entryType === 'navigation') {
                console.log('Page load time:', entry.loadEventEnd - entry.loadEventStart, 'ms');
            }
        });
    });

    observer.observe({ entryTypes: ['navigation'] });
}