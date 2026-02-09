import config from './config.js';

const BASE_URL = config.API_BASE_URL;

let notifications = [];
let unreadCount = 0;

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    initNotifications();
});

async function initNotifications() {
    await loadNotifications();
    setupNotificationButton();
    startPolling(30000); // Poll every 30 seconds
}

async function loadNotifications() {
    try {
        const response = await fetch(`${BASE_URL}/notifications`, {
            method: 'GET',
            credentials: 'include'
        });

        if (!response.ok) return;

        notifications = await response.json();
        updateBadge();
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

function updateBadge() {
    unreadCount = notifications.filter(n => !n.isRead).length;
    
    const badge = document.querySelector('.notification-badge');
    if (badge) {
        if (unreadCount > 0) {
            badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
            badge.style.display = 'block';
        } else {
            badge.style.display = 'none';
        }
    }
}

function setupNotificationButton() {
    const notificationBtn = document.querySelector('.notification-btn');
    if (!notificationBtn) return;

    // Create dropdown
    const dropdown = createDropdown();
    notificationBtn.parentElement.style.position = 'relative';
    notificationBtn.parentElement.appendChild(dropdown);

    // Toggle dropdown on click
    notificationBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
        if (dropdown.style.display === 'block') {
            renderNotifications(dropdown);
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', () => {
        dropdown.style.display = 'none';
    });

    dropdown.addEventListener('click', (e) => {
        e.stopPropagation();
    });
}

function createDropdown() {
    const dropdown = document.createElement('div');
    dropdown.className = 'notification-dropdown';
    dropdown.style.display = 'none';
    return dropdown;
}

function renderNotifications(dropdown) {
    if (notifications.length === 0) {
        dropdown.innerHTML = `
            <div class="notification-header">
                <h3>Notifications</h3>
            </div>
            <div class="notification-empty">
                <i class="fas fa-bell-slash"></i>
                <p>No notifications yet</p>
            </div>
        `;
        return;
    }

    const unread = notifications.filter(n => !n.isRead).length;

    dropdown.innerHTML = `
        <div class="notification-header">
            <h3>Notifications</h3>
            ${unread > 0 ? `<button class="mark-all-btn" onclick="window.markAllNotificationsRead()">Mark all read</button>` : ''}
        </div>
        <div class="notification-list">
            ${notifications.map(n => createNotificationHTML(n)).join('')}
        </div>
    `;
}

function createNotificationHTML(n) {
    const timeAgo = getTimeAgo(n.createdAt);
    const readClass = n.isRead ? 'read' : 'unread';
    
    let icon = 'fa-bell';
    let iconColor = '#5b4acf';
    
    if (n.type === 'COURSE_ENROLLMENT') {
        icon = 'fa-book';
        iconColor = '#3b82f6';
    } else if (n.type === 'NEW_CONTENT') {
        icon = 'fa-plus-circle';
        iconColor = '#10b981';
    } else if (n.type === 'GRADE_POSTED') {
        icon = 'fa-star';
        iconColor = '#f59e0b';
    } else if (n.type === 'ANNOUNCEMENT') {
        icon = 'fa-bullhorn';
        iconColor = '#8b5cf6';
    }

    return `
        <div class="notification-item ${readClass}" onclick="window.markNotificationRead(${n.id})">
            <div class="notification-icon" style="background: ${iconColor};">
                <i class="fas ${icon}"></i>
            </div>
            <div class="notification-content">
                <div class="notification-title">${n.title}</div>
                <div class="notification-message">${n.message}</div>
                <div class="notification-time">
                    <i class="fas fa-clock"></i> ${timeAgo}
                </div>
            </div>
            <button class="notification-delete" onclick="event.stopPropagation(); window.deleteNotification(${n.id})">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
}

function getTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);

    if (seconds < 60) return 'Just now';
    if (seconds < 3600) return `${Math.floor(seconds / 60)} min ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`;
    if (seconds < 604800) return `${Math.floor(seconds / 86400)} days ago`;
    
    return date.toLocaleDateString();
}

function startPolling(interval) {
    setInterval(() => {
        loadNotifications();
    }, interval);
}

// Global functions for onclick handlers
window.markNotificationRead = async function(id) {
    try {
        const response = await fetch(`${BASE_URL}/notifications`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ notificationId: id })
        });

        if (response.ok) {
            const notification = notifications.find(n => n.id === id);
            if (notification) notification.isRead = true;
            updateBadge();
            
            const dropdown = document.querySelector('.notification-dropdown');
            if (dropdown && dropdown.style.display === 'block') {
                renderNotifications(dropdown);
            }
        }
    } catch (error) {
        console.error('Error marking as read:', error);
    }
};

window.markAllNotificationsRead = async function() {
    try {
        const response = await fetch(`${BASE_URL}/notifications`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ markAll: true })
        });

        if (response.ok) {
            notifications.forEach(n => n.isRead = true);
            updateBadge();
            
            const dropdown = document.querySelector('.notification-dropdown');
            if (dropdown && dropdown.style.display === 'block') {
                renderNotifications(dropdown);
            }
        }
    } catch (error) {
        console.error('Error marking all as read:', error);
    }
};

window.deleteNotification = async function(id) {
    try {
        const response = await fetch(`${BASE_URL}/notifications`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ notificationId: id })
        });

        if (response.ok) {
            notifications = notifications.filter(n => n.id !== id);
            updateBadge();
            
            const dropdown = document.querySelector('.notification-dropdown');
            if (dropdown && dropdown.style.display === 'block') {
                renderNotifications(dropdown);
            }
        }
    } catch (error) {
        console.error('Error deleting notification:', error);
    }
};