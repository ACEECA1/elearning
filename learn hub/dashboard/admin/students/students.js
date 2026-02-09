import api from '../../../api.js';

let currentUser = null;
let allStudents = [];
let currentEditStudentId = null;
let currentDeleteStudentId = null;

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
    
    await loadStudents();
    
    setupEventListeners();
});

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}

async function loadStudents() {
    const container = document.getElementById('studentsTableContainer');
    
    try {
        allStudents = await api.user.getAllStudents();
        
        if (!allStudents || allStudents.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-user-graduate fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No students yet. Add your first student!</p>
                </div>
            `;
            return;
        }
        
        renderStudentsTable(allStudents);
        
    } catch (error) {
        console.error('Error loading students:', error);
        container.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-circle fa-2x" style="color: #ef4444; margin-bottom: 1rem;"></i>
                <p>Failed to load students</p>
            </div>
        `;
    }
}

function renderStudentsTable(students) {
    const container = document.getElementById('studentsTableContainer');
    
    const table = document.createElement('table');
    table.className = 'students-table';
    
    table.innerHTML = `
        <thead>
            <tr>
                <th>Student</th>
                <th>Academic Year</th>
                <th>Student ID</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody id="studentsTableBody"></tbody>
    `;
    
    container.innerHTML = '';
    container.appendChild(table);
    
    const tbody = document.getElementById('studentsTableBody');
    
    students.forEach(student => {
        const row = createStudentRow(student);
        tbody.appendChild(row);
    });
}

function createStudentRow(student) {
    const row = document.createElement('tr');
    row.className = 'student-row';
    
    const initials = `${student.firstName?.charAt(0) || ''}${student.lastName?.charAt(0) || ''}`;
    const fullName = `${student.firstName || ''} ${student.lastName || ''}`;
    
    row.innerHTML = `
        <td>
            <div class="student-info">
                <div class="student-avatar" style="background: ${getRandomColor(student.id)};">
                    ${initials}
                </div>
                <div class="student-details">
                    <div class="student-name">${fullName}</div>
                    <div class="student-email">${student.email || 'No email'}</div>
                </div>
            </div>
        </td>
        <td>${student.academicYear || '-'}</td>
        <td>${student.studentCardNumber || '-'}</td>
        <td><span class="status-badge ${student.isVerified ? 'active' : 'inactive'}">${student.isVerified ? 'Active' : 'Inactive'}</span></td>
        <td>
            <div class="action-buttons">
                <button class="action-btn edit-btn" title="Edit" data-id="${student.id}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn delete-btn" title="Delete" data-id="${student.id}" data-name="${fullName}">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>
        </td>
    `;
    
    return row;
}

function getRandomColor(id) {
    const colors = ['#5b4acf', '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];
    return colors[id % colors.length] || colors[0];
}

function setupEventListeners() {
    document.getElementById('addStudentBtn').addEventListener('click', () => {
        document.getElementById('addStudentModal').style.display = 'flex';
    });
    
    document.getElementById('addStudentForm').addEventListener('submit', handleAddStudent);
    document.getElementById('editStudentForm').addEventListener('submit', handleEditStudent);
    
    document.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.edit-btn');
        if (editBtn) {
            const studentId = parseInt(editBtn.dataset.id);
            await openEditStudentModal(studentId);
        }
        
        const deleteBtn = e.target.closest('.delete-btn');
        if (deleteBtn) {
            currentDeleteStudentId = parseInt(deleteBtn.dataset.id);
            document.getElementById('deleteStudentName').textContent = deleteBtn.dataset.name;
            document.getElementById('deleteStudentModal').style.display = 'flex';
        }
    });
    
    document.getElementById('confirmDeleteBtn').addEventListener('click', handleDeleteStudent);
    
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
    
    document.getElementById('studentSearch').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = allStudents.filter(student => 
            (student.firstName && student.firstName.toLowerCase().includes(query)) ||
            (student.lastName && student.lastName.toLowerCase().includes(query)) ||
            (student.email && student.email.toLowerCase().includes(query)) ||
            (student.studentCardNumber && student.studentCardNumber.toLowerCase().includes(query))
        );
        renderStudentsTable(filtered);
    });
    
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    const adminSidebar = document.getElementById('adminSidebar');
    
    if (mobileSidebarToggle && adminSidebar) {
        mobileSidebarToggle.addEventListener('click', () => {
            adminSidebar.classList.toggle('active');
        });
    }
}

async function handleAddStudent(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('.btn-save');
    const originalHTML = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';
    
    try {
        const studentData = {
            firstName: document.getElementById('studentFirstName').value.trim(),
            lastName: document.getElementById('studentLastName').value.trim(),
            username: document.getElementById('studentUsername').value.trim(),
            email: document.getElementById('studentEmail').value.trim(),
            password: document.getElementById('studentPassword').value,
            studentCardNumber: document.getElementById('studentCardNumber').value.trim(),
            academicYear: document.getElementById('studentAcademicYear').value.trim()
        };
        
        await api.user.createStudent(studentData);
        
        alert('Student added successfully!');
        closeAddStudentModal();
        await loadStudents();
        
    } catch (error) {
        console.error('Error adding student:', error);
        alert('Failed to add student: ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalHTML;
    }
}

async function openEditStudentModal(studentId) {
    try {
        currentEditStudentId = studentId;
        const student = await api.user.getById(studentId);
        
        document.getElementById('editStudentFirstName').value = student.firstName || '';
        document.getElementById('editStudentLastName').value = student.lastName || '';
        document.getElementById('editStudentUsername').value = student.username || '';
        document.getElementById('editStudentEmail').value = student.email || '';
        document.getElementById('editStudentCardNumber').value = student.studentCardNumber || '';
        document.getElementById('editStudentAcademicYear').value = student.academicYear || '';
        
        document.getElementById('editStudentModal').style.display = 'flex';
        
    } catch (error) {
        console.error('Error loading student details:', error);
        alert('Failed to load student details: ' + error.message);
    }
}

async function handleEditStudent(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('.btn-save');
    const originalHTML = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
    
    try {
        const studentData = {
            userId: currentEditStudentId,
            firstName: document.getElementById('editStudentFirstName').value.trim(),
            lastName: document.getElementById('editStudentLastName').value.trim(),
            username: document.getElementById('editStudentUsername').value.trim(),
            email: document.getElementById('editStudentEmail').value.trim(),
            studentCardNumber: document.getElementById('editStudentCardNumber').value.trim(),
            academicYear: document.getElementById('editStudentAcademicYear').value.trim()
        };
        
        await api.user.updateStudent(studentData);
        
        alert('Student updated successfully!');
        closeEditStudentModal();
        await loadStudents();
        
    } catch (error) {
        console.error('Error updating student:', error);
        alert('Failed to update student: ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalHTML;
    }
}

async function handleDeleteStudent() {
    const btn = document.getElementById('confirmDeleteBtn');
    const originalHTML = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
    
    try {
        await api.user.deleteUser(currentDeleteStudentId);
        
        alert('Student deleted successfully!');
        closeDeleteStudentModal();
        await loadStudents();
        
    } catch (error) {
        console.error('Error deleting student:', error);
        alert('Failed to delete student: ' + error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHTML;
    }
}

window.closeAddStudentModal = function() {
    document.getElementById('addStudentModal').style.display = 'none';
    document.getElementById('addStudentForm').reset();
};

window.closeEditStudentModal = function() {
    document.getElementById('editStudentModal').style.display = 'none';
    document.getElementById('editStudentForm').reset();
    currentEditStudentId = null;
};

window.closeDeleteStudentModal = function() {
    document.getElementById('deleteStudentModal').style.display = 'none';
    currentDeleteStudentId = null;
};
