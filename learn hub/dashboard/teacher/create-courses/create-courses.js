import api from '../../../api.js';
const IMAGE_BASE_URL = `http://192.168.100.3:8080/api/`;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }
    const user = JSON.parse(userJson);
    
    // Ensure only Teachers/Admins can access
    if (user.role !== 'TEACHER' && user.role !== 'ADMIN') {
        alert("Access Denied: Teachers only.");
        window.location.href = '../../../index.html';
        return;
    }

    updateUserProfile(user);
    setupEventListeners();
});

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}</span>`;
}

function setupEventListeners() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('thumbnailInput');
    const selectBtn = document.getElementById('selectImageBtn');
    const uploadText = document.getElementById('uploadText');
    const backBtn = document.getElementById('backBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const form = document.getElementById('createCourseForm');
    const createBtn = document.getElementById('createBtn');

    // --- File Upload UI ---
    selectBtn.addEventListener('click', () => fileInput.click());
    uploadArea.addEventListener('click', (e) => {
        if(e.target !== selectBtn) fileInput.click();
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            const fileName = fileInput.files[0].name;
            uploadText.textContent = `Selected: ${fileName}`;
            uploadArea.style.borderColor = '#5b4acf';
            uploadArea.style.backgroundColor = '#f3f4f6';
        }
    });

    // --- Navigation ---
    backBtn.addEventListener('click', () => window.history.back());
    
    cancelBtn.addEventListener('click', () => {
        if (confirm("Discard changes?")) {
            window.location.href = '../../../teacher-dashboard/teacher-dashboard.html';
        }
    });

    // --- Form Submission ---
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const title = document.getElementById('courseTitle').value.trim();
        const description = document.getElementById('courseInfo').value.trim();
        const targetAudience = document.getElementById('targetAudience').value.trim();
        const enrollmentKey = document.getElementById('enrollmentKey').value.trim(); // NEW
        const file = fileInput.files[0];

        if (!title || !enrollmentKey) {
            alert("Course Title and Enrollment Key are required.");
            return;
        }

        createBtn.disabled = true;
        createBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';

        try {
            let thumbnailPath = "";

            // 1. Upload Thumbnail (if selected)
            if (file) {
                const uploadResult = await api.uploadFile(file, 'course_content'); 
                thumbnailPath = uploadResult.filePath;
            }

            // 2. Create Course
            const courseData = {
                title: title,
                description: description,
                targetAudience: targetAudience,
                enrollmentKey: enrollmentKey, // Added to payload
                thumbnailPath: thumbnailPath
            };

            await api.course.create(courseData);

            alert("Course created successfully!");
            window.location.href = '../teacher-dashboard/teacher-dashboard.html';

        } catch (error) {
            console.error(error);
            alert("Failed to create course: " + error.message);
            createBtn.disabled = false;
            createBtn.innerHTML = 'Create Course';
        }
    });
    
    // Logout
    document.getElementById('userProfile').addEventListener('click', async () => {
        if(confirm("Log out?")) {
            try { await api.auth.logout(); } catch(e){}
            localStorage.removeItem('user');
            window.location.href = '../auth/login.html';
        }
    });
}