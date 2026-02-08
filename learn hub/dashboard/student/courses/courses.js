import api from '../../../api.js';
const IMAGE_BASE_URL = `http://192.168.100.3:8080/api/`;
document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../auth/login.html';
        return;
    }
    const user = JSON.parse(userJson);
    updateUserProfile(user);

    // 2. Load Data
    await loadMyCourses();
    await loadAvailableCourses();

    // 3. Setup Listeners
    setupEventListeners();
    setupModalListeners(); // <--- New Function
});

// --- LOADERS ---

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

        // LOGIC: 
        // If type is 'my_course', we show "Continue Learning" (Enter).
        // If type is 'available', we show "Enroll Now" (Open Modal).
        
        let actionBtn = '';
        if (type === 'my_course') {
            actionBtn = `<button class="course-btn btn-primary" onclick="window.location.href='../courses-deatils/course-details.html?id=${course.id}'">Continue Learning</button>`;
        } else {
            // We use data attributes to pass ID to the click handler
            actionBtn = `<button class="course-btn btn-secondary enroll-trigger" data-id="${course.id}">Enroll Now</button>`;
        }
        console.log("Course Thumbnail Path:", course.thumbnailPath);
        card.innerHTML = `
            <div class="course-image">
                <img src= "${IMAGE_BASE_URL + course.thumbnailPath}" alt="${course.title}" ">
                <div class="course-badge">${type === 'my_course' ? 'Enrolled' : 'Open'}</div>
            </div>
            <div class="course-body">
                <div class="course-meta">ID: ${course.id}</div>
                <h3 class="course-title">${course.title || course.name}</h3>
                <p class="course-description">${course.description || 'No description provided.'}</p>
                ${actionBtn}
            </div>
        `;
        container.appendChild(card);
    });

    // Attach Click Listeners for "Enroll Now" buttons
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
    modalKeyInput.value = ""; // Clear previous input
    modal.style.display = "block";
    modalKeyInput.focus();
}

function closeEnrollModal() {
    modal.style.display = "none";
}

function setupModalListeners() {
    // Close on X click
    if(closeModal) closeModal.addEventListener('click', closeEnrollModal);

    // Close on outside click
    window.addEventListener('click', (e) => {
        if (e.target === modal) closeEnrollModal();
    });

    // Confirm Enrollment
    if(confirmBtn) {
        confirmBtn.addEventListener('click', async () => {
            const courseId = modalCourseIdInput.value;
            const code = modalKeyInput.value.trim();

            if (!courseId) return;

            // Optional: If you want to force a code, check `if(!code) ...` here. 
            // Currently it allows empty code if the course is public.

            const originalText = confirmBtn.textContent;
            confirmBtn.textContent = "Processing...";
            confirmBtn.disabled = true;

            try {
                await api.course.enroll(parseInt(courseId), code);
                
                alert("Successfully enrolled!");
                closeEnrollModal();
                
                // Refresh both lists
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
    // Logout
    const profileBtn = document.getElementById('userProfile');
    if (profileBtn) {
        profileBtn.addEventListener('click', async () => {
            if (confirm("Log out?")) {
                try { await api.auth.logout(); } catch (e) { console.error(e); }
                localStorage.removeItem('user');
                window.location.href = '../../auth/login.html';
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

function getRandomColor() {
    const colors = ['#5b4acf', '#10b981', '#f59e0b', '#ef4444', '#3b82f6'];
    return colors[Math.floor(Math.random() * colors.length)];
}