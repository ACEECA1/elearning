import api from '../../../api.js';
import config from '../../../config.js';
const IMAGE_BASE_URL = config.IMAGE_BASE_URL;
let courseToDeleteId = null;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../auth/login.html';
        return;
    }
    const user = JSON.parse(userJson);
    updateUserProfile(user);

    // 2. Load Courses
    await loadTeacherCourses();

    // 3. Setup Global Listeners
    setupEventListeners();
});

async function loadTeacherCourses() {
    const grid = document.getElementById('coursesGrid');
    try {
        // Fetch courses where current user is the teacher
        const courses = await api.course.getMyCoursesTeacher();
        grid.innerHTML = '';

        if (!courses || courses.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 3rem; background: #fff; border-radius: 12px;">
                    <p>You haven't created any courses yet.</p>
                    <button class="create-course-btn" style="margin-top: 1rem;" onclick="window.location.href='../create-courses/create-courses.html'">
                        Create your first course
                    </button>
                </div>`;
            return;
        }

        courses.forEach(course => {
            const card = document.createElement('div');
            card.className = 'course-card';
            
            const thumbSrc = course.thumbnailPath 
                ? (course.thumbnailPath.startsWith('http') ? course.thumbnailPath : IMAGE_BASE_URL + course.thumbnailPath)
                : 'https://via.placeholder.com/400x250?text=No+Thumbnail';

            card.innerHTML = `
                <div class="course-image-container">
                    <img src="${thumbSrc}" class="course-image" style="width:100%; height:100%; object-fit:cover;" onerror="this.src='https://via.placeholder.com/400x250?text=Error'">
                    <div class="course-badge-overlay">${course.targetAudience || 'General'}</div>
                </div>
                <div class="course-card-body">
                    <h3 class="course-card-title">${course.title}</h3>
                    <div class="course-card-meta">
                        <span class="course-status published">Enrollment Key: ${course.enrollmentKey || 'None'}</span>
                    </div>
                    <div class="course-card-actions">
                        <button class="action-icon-btn edit-btn" title="Edit Course" data-id="${course.id}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-icon-btn delete-btn" title="Delete Course" data-id="${course.id}" data-title="${course.title}">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                        <button class="view-course-btn" onclick="window.location.href='../courses-details/course-details.html?id=${course.id}'">Manage Content</button>
                    </div>
                </div>
            `;
            grid.appendChild(card);
        });

    } catch (error) {
        console.error("Load Error:", error);
        grid.innerHTML = `<p style="color:red; grid-column:1/-1; text-align:center;">Error: ${error.message}</p>`;
    }
}

function setupEventListeners() {
    const grid = document.getElementById('coursesGrid');
    const modal = document.getElementById('deleteModal');

    // Delegation for dynamic buttons
    grid.addEventListener('click', (e) => {
        const deleteBtn = e.target.closest('.delete-btn');
        const editBtn = e.target.closest('.edit-btn');

        if (deleteBtn) {
            courseToDeleteId = deleteBtn.dataset.id;
            document.getElementById('deleteCourseName').textContent = deleteBtn.dataset.title;
            modal.style.display = 'flex';
        }

        if (editBtn) {
            alert("Edit functionality would go to a specialized edit page for Course ID: " + editBtn.dataset.id);
        }
    });

    // Modal Listeners
    document.getElementById('modalClose').onclick = closeModal;
    document.getElementById('cancelDelete').onclick = closeModal;
    document.getElementById('confirmDelete').onclick = async () => {
        if (!courseToDeleteId) return;

        try {
            // Note: Ensure your CourseServlet supports DELETE /api/course?courseId=...
            await api.request(`/course?courseId=${courseToDeleteId}`, 'DELETE');
            alert("Course deleted successfully.");
            closeModal();
            loadTeacherCourses(); // Refresh
        } catch (error) {
            alert("Delete failed: " + error.message);
        }
    };

    // Logout
    document.getElementById('userProfile').onclick = async () => {
        if (confirm("Log out?")) {
            try { await api.auth.logout(); } catch (e) {}
            localStorage.removeItem('user');
            window.location.href = '../auth/login.html';
        }
    };
}

function closeModal() {
    document.getElementById('deleteModal').style.display = 'none';
    courseToDeleteId = null;
}

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `Prof. ${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}