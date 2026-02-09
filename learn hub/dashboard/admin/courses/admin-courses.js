import api from '../../../api.js';
import config from '../../../config.js';

let IMAGE_BASE_URL = config.IMAGE_BASE_URL;


let currentUser = null;
let allCourses = [];
let allTeachers = [];
let currentEditCourseId = null;
let currentDeleteCourseId = null;

document.addEventListener("DOMContentLoaded", async () => {
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }

    currentUser = JSON.parse(userJson);
    
    if (currentUser.role !== 'ADMIN') {
        alert('Access denied. Admin privileges required.');
        window.location.href = '../../../index.html';
        return;
    }

    updateUserProfile(currentUser);
    
    await loadTeachers();
    await loadCourses();
    
    setupEventListeners();
});

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}

async function loadTeachers() {
    try {
        allTeachers = await api.user.getAllTeachers();
    } catch (error) {
        console.error('Error loading teachers:', error);
        allTeachers = [];
    }
}

async function loadCourses() {
    const container = document.getElementById('coursesTableContainer');
    
    try {
        allCourses = await api.course.getAllCourses();
        
        if (!allCourses || allCourses.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-book-open fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No courses yet. Create your first course!</p>
                </div>
            `;
            return;
        }
        
        renderCoursesTable(allCourses);
        
    } catch (error) {
        console.error('Error loading courses:', error);
        container.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-circle fa-2x" style="color: #ef4444; margin-bottom: 1rem;"></i>
                <p>Failed to load courses</p>
            </div>
        `;
    }
}

function renderCoursesTable(courses) {
    const container = document.getElementById('coursesTableContainer');
    
    const table = document.createElement('table');
    table.className = 'courses-table';
    
    table.innerHTML = `
        <thead>
            <tr>
                <th>Course Title</th>
                <th>Instructor</th>
                <th>Students</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody id="coursesTableBody"></tbody>
    `;
    
    container.innerHTML = '';
    container.appendChild(table);
    
    const tbody = document.getElementById('coursesTableBody');
    
    courses.forEach(course => {
        const row = createCourseRow(course);
        tbody.appendChild(row);
    });
}

function createCourseRow(course) {
    const row = document.createElement('tr');
    row.className = 'course-row';
    
    const teacher = allTeachers.find(t => t.id === course.teacherId);
    const teacherName = teacher ? `${teacher.firstName} ${teacher.lastName}` : 'Unknown';
    
    const thumbnailUrl = course.thumbnailPath 
        ? `${IMAGE_BASE_URL}${course.thumbnailPath}`
        : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=100&h=60&fit=crop';
    
    const createdDate = course.createdAt ? new Date(course.createdAt).toLocaleDateString() : '-';
    
    row.innerHTML = `
        <td>
            <div class="course-info">
                <img src="${thumbnailUrl}" alt="Course" class="course-thumbnail" onerror="this.src='https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=100&h=60&fit=crop'">
                <div class="course-details">
                    <div class="course-name">${course.title}</div>
                    <div class="course-category">${course.targetAudience || 'General'}</div>
                </div>
            </div>
        </td>
        <td>${teacherName}</td>
        <td>${course.enrolledCount || 0}</td>
        <td>${createdDate}</td>
        <td>
            <div class="action-buttons">
                <button class="action-btn edit-btn" title="Edit" data-id="${course.id}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn delete-btn" title="Delete" data-id="${course.id}" data-name="${course.title}">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>
        </td>
    `;
    
    return row;
}

function setupEventListeners() {
    document.getElementById('createCourseBtn').addEventListener('click', () => {
        document.getElementById('createCourseModal').style.display = 'flex';
    });
    
    document.getElementById('createCourseForm').addEventListener('submit', handleCreateCourse);
    document.getElementById('editCourseForm').addEventListener('submit', handleEditCourse);
    
    document.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.edit-btn');
        if (editBtn) {
            const courseId = parseInt(editBtn.dataset.id);
            await openEditCourseModal(courseId);
        }
        
        const deleteBtn = e.target.closest('.delete-btn');
        if (deleteBtn) {
            currentDeleteCourseId = parseInt(deleteBtn.dataset.id);
            document.getElementById('deleteCourseName').textContent = deleteBtn.dataset.name;
            document.getElementById('deleteCourseModal').style.display = 'flex';
        }
    });
    
    document.getElementById('confirmDeleteBtn').addEventListener('click', handleDeleteCourse);
    
    document.getElementById('userProfile').addEventListener('click', async () => {
        if (confirm("Log out?")) {
            try {
                await api.auth.logout();
            } catch (e) {
                console.error('Logout error:', e);
            }
            localStorage.removeItem('user');
            window.location.href = '../../../auth/login.html';
        }
    });
    
    document.getElementById('courseSearch').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = allCourses.filter(course => 
            course.title.toLowerCase().includes(query) ||
            (course.targetAudience && course.targetAudience.toLowerCase().includes(query))
        );
        renderCoursesTable(filtered);
    });
    
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    const adminSidebar = document.getElementById('adminSidebar');
    
    if (mobileSidebarToggle && adminSidebar) {
        mobileSidebarToggle.addEventListener('click', () => {
            adminSidebar.classList.toggle('active');
        });
    }
}

async function handleCreateCourse(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('.btn-save');
    const originalHTML = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';
    
    try {
        let thumbnailPath = '';
        const thumbnailFile = document.getElementById('courseThumbnail').files[0];
        
        if (thumbnailFile) {
            const uploadResult = await api.uploadFile(thumbnailFile, 'course_content');
            thumbnailPath = uploadResult.filePath;
        }
        
        const courseData = {
            title: document.getElementById('courseTitle').value.trim(),
            description: document.getElementById('courseDescription').value.trim(),
            targetAudience: document.getElementById('courseTargetAudience').value.trim() || null,
            thumbnailPath: thumbnailPath,
            enrollmentKey: document.getElementById('courseEnrollmentKey').value.trim() || null
        };
        console.log('Creating course with data:', courseData);
        await api.course.create(courseData);
        
        alert('Course created successfully!');
        closeCreateCourseModal();
        await loadCourses();
        
    } catch (error) {
        console.error('Error creating course:', error);
        alert('Failed to create course: ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalHTML;
    }
}

async function openEditCourseModal(courseId) {
    try {
        currentEditCourseId = courseId;
        const course = await api.course.getDetails(courseId);
        
        document.getElementById('editCourseTitle').value = course.title || '';
        document.getElementById('editCourseDescription').value = course.description || '';
        document.getElementById('editCourseTargetAudience').value = course.targetAudience || '';
        document.getElementById('editCourseEnrollmentKey').value = course.enrollmentKey || '';
        
        document.getElementById('editCourseModal').style.display = 'flex';
        
    } catch (error) {
        console.error('Error loading course details:', error);
        alert('Failed to load course details: ' + error.message);
    }
}

async function handleEditCourse(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('.btn-save');
    const originalHTML = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
    
    try {
        const course = await api.course.getDetails(currentEditCourseId);
        let thumbnailPath = course.thumbnailPath;
        
        const thumbnailFile = document.getElementById('editCourseThumbnail').files[0];
        if (thumbnailFile) {
            const uploadResult = await api.uploadFile(thumbnailFile, 'course_content');
            thumbnailPath = uploadResult.filePath;
        }
        
        const courseData = {
            courseId: currentEditCourseId,
            title: document.getElementById('editCourseTitle').value.trim(),
            description: document.getElementById('editCourseDescription').value.trim(),
            targetAudience: document.getElementById('editCourseTargetAudience').value.trim() || null,
            thumbnailPath: thumbnailPath,
            enrollmentKey: document.getElementById('editCourseEnrollmentKey').value.trim() || null
        };
        
        await api.course.update(courseData);
        
        alert('Course updated successfully!');
        closeEditCourseModal();
        await loadCourses();
        
    } catch (error) {
        console.error('Error updating course:', error);
        alert('Failed to update course: ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalHTML;
    }
}

async function handleDeleteCourse() {
    const btn = document.getElementById('confirmDeleteBtn');
    const originalHTML = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
    
    try {
        await api.course.delete(currentDeleteCourseId);
        
        alert('Course deleted successfully!');
        closeDeleteCourseModal();
        await loadCourses();
        
    } catch (error) {
        console.error('Error deleting course:', error);
        alert('Failed to delete course: ' + error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHTML;
    }
}

window.closeCreateCourseModal = function() {
    document.getElementById('createCourseModal').style.display = 'none';
    document.getElementById('createCourseForm').reset();
};

window.closeEditCourseModal = function() {
    document.getElementById('editCourseModal').style.display = 'none';
    document.getElementById('editCourseForm').reset();
    currentEditCourseId = null;
};

window.closeDeleteCourseModal = function() {
    document.getElementById('deleteCourseModal').style.display = 'none';
    currentDeleteCourseId = null;
};
