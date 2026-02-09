import api from '../../../api.js';
import config from '../../../config.js';

const IMAGE_BASE_URL = config.IMAGE_BASE_URL;

let currentUser = null;
let isSearchMode = false;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }
    currentUser = JSON.parse(userJson);
    updateUserProfile(currentUser);

    // 2. Load Data
    await loadMyCourses();
    await loadAvailableCourses();

    // 3. Setup Listeners
    setupEventListeners();
    setupModalListeners();
    setupSearchListeners(); // NEW
});

// --- SEARCH FUNCTIONALITY ---

function setupSearchListeners() {
    const searchInput = document.getElementById('searchInput');
    const clearSearchBtn = document.getElementById('clearSearchBtn');

    if (searchInput) {
        let searchTimeout;
        
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();
            
            // Clear previous timeout
            clearTimeout(searchTimeout);
            
            // If empty, clear search
            if (query === '') {
                clearSearch();
                return;
            }
            
            // Debounce: Wait 500ms after user stops typing
            searchTimeout = setTimeout(() => {
                performSearch(query);
            }, 500);
        });
        
        // Also search on Enter key
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                clearTimeout(searchTimeout);
                const query = e.target.value.trim();
                if (query) performSearch(query);
            }
        });
    }
    
    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', clearSearch);
    }
}

async function performSearch(query) {
    const resultsSection = document.getElementById('searchResultsSection');
    const resultsGrid = document.getElementById('searchResultsGrid');
    const searchQueryText = document.getElementById('searchQuery');
    const myCoursesSection = document.getElementById('myCoursesSection');
    const availableCoursesSection = document.getElementById('availableCoursesSection');
    
    if (!resultsSection || !resultsGrid) return;
    
    // Show search results section, hide others
    isSearchMode = true;
    resultsSection.style.display = 'block';
    myCoursesSection.style.display = 'none';
    availableCoursesSection.style.display = 'none';
    
    // Update search query text
    if (searchQueryText) {
        searchQueryText.textContent = `for "${query}"`;
    }
    
    // Show loading
    resultsGrid.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Searching...</div>';
    
    try {
        const results = await api.course.search(query);
        renderSearchResults(resultsGrid, results);
    } catch (error) {
        console.error('Search error:', error);
        resultsGrid.innerHTML = `
            <div class="error-msg">
                <i class="fas fa-exclamation-circle"></i>
                <p>Search failed. Please try again.</p>
            </div>
        `;
    }
}

function renderSearchResults(container, courses) {
    container.innerHTML = '';
    
    if (!courses || courses.length === 0) {
        container.innerHTML = `
            <div class="empty-msg">
                <i class="fas fa-search" style="font-size: 3rem; color: var(--text-light); margin-bottom: 1rem;"></i>
                <p>No courses found matching your search.</p>
            </div>
        `;
        return;
    }
    
    // Check which courses the user is enrolled in
    // We'll render them with different actions
    courses.forEach(course => {
        const card = document.createElement('div');
        card.className = 'course-card';
        
        // Determine if enrolled (you might need to check this against user's enrolled courses)
        const isEnrolled = checkIfEnrolled(course.id);
        
        let actionBtn = '';
        if (isEnrolled) {
            actionBtn = `<button class="course-btn btn-primary" onclick="window.location.href='../courses-deatils/course-details.html?id=${course.id}'">Continue Learning</button>`;
        } else {
            actionBtn = `<button class="course-btn btn-secondary enroll-trigger" data-id="${course.id}">Enroll Now</button>`;
        }
        
        const thumbnailUrl = course.thumbnailPath 
            ? `${IMAGE_BASE_URL}${course.thumbnailPath}`
            : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=250&fit=crop';
        
        card.innerHTML = `
            <div class="course-image">
                <img src="${thumbnailUrl}" alt="${course.title}" onerror="this.src='https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=250&fit=crop'">
                <div class="course-badge">${isEnrolled ? 'Enrolled' : 'Available'}</div>
            </div>
            <div class="course-body">
                <h3 class="course-title">${course.title || course.name}</h3>
                <p class="course-description">${course.description || 'No description provided.'}</p>
                ${actionBtn}
            </div>
        `;
        container.appendChild(card);
    });
    
    // Attach Click Listeners for "Enroll Now" buttons
    document.querySelectorAll('.enroll-trigger').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const courseId = e.target.getAttribute('data-id');
            openEnrollModal(courseId);
        });
    });
}

function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    const resultsSection = document.getElementById('searchResultsSection');
    const myCoursesSection = document.getElementById('myCoursesSection');
    const availableCoursesSection = document.getElementById('availableCoursesSection');
    
    if (searchInput) searchInput.value = '';
    
    isSearchMode = false;
    
    if (resultsSection) resultsSection.style.display = 'none';
    if (myCoursesSection) myCoursesSection.style.display = 'block';
    if (availableCoursesSection) availableCoursesSection.style.display = 'block';
}

// Helper function to check if user is enrolled
// You might want to cache this data or check against enrolled courses list
function checkIfEnrolled(courseId) {
    // This is a simple check - you might want to improve this
    // by storing enrolled course IDs when loading
    const myCoursesGrid = document.getElementById('myCoursesGrid');
    if (myCoursesGrid) {
        const enrolledCourses = myCoursesGrid.querySelectorAll('.course-card');
        // This is a basic check - improve as needed
        return false; // For now, return false
    }
    return false;
}

// --- EXISTING LOADERS ---

async function loadMyCourses() {
    const grid = document.getElementById('myCoursesGrid');
    if (!grid) return;

    grid.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading courses...</div>';

    try {
        const courses = await api.course.getMyCourses();
        renderCourses(grid, courses, 'my_course');
    } catch (error) {
        console.error(error);
        grid.innerHTML = `<p class="error-msg">Failed to load enrolled courses.</p>`;
    }
}

async function loadAvailableCourses() {
    const grid = document.getElementById('availableCoursesGrid');
    if (!grid) return;

    grid.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading available courses...</div>';

    try {
        const courses = await api.course.getAvailableCourses();
        renderCourses(grid, courses, 'available');
    } catch (error) {
        console.error(error);
        grid.innerHTML = `<p class="error-msg">Failed to load available courses.</p>`;
    }
}

// --- RENDERING ---

function renderCourses(container, courses, type) {
    container.innerHTML = '';

    if (!courses || courses.length === 0) {
        container.innerHTML = `<p class="empty-msg">No courses found.</p>`;
        return;
    }

    courses.forEach(course => {
        const card = document.createElement('div');
        card.className = 'course-card';

        let actionBtn = '';
        if (type === 'my_course') {
            actionBtn = `<button class="course-btn btn-primary" onclick="window.location.href='../courses-deatils/course-details.html?id=${course.id}'">Continue Learning</button>`;
        } else {
            actionBtn = `<button class="course-btn btn-secondary enroll-trigger" data-id="${course.id}">Enroll Now</button>`;
        }
        
        const thumbnailUrl = course.thumbnailPath 
            ? `${IMAGE_BASE_URL}${course.thumbnailPath}`
            : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=250&fit=crop';
        
        card.innerHTML = `
            <div class="course-image">
                <img src="${thumbnailUrl}" alt="${course.title}" onerror="this.src='https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=250&fit=crop'">
                <div class="course-badge">${type === 'my_course' ? 'Enrolled' : 'Open'}</div>
            </div>
            <div class="course-body">
                <h3 class="course-title">${course.title || course.name}</h3>
                <p class="course-description">${course.description || 'No description provided.'}</p>
                ${actionBtn}
            </div>
        `;
        container.appendChild(card);
    });

    if (type === 'available') {
        document.querySelectorAll('.enroll-trigger').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const courseId = e.target.getAttribute('data-id');
                openEnrollModal(courseId);
            });
        });
    }
}

// --- MODAL LOGIC ---

const modal = document.getElementById('enrollModal');
const closeModal = document.querySelector('.close-modal');
const modalCourseIdInput = document.getElementById('modalCourseId');
const modalKeyInput = document.getElementById('modalEnrollmentKey');
const confirmBtn = document.getElementById('confirmEnrollBtn');

function openEnrollModal(courseId) {
    modalCourseIdInput.value = courseId;
    modalKeyInput.value = "";
    modal.style.display = "block";
    modalKeyInput.focus();
}

function closeEnrollModal() {
    modal.style.display = "none";
}

function setupModalListeners() {
    if(closeModal) closeModal.addEventListener('click', closeEnrollModal);

    window.addEventListener('click', (e) => {
        if (e.target === modal) closeEnrollModal();
    });

    if(confirmBtn) {
        confirmBtn.addEventListener('click', async () => {
            const courseId = modalCourseIdInput.value;
            const code = modalKeyInput.value.trim();

            if (!courseId) return;

            const originalText = confirmBtn.textContent;
            confirmBtn.textContent = "Processing...";
            confirmBtn.disabled = true;

            try {
                await api.course.enroll(parseInt(courseId), code);
                
                alert("Successfully enrolled!");
                closeEnrollModal();
                
                // Refresh lists
                if (isSearchMode) {
                    clearSearch();
                }
                loadMyCourses();
                loadAvailableCourses();

            } catch (error) {
                alert("Enrollment failed: " + error.message);
            } finally {
                confirmBtn.textContent = originalText;
                confirmBtn.disabled = false;
            }
        });
    }
}

// --- GENERAL LISTENERS ---

function setupEventListeners() {
    const profileBtn = document.getElementById('userProfile');
    if (profileBtn) {
        profileBtn.addEventListener('click', async () => {
            if (confirm("Log out?")) {
                try { await api.auth.logout(); } catch (e) { console.error(e); }
                localStorage.removeItem('user');
                window.location.href = '../../../auth/login.html';
            }
        });
    }
}

// --- UTILS ---

function updateUserProfile(user) {
    const nameEl = document.getElementById('userName');
    const roleEl = document.getElementById('userRole');
    const welcomeEl = document.getElementById('welcomeMessage');
    const avatarEl = document.getElementById('userAvatar');

    if (nameEl) nameEl.textContent = `${user.firstName} ${user.lastName}`;
    if (roleEl) roleEl.textContent = user.role;
    if (welcomeEl) welcomeEl.textContent = `Welcome back, ${user.firstName}!`;
    if (avatarEl) avatarEl.innerHTML = `<span>${user.firstName.charAt(0)}</span>`;
}