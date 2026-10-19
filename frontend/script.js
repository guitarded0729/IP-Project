/**
 * ==========================================================================
 * Smart Campus Lost & Found Management System - Pure Vanilla JavaScript
 * Offline-ready, single-page application logic with zero external libraries.
 * ==========================================================================
 */

// Application State
const state = {
    currentSection: 'section-home',
    adminToken: sessionStorage.getItem('adminToken') || null,
    adminUser: sessionStorage.getItem('adminUser') || null,
    items: [],
    stats: null,
    activeModalItem: null,
    filters: {
        query: '',
        type: 'ALL',
        category: 'ALL',
        status: 'ALL'
    }
};

// ==========================================================================
// 1. HELPER & UTILITY FUNCTIONS
// ==========================================================================

// Prevents Cross-Site Scripting (XSS) by escaping HTML characters
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Display floating toast notifications
function showToast(type, title, message) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-content">
            <div class="toast-title">${escapeHtml(title)}</div>
            <div class="toast-msg">${escapeHtml(message)}</div>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(20px)';
        toast.style.transition = 'all 0.25s ease';
        setTimeout(() => toast.remove(), 250);
    }, 3500);
}

// Format YYYY-MM-DD into a human-friendly string
function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    try {
        const parts = dateStr.split('-');
        if (parts.length === 3) {
            const d = new Date(parts[0], parts[1] - 1, parts[2]);
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        }
    } catch (e) {}
    return dateStr;
}

// Get current date formatted as YYYY-MM-DD for date input limits
function getTodayDateString() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Clear all inline error messages in a form
function clearFormErrors(formElement) {
    const errorContainers = formElement.querySelectorAll('.field-error');
    errorContainers.forEach(container => container.textContent = '');

    const inputs = formElement.querySelectorAll('.input-error');
    inputs.forEach(input => input.classList.remove('input-error'));
}

// Set inline error message for a specific field
function setFieldError(fieldId, errorContainerId, message) {
    const field = document.getElementById(fieldId);
    const container = document.getElementById(errorContainerId);
    if (field) field.classList.add('input-error');
    if (container) container.textContent = message;
}

// ==========================================================================
// 2. API FUNCTIONS (Fetch with async/await and URLSearchParams)
// ==========================================================================

// GET /api/stats
async function apiGetStats() {
    const response = await fetch('/api/stats');
    if (!response.ok) throw new Error('Failed to load dashboard statistics.');
    return await response.json();
}

// GET /api/items with optional filters
async function apiGetItems(filters = {}) {
    const params = new URLSearchParams();
    if (filters.type && filters.type !== 'ALL') params.append('type', filters.type);
    if (filters.category && filters.category !== 'ALL') params.append('category', filters.category);
    if (filters.status && filters.status !== 'ALL') params.append('status', filters.status);
    if (filters.query && filters.query.trim()) params.append('q', filters.query.trim());

    const url = `/api/items?${params.toString()}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Failed to fetch items from server.');
    return await response.json();
}

// GET /api/items/{id}
async function apiGetItemById(id) {
    const response = await fetch(`/api/items/${id}`);
    if (!response.ok) throw new Error(`Item #${id} not found.`);
    return await response.json();
}

// GET /api/items/{id}/matches
async function apiGetSmartMatches(id) {
    const response = await fetch(`/api/items/${id}/matches`);
    if (!response.ok) throw new Error('Failed to retrieve smart matches.');
    return await response.json();
}

// POST /api/items (form-urlencoded)
async function apiCreateItem(itemData) {
    const body = new URLSearchParams(itemData);
    const response = await fetch('/api/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
    });

    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Failed to submit item report.');
    return data;
}

// POST /api/claims (form-urlencoded)
async function apiCreateClaim(claimData) {
    const body = new URLSearchParams(claimData);
    const response = await fetch('/api/claims', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
    });

    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Failed to submit claim.');
    return data;
}

// POST /api/login (form-urlencoded)
async function apiLogin(username, password) {
    const body = new URLSearchParams({ username, password });
    const response = await fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
    });

    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Invalid username or password.');
    return data;
}

// POST /api/logout
async function apiLogout() {
    if (!state.adminToken) return;
    try {
        await fetch('/api/logout', {
            method: 'POST',
            headers: { 'X-Admin-Token': state.adminToken }
        });
    } catch (e) {
        console.warn('Logout call failed:', e);
    }
}

// GET /api/claims (Admin protected)
async function apiGetAdminClaims() {
    if (!state.adminToken) throw new Error('Admin authentication required.');
    const response = await fetch('/api/claims', {
        headers: { 'X-Admin-Token': state.adminToken }
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Failed to fetch claims.');
    return data;
}

// PUT /api/claims/{id} (Admin protected)
async function apiProcessClaim(claimId, action) {
    if (!state.adminToken) throw new Error('Admin authentication required.');
    const body = new URLSearchParams({ action });
    const response = await fetch(`/api/claims/${claimId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Admin-Token': state.adminToken
        },
        body: body.toString()
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Failed to process claim.');
    return data;
}

// PUT /api/items/{id} (Admin protected)
async function apiUpdateItemStatus(itemId, newStatus) {
    if (!state.adminToken) throw new Error('Admin authentication required.');
    const body = new URLSearchParams({ status: newStatus });
    const response = await fetch(`/api/items/${itemId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Admin-Token': state.adminToken
        },
        body: body.toString()
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Failed to update item status.');
    return data;
}

// DELETE /api/items/{id} (Admin protected)
async function apiDeleteItem(itemId) {
    if (!state.adminToken) throw new Error('Admin authentication required.');
    const response = await fetch(`/api/items/${itemId}`, {
        method: 'DELETE',
        headers: { 'X-Admin-Token': state.adminToken }
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Failed to delete item.');
    return data;
}

// ==========================================================================
// 3. VALIDATION FUNCTIONS
// ==========================================================================

function validateReportForm(formData) {
    let isValid = true;
    const form = document.getElementById('report-item-form');
    clearFormErrors(form);

    const title = formData.get('title') ? formData.get('title').trim() : '';
    const category = formData.get('category');
    const location = formData.get('location');
    const date = formData.get('date');
    const description = formData.get('description') ? formData.get('description').trim() : '';
    const reporterRole = formData.get('reporterRole');
    const reporterId = formData.get('reporterId') ? formData.get('reporterId').trim() : '';
    const reporterName = formData.get('reporterName') ? formData.get('reporterName').trim() : '';
    const reporterContact = formData.get('reporterContact') ? formData.get('reporterContact').trim() : '';

    // Title validation
    if (!title) {
        setFieldError('item-title', 'error-title', 'Item title is required.');
        isValid = false;
    } else if (title.length < 2) {
        setFieldError('item-title', 'error-title', 'Title must be at least 2 characters long.');
        isValid = false;
    }

    // Category validation
    if (!category) {
        setFieldError('item-category', 'error-category', 'Please select a category.');
        isValid = false;
    }

    // Location validation
    if (!location) {
        setFieldError('item-location', 'error-location', 'Please choose a campus location.');
        isValid = false;
    }

    // Date validation
    if (!date) {
        setFieldError('item-date', 'error-date', 'Date is required.');
        isValid = false;
    } else {
        const today = getTodayDateString();
        if (date > today) {
            setFieldError('item-date', 'error-date', 'Report date cannot be in the future.');
            isValid = false;
        }
    }

    // Description validation
    if (!description) {
        setFieldError('item-description', 'error-description', 'Description is required.');
        isValid = false;
    } else if (description.length < 5) {
        setFieldError('item-description', 'error-description', 'Please provide at least 5 characters of description.');
        isValid = false;
    }

    // Reporter Role validation
    if (!reporterRole) {
        setFieldError('reporter-role', 'error-reporterRole', 'Select your role (Student or Staff).');
        isValid = false;
    }

    // Reporter ID validation
    if (!reporterId) {
        setFieldError('reporter-id', 'error-reporterId', 'Roll Number or Staff ID is required.');
        isValid = false;
    }

    // Reporter Name validation
    if (!reporterName) {
        setFieldError('reporter-name', 'error-reporterName', 'Reporter name is required.');
        isValid = false;
    } else if (reporterName.length < 2) {
        setFieldError('reporter-name', 'error-reporterName', 'Name must be at least 2 characters.');
        isValid = false;
    }

    // Reporter Contact validation
    if (!reporterContact) {
        setFieldError('reporter-contact', 'error-reporterContact', 'Phone number or email is required.');
        isValid = false;
    } else if (reporterContact.length < 6) {
        setFieldError('reporter-contact', 'error-reporterContact', 'Enter a valid phone number or email.');
        isValid = false;
    }

    return isValid;
}

function validateClaimForm(formData) {
    let isValid = true;
    const form = document.getElementById('claim-form');
    clearFormErrors(form);

    const claimantName = formData.get('claimantName') ? formData.get('claimantName').trim() : '';
    const claimantIdNumber = formData.get('claimantIdNumber') ? formData.get('claimantIdNumber').trim() : '';
    const claimantContact = formData.get('claimantContact') ? formData.get('claimantContact').trim() : '';
    const claimDate = formData.get('claimDate');
    const proofDetails = formData.get('proofDetails') ? formData.get('proofDetails').trim() : '';

    if (!claimantName) {
        setFieldError('claimant-name', 'error-claimantName', 'Your full name is required.');
        isValid = false;
    }

    if (!claimantIdNumber) {
        setFieldError('claimant-id', 'error-claimantIdNumber', 'Student / Staff ID is required.');
        isValid = false;
    }

    if (!claimantContact || claimantContact.length < 6) {
        setFieldError('claimant-contact', 'error-claimantContact', 'Valid contact number or email required.');
        isValid = false;
    }

    if (!claimDate) {
        setFieldError('claim-date', 'error-claimDate', 'Claim date is required.');
        isValid = false;
    } else {
        const today = getTodayDateString();
        if (claimDate > today) {
            setFieldError('claim-date', 'error-claimDate', 'Date cannot be in the future.');
            isValid = false;
        }
    }

    if (!proofDetails) {
        setFieldError('claim-proof', 'error-proofDetails', 'Proof details are required.');
        isValid = false;
    } else if (proofDetails.length < 10) {
        setFieldError('claim-proof', 'error-proofDetails', 'Please provide descriptive ownership proof (at least 10 characters).');
        isValid = false;
    }

    return isValid;
}

// ==========================================================================
// 4. RENDERING FUNCTIONS
// ==========================================================================

// Render Dashboard live statistics
function renderStats(stats) {
    state.stats = stats;
    document.getElementById('stat-lost-count').textContent = stats.totalLost;
    document.getElementById('stat-found-count').textContent = stats.totalFound;
    document.getElementById('stat-pending-count').textContent = stats.pendingClaims;
    document.getElementById('stat-returned-count').textContent = stats.returnedItems;

    const timeLabel = document.getElementById('stats-refreshed-time');
    if (timeLabel) {
        const now = new Date();
        timeLabel.textContent = `Updated: ${now.toLocaleTimeString()}`;
    }
}

// Render Recent items preview on Home section
function renderRecentItems(items) {
    const container = document.getElementById('home-recent-items-container');
    if (!container) return;

    if (!items || items.length === 0) {
        container.innerHTML = `<div class="empty-state"><p>No items reported yet.</p></div>`;
        return;
    }

    // Show up to 5 most recent items
    const recent = items.slice(0, 5);
    container.innerHTML = '';

    recent.forEach(item => {
        const row = document.createElement('div');
        row.className = 'recent-item-row';
        row.setAttribute('role', 'button');
        row.tabIndex = 0;

        const isLost = item.type === 'LOST';
        const typeBadge = isLost 
            ? `<span class="badge badge-lost">LOST</span>` 
            : `<span class="badge badge-found">FOUND</span>`;

        let statusBadge = '';
        if (item.status === 'OPEN') statusBadge = `<span class="badge badge-open">Open</span>`;
        else if (item.status === 'CLAIM_PENDING') statusBadge = `<span class="badge badge-pending">Claim Pending</span>`;
        else statusBadge = `<span class="badge badge-returned">Returned</span>`;

        row.innerHTML = `
            <div class="recent-item-left">
                ${typeBadge}
                <div>
                    <div class="recent-item-title">${escapeHtml(item.title)}</div>
                    <div class="recent-item-meta">${escapeHtml(item.location)} &bull; ${formatDate(item.date)}</div>
                </div>
            </div>
            <div>${statusBadge}</div>
        `;

        row.addEventListener('click', () => openItemModal(item.id));
        container.appendChild(row);
    });
}

// Render Browse items cards grid
function renderBrowseItems(items) {
    const grid = document.getElementById('items-grid');
    const countLabel = document.getElementById('results-count');
    if (!grid) return;

    grid.innerHTML = '';

    if (!items || items.length === 0) {
        countLabel.textContent = 'Showing 0 items';
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔍</div>
                <h3>No Matching Items Found</h3>
                <p>Try modifying your keyword search or adjusting the category and status filters.</p>
            </div>
        `;
        return;
    }

    countLabel.textContent = `Showing ${items.length} item${items.length === 1 ? '' : 's'}`;

    items.forEach(item => {
        const card = document.createElement('div');
        card.className = 'item-card';

        const isLost = item.type === 'LOST';
        const typeBadge = isLost 
            ? `<span class="badge badge-lost">LOST</span>` 
            : `<span class="badge badge-found">FOUND</span>`;

        let statusBadge = '';
        if (item.status === 'OPEN') statusBadge = `<span class="badge badge-open">Open</span>`;
        else if (item.status === 'CLAIM_PENDING') statusBadge = `<span class="badge badge-pending">Claim Pending</span>`;
        else statusBadge = `<span class="badge badge-returned">Returned</span>`;

        card.innerHTML = `
            <div>
                <div class="item-card-top">
                    <div class="card-badges">
                        ${typeBadge}
                        <span class="badge badge-category">${escapeHtml(item.categoryDisplay || item.category)}</span>
                    </div>
                    <div>${statusBadge}</div>
                </div>
                <h4 class="item-title">${escapeHtml(item.title)}</h4>
                <p class="item-desc-snippet">${escapeHtml(item.description)}</p>
            </div>

            <div>
                <div class="item-card-meta">
                    <div class="meta-row">
                        <span>📍</span>
                        <span>${escapeHtml(item.location)}</span>
                    </div>
                    <div class="meta-row">
                        <span>📅</span>
                        <span>${formatDate(item.date)}</span>
                    </div>
                </div>

                <div class="item-card-footer">
                    <span class="reporter-tag">By ${escapeHtml(item.reporter ? item.reporter.role : 'Campus User')}</span>
                    <button class="btn btn-secondary btn-small">View Details</button>
                </div>
            </div>
        `;

        card.addEventListener('click', () => openItemModal(item.id));
        grid.appendChild(card);
    });
}

// Render Item Details & Matches Modal
async function openItemModal(itemId) {
    try {
        const item = await apiGetItemById(itemId);
        state.activeModalItem = item;

        const modal = document.getElementById('item-modal');
        const modalTitle = document.getElementById('modal-title');
        const modalBadges = document.getElementById('modal-badges');
        const modalMeta = document.getElementById('modal-meta');
        const modalDesc = document.getElementById('modal-description');
        const modalLocation = document.getElementById('modal-location');
        const modalDate = document.getElementById('modal-date');
        const modalCategory = document.getElementById('modal-category');
        const modalStatus = document.getElementById('modal-status');
        const modalReporterName = document.getElementById('modal-reporter-name');
        const modalReporterRole = document.getElementById('modal-reporter-role');
        const modalReporterId = document.getElementById('modal-reporter-id');
        const modalReporterContact = document.getElementById('modal-reporter-contact');
        const claimTriggerBtn = document.getElementById('modal-claim-btn-trigger');
        const claimTabBtn = document.getElementById('modal-tab-claim');

        // Populate basic item fields
        modalTitle.textContent = item.title;
        modalDesc.textContent = item.description;
        modalLocation.textContent = item.location;
        modalDate.textContent = formatDate(item.date);
        modalCategory.textContent = item.categoryDisplay || item.category;
        modalStatus.textContent = item.statusDisplay || item.status;

        // Reporter fields
        if (item.reporter) {
            modalReporterName.textContent = item.reporter.name;
            modalReporterRole.textContent = item.reporter.role;
            modalReporterId.textContent = item.reporter.idNumber;
            modalReporterContact.textContent = item.reporter.contact;
        } else {
            modalReporterName.textContent = 'Anonymous';
            modalReporterRole.textContent = 'Campus Member';
            modalReporterId.textContent = '-';
            modalReporterContact.textContent = '-';
        }

        // Badges in modal header
        const isLost = item.type === 'LOST';
        modalBadges.innerHTML = `
            <span class="badge ${isLost ? 'badge-lost' : 'badge-found'}">${item.type}</span>
            <span class="badge badge-category">${escapeHtml(item.categoryDisplay || item.category)}</span>
            <span class="badge ${item.status === 'OPEN' ? 'badge-open' : item.status === 'CLAIM_PENDING' ? 'badge-pending' : 'badge-returned'}">${escapeHtml(item.statusDisplay || item.status)}</span>
        `;
        modalMeta.textContent = `Reported on ${formatDate(item.date)} at ${item.location}`;

        // Claim form button logic: Only FOUND items with OPEN status are claimable
        const isClaimable = item.type === 'FOUND' && item.status === 'OPEN';
        if (isClaimable) {
            claimTriggerBtn.classList.remove('hidden');
            claimTabBtn.classList.remove('hidden');
            document.getElementById('claim-item-id').value = item.id;
            document.getElementById('claim-date').value = getTodayDateString();
        } else {
            claimTriggerBtn.classList.add('hidden');
            claimTabBtn.classList.add('hidden');
        }

        // Reset modal to Details tab
        switchModalTab('details');

        // Show modal backdrop
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';

        // Load Smart Matches asynchronously
        loadModalSmartMatches(item.id);

    } catch (err) {
        showToast('error', 'Error', err.message);
    }
}

// Load and render smart matches in modal
async function loadModalSmartMatches(itemId) {
    const listContainer = document.getElementById('modal-matches-list');
    const badgeCount = document.getElementById('modal-match-count');
    listContainer.innerHTML = '<div class="loading-placeholder">Searching campus database for matches...</div>';

    try {
        const matches = await apiGetSmartMatches(itemId);
        badgeCount.textContent = matches.length;

        if (matches.length === 0) {
            listContainer.innerHTML = `
                <div class="empty-state">
                    <p>No high-confidence matches found yet.</p>
                    <small>Matches are automatically suggested when opposite-type items share categories, campus locations, or title keywords.</small>
                </div>
            `;
            return;
        }

        listContainer.innerHTML = '';
        matches.forEach(m => {
            const card = document.createElement('div');
            card.className = 'match-card';

            const matchItem = m.item;
            const score = m.score;
            const reasons = m.reasons;

            card.innerHTML = `
                <div class="match-card-top">
                    <span class="badge ${matchItem.type === 'LOST' ? 'badge-lost' : 'badge-found'}">${matchItem.type}</span>
                    <span class="match-score-pill">🔥 ${score}% Match</span>
                </div>
                <div class="match-card-title">${escapeHtml(matchItem.title)}</div>
                <div class="match-card-reasons">✨ Match Factors: ${escapeHtml(reasons)}</div>
                <p class="match-card-desc">${escapeHtml(matchItem.description)}</p>
                <div class="meta-row" style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">
                    <span>📍 ${escapeHtml(matchItem.location)} &bull; 📅 ${formatDate(matchItem.date)}</span>
                </div>
                <button class="btn btn-secondary btn-small" data-switch-id="${matchItem.id}">View This Item</button>
            `;

            const switchBtn = card.querySelector('[data-switch-id]');
            switchBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                openItemModal(matchItem.id);
            });

            listContainer.appendChild(card);
        });

    } catch (err) {
        listContainer.innerHTML = `<div class="field-error">Could not calculate matches: ${escapeHtml(err.message)}</div>`;
    }
}

function switchModalTab(tabName) {
    const tabs = document.querySelectorAll('.modal-tab');
    const panes = document.querySelectorAll('.modal-pane');

    tabs.forEach(tab => {
        if (tab.getAttribute('data-tab') === tabName) {
            tab.classList.add('active');
        } else {
            tab.classList.remove('active');
        }
    });

    panes.forEach(pane => {
        if (pane.id === `modal-pane-${tabName}`) {
            pane.classList.add('active');
        } else {
            pane.classList.remove('active');
        }
    });
}

function closeModal() {
    const modal = document.getElementById('item-modal');
    modal.classList.add('hidden');
    document.body.style.overflow = '';
    state.activeModalItem = null;
}

// ==========================================================================
// 5. ADMIN PORTAL LOGIC & RENDERING
// ==========================================================================

function updateAdminAuthState() {
    const loginView = document.getElementById('admin-login-view');
    const dashboardView = document.getElementById('admin-dashboard-view');
    const userBadge = document.getElementById('admin-user-badge');
    const adminNavIndicator = document.getElementById('admin-indicator');

    if (state.adminToken) {
        loginView.classList.add('hidden');
        dashboardView.classList.remove('hidden');
        userBadge.classList.remove('hidden');
        adminNavIndicator.classList.add('logged-in');
        loadAdminData();
    } else {
        loginView.classList.remove('hidden');
        dashboardView.classList.add('hidden');
        userBadge.classList.add('hidden');
        adminNavIndicator.classList.remove('logged-in');
    }
}

async function loadAdminData() {
    if (!state.adminToken) return;
    try {
        const claims = await apiGetAdminClaims();
        renderAdminClaims(claims);

        const items = await apiGetItems({});
        renderAdminItems(items);
    } catch (err) {
        showToast('error', 'Admin Session Expired', err.message);
        handleAdminLogout();
    }
}

function renderAdminClaims(claims) {
    const tbody = document.getElementById('claims-table-body');
    const badge = document.getElementById('admin-claims-badge');
    if (!tbody) return;

    badge.textContent = claims.length;
    tbody.innerHTML = '';

    if (claims.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem;">No ownership claims recorded yet.</td></tr>`;
        return;
    }

    claims.forEach(claim => {
        const tr = document.createElement('tr');

        let statusClass = 'badge-pending';
        if (claim.status === 'APPROVED') statusClass = 'badge-open';
        if (claim.status === 'REJECTED') statusClass = 'badge-lost';

        const isPending = claim.status === 'PENDING';

        tr.innerHTML = `
            <td><strong>#${claim.id}</strong></td>
            <td>Item #${claim.itemId}</td>
            <td>
                <strong>${escapeHtml(claim.claimantName)}</strong><br>
                <small>${escapeHtml(claim.claimantIdNumber)} &bull; ${escapeHtml(claim.claimantContact)}</small>
            </td>
            <td><small style="max-width:240px; display:inline-block;">${escapeHtml(claim.proofDetails)}</small></td>
            <td>${formatDate(claim.claimDate)}</td>
            <td><span class="badge ${statusClass}">${escapeHtml(claim.status)}</span></td>
            <td>
                <div class="table-actions">
                    ${isPending ? `
                        <button class="btn btn-teal btn-small" data-claim-action="APPROVE" data-claim-id="${claim.id}">Approve</button>
                        <button class="btn btn-danger btn-small" data-claim-action="REJECT" data-claim-id="${claim.id}">Reject</button>
                    ` : `<span style="font-size:0.75rem; color:var(--text-subtle);">Processed</span>`}
                </div>
            </td>
        `;

        const approveBtn = tr.querySelector('[data-claim-action="APPROVE"]');
        const rejectBtn = tr.querySelector('[data-claim-action="REJECT"]');

        if (approveBtn) {
            approveBtn.addEventListener('click', () => processClaimAction(claim.id, 'APPROVE'));
        }
        if (rejectBtn) {
            rejectBtn.addEventListener('click', () => processClaimAction(claim.id, 'REJECT'));
        }

        tbody.appendChild(tr);
    });
}

async function processClaimAction(claimId, action) {
    const confirmMsg = action === 'APPROVE' 
        ? `Approve Claim #${claimId}? This will mark the item as RETURNED.`
        : `Reject Claim #${claimId}?`;

    if (!confirm(confirmMsg)) return;

    try {
        await apiProcessClaim(claimId, action);
        showToast('success', 'Claim Processed', `Claim #${claimId} was ${action.toLowerCase()}d.`);
        await loadAdminData();
        await refreshDashboardAndItems();
    } catch (err) {
        showToast('error', 'Action Failed', err.message);
    }
}

function renderAdminItems(items) {
    const tbody = document.getElementById('admin-items-table-body');
    const badge = document.getElementById('admin-items-badge');
    if (!tbody) return;

    badge.textContent = items.length;
    tbody.innerHTML = '';

    if (items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 2rem;">No items recorded in database.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const tr = document.createElement('tr');
        const isLost = item.type === 'LOST';

        tr.innerHTML = `
            <td>#${item.id}</td>
            <td><span class="badge ${isLost ? 'badge-lost' : 'badge-found'}">${item.type}</span></td>
            <td><strong>${escapeHtml(item.title)}</strong></td>
            <td>${escapeHtml(item.categoryDisplay || item.category)}</td>
            <td>${escapeHtml(item.location)}</td>
            <td><small>${escapeHtml(item.reporter ? item.reporter.name : '-')}</small></td>
            <td>
                <select class="admin-status-select" data-item-id="${item.id}" style="height: 30px; font-size: 0.8rem; padding: 0 0.4rem;">
                    <option value="OPEN" ${item.status === 'OPEN' ? 'selected' : ''}>OPEN</option>
                    <option value="CLAIM_PENDING" ${item.status === 'CLAIM_PENDING' ? 'selected' : ''}>CLAIM_PENDING</option>
                    <option value="RETURNED" ${item.status === 'RETURNED' ? 'selected' : ''}>RETURNED</option>
                </select>
            </td>
            <td>
                <button class="btn btn-danger btn-small" data-delete-id="${item.id}">Delete</button>
            </td>
        `;

        const statusSelect = tr.querySelector('.admin-status-select');
        statusSelect.addEventListener('change', async (e) => {
            const newStatus = e.target.value;
            try {
                await apiUpdateItemStatus(item.id, newStatus);
                showToast('success', 'Status Updated', `Item #${item.id} status changed to ${newStatus}`);
                await refreshDashboardAndItems();
            } catch (err) {
                showToast('error', 'Update Failed', err.message);
                loadAdminData();
            }
        });

        const deleteBtn = tr.querySelector('[data-delete-id]');
        deleteBtn.addEventListener('click', async () => {
            if (!confirm(`Are you sure you want to permanently delete Item #${item.id}: "${item.title}"?`)) return;
            try {
                await apiDeleteItem(item.id);
                showToast('success', 'Deleted', `Item #${item.id} deleted.`);
                await loadAdminData();
                await refreshDashboardAndItems();
            } catch (err) {
                showToast('error', 'Delete Failed', err.message);
            }
        });

        tbody.appendChild(tr);
    });
}

function handleAdminLogout() {
    apiLogout();
    state.adminToken = null;
    state.adminUser = null;
    sessionStorage.removeItem('adminToken');
    sessionStorage.removeItem('adminUser');
    updateAdminAuthState();
    showToast('info', 'Logged Out', 'You have been logged out of the admin panel.');
}

// ==========================================================================
// 6. SECTION NAVIGATION & COORDINATION
// ==========================================================================

function navigateTo(sectionId) {
    state.currentSection = sectionId;

    // Update section active classes
    const sections = document.querySelectorAll('.page-section');
    sections.forEach(sec => {
        if (sec.id === sectionId) {
            sec.classList.add('active');
        } else {
            sec.classList.remove('active');
        }
    });

    // Update nav buttons active classes
    const navButtons = document.querySelectorAll('.nav-btn');
    navButtons.forEach(btn => {
        if (btn.getAttribute('data-section') === sectionId) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Section specific refresh
    if (sectionId === 'section-home') {
        refreshDashboardAndItems();
    } else if (sectionId === 'section-browse') {
        applyFiltersAndRender();
    } else if (sectionId === 'section-admin') {
        updateAdminAuthState();
    }
}

async function refreshDashboardAndItems() {
    try {
        const stats = await apiGetStats();
        renderStats(stats);

        const items = await apiGetItems({});
        state.items = items;
        renderRecentItems(items);
    } catch (err) {
        console.error('Error refreshing dashboard:', err);
    }
}

async function applyFiltersAndRender() {
    const loader = document.getElementById('browse-loading-indicator');
    if (loader) loader.classList.remove('hidden');

    try {
        const filtered = await apiGetItems(state.filters);
        state.items = filtered;
        renderBrowseItems(filtered);
    } catch (err) {
        showToast('error', 'Filter Error', err.message);
    } finally {
        if (loader) loader.classList.add('hidden');
    }
}

// ==========================================================================
// 7. EVENT LISTENERS SETUP
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {

    // Set today's date on date pickers as default and max value
    const today = getTodayDateString();
    const dateInput = document.getElementById('item-date');
    if (dateInput) {
        dateInput.value = today;
        dateInput.max = today;
    }
    const claimDateInput = document.getElementById('claim-date');
    if (claimDateInput) {
        claimDateInput.value = today;
        claimDateInput.max = today;
    }

    // Top Navigation click handlers
    document.getElementById('nav-home').addEventListener('click', () => navigateTo('section-home'));
    document.getElementById('nav-browse').addEventListener('click', () => navigateTo('section-browse'));
    document.getElementById('nav-report').addEventListener('click', () => navigateTo('section-report'));
    document.getElementById('nav-admin').addEventListener('click', () => navigateTo('section-admin'));
    document.getElementById('brand-link').addEventListener('click', () => navigateTo('section-home'));

    // Hero Call-to-action buttons
    document.getElementById('hero-btn-lost').addEventListener('click', () => {
        document.getElementById('type-lost').checked = true;
        navigateTo('section-report');
    });
    document.getElementById('hero-btn-found').addEventListener('click', () => {
        document.getElementById('type-found').checked = true;
        navigateTo('section-report');
    });
    document.getElementById('hero-btn-browse').addEventListener('click', () => navigateTo('section-browse'));
    document.getElementById('home-view-all-link').addEventListener('click', () => navigateTo('section-browse'));

    // Filter controls in Browse Section
    const queryInput = document.getElementById('filter-query');
    let debounceTimer = null;
    queryInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            state.filters.query = e.target.value;
            applyFiltersAndRender();
        }, 300);
    });

    document.getElementById('btn-clear-search').addEventListener('click', () => {
        queryInput.value = '';
        state.filters.query = '';
        applyFiltersAndRender();
    });

    document.getElementById('filter-type').addEventListener('change', (e) => {
        state.filters.type = e.target.value;
        applyFiltersAndRender();
    });

    document.getElementById('filter-category').addEventListener('change', (e) => {
        state.filters.category = e.target.value;
        applyFiltersAndRender();
    });

    document.getElementById('filter-status').addEventListener('change', (e) => {
        state.filters.status = e.target.value;
        applyFiltersAndRender();
    });

    document.getElementById('btn-reset-filters').addEventListener('click', () => {
        queryInput.value = '';
        document.getElementById('filter-type').value = 'ALL';
        document.getElementById('filter-category').value = 'ALL';
        document.getElementById('filter-status').value = 'ALL';
        state.filters = { query: '', type: 'ALL', category: 'ALL', status: 'ALL' };
        applyFiltersAndRender();
    });

    // Report Item Form Submit
    const reportForm = document.getElementById('report-item-form');
    reportForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(reportForm);

        if (!validateReportForm(formData)) {
            showToast('error', 'Validation Error', 'Please correct the highlighted form fields.');
            return;
        }

        const spinner = document.getElementById('report-submit-spinner');
        const submitText = document.getElementById('report-submit-text');
        spinner.classList.remove('hidden');
        submitText.textContent = 'Submitting...';

        try {
            const created = await apiCreateItem(formData);
            showToast('success', 'Report Filed', `Item "${created.title}" successfully recorded as ${created.type}!`);
            reportForm.reset();
            document.getElementById('type-lost').checked = true;
            document.getElementById('item-date').value = getTodayDateString();

            // Refresh live stats & items
            await refreshDashboardAndItems();

            // Navigate to Browse section to see the new item
            navigateTo('section-browse');
        } catch (err) {
            showToast('error', 'Submission Failed', err.message);
        } finally {
            spinner.classList.add('hidden');
            submitText.textContent = 'Submit Report';
        }
    });

    // Modal tabs & buttons
    document.getElementById('modal-close-btn').addEventListener('click', closeModal);
    document.getElementById('item-modal').addEventListener('click', (e) => {
        if (e.target.id === 'item-modal') closeModal();
    });

    document.getElementById('modal-tab-details').addEventListener('click', () => switchModalTab('details'));
    document.getElementById('modal-tab-matches').addEventListener('click', () => switchModalTab('matches'));
    document.getElementById('modal-tab-claim').addEventListener('click', () => switchModalTab('claim'));

    document.getElementById('modal-claim-btn-trigger').addEventListener('click', () => switchModalTab('claim'));
    document.getElementById('btn-cancel-claim').addEventListener('click', () => switchModalTab('details'));

    // Claim Form Submit
    const claimForm = document.getElementById('claim-form');
    claimForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(claimForm);

        if (!validateClaimForm(formData)) {
            showToast('error', 'Validation Error', 'Please fill in all claim fields with valid details.');
            return;
        }

        const spinner = document.getElementById('claim-submit-spinner');
        const submitText = document.getElementById('claim-submit-text');
        spinner.classList.remove('hidden');
        submitText.textContent = 'Filing Claim...';

        try {
            const createdClaim = await apiCreateClaim(formData);
            showToast('success', 'Claim Filed', `Claim #${createdClaim.id} submitted! Status is now CLAIM_PENDING.`);
            claimForm.reset();
            closeModal();
            await refreshDashboardAndItems();
            await applyFiltersAndRender();
        } catch (err) {
            showToast('error', 'Claim Failed', err.message);
        } finally {
            spinner.classList.add('hidden');
            submitText.textContent = 'Submit Claim';
        }
    });

    // Admin Login Form Submit
    const adminLoginForm = document.getElementById('admin-login-form');
    adminLoginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('admin-username').value;
        const password = document.getElementById('admin-password').value;

        const spinner = document.getElementById('login-spinner');
        const btnText = document.getElementById('login-btn-text');
        spinner.classList.remove('hidden');
        btnText.textContent = 'Verifying...';

        try {
            const res = await apiLogin(username, password);
            state.adminToken = res.token;
            state.adminUser = res.username;
            sessionStorage.setItem('adminToken', res.token);
            sessionStorage.setItem('adminUser', res.username);

            updateAdminAuthState();
            showToast('success', 'Authenticated', `Welcome back, Administrator!`);
        } catch (err) {
            showToast('error', 'Authentication Failed', err.message);
        } finally {
            spinner.classList.add('hidden');
            btnText.textContent = 'Sign In to Admin Portal';
        }
    });

    // Admin Logout button
    document.getElementById('btn-admin-logout').addEventListener('click', handleAdminLogout);

    // Admin Subtabs (Claims vs Items)
    document.getElementById('tab-admin-claims').addEventListener('click', () => {
        document.getElementById('tab-admin-claims').classList.add('active');
        document.getElementById('tab-admin-items').classList.remove('active');
        document.getElementById('admin-claims-pane').classList.add('active');
        document.getElementById('admin-items-pane').classList.remove('active');
    });

    document.getElementById('tab-admin-items').addEventListener('click', () => {
        document.getElementById('tab-admin-items').classList.add('active');
        document.getElementById('tab-admin-claims').classList.remove('active');
        document.getElementById('admin-items-pane').classList.add('active');
        document.getElementById('admin-claims-pane').classList.remove('active');
    });

    // Initial Data Fetch
    refreshDashboardAndItems();
    updateAdminAuthState();
});
