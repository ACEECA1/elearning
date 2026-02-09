import api from '../../../api.js';

let currentUser = null;
let allTeachers = [];
let currentEditTeacherId = null;
let currentDeleteTeacherId = null;

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
    
    setupEventListeners();
});

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}

async function loadTeachers() {
    const container = document.getElementById('teachersTableContainer');
    
    try {
        allTeachers = await api.user.getAllTeachers();
        
        if (!allTeachers || allTeachers.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-chalkboard-teacher fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No teachers yet. Add your first teacher!</p>
                </div>
            `;
            return;
        }
        
        renderTeachersTable(allTeachers);
        
    } catch (error) {
        console.error('Error loading teachers:', error);
        container.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-circle fa-2x" style="color: #ef4444; margin-bottom: 1rem;"></i>
                <p>Failed to load teachers</p>
            </div>
        `;
    }
}

function renderTeachersTable(teachers) {
    const container = document.getElementById('teachersTableContainer');
    
    const table = document.createElement('table');
    table.className = 'teachers-table';
    
    table.innerHTML = `
        <thead>
            <tr>
                <th>Teacher</th>
                <th>Department</th>
                <th>Grade</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody id="teachersTableBody"></tbody>
    `;
    
    container.innerHTML = '';
    container.appendChild(table);
    
    const tbody = document.getElementById('teachersTableBody');
    
    teachers.forEach(teacher => {
        const row = createTeacherRow(teacher);
        tbody.appendChild(row);
    });
}

function createTeacherRow(teacher) {
    const row = document.createElement('tr');
    row.className = 'teacher-row';
    
    const initials = `${teacher.firstName?.charAt(0) || ''}${teacher.lastName?.charAt(0) || ''}`;
    const fullName = `${teacher.firstName || ''} ${teacher.lastName || ''}`;
    
    row.innerHTML = `
        <td>
            <div class="teacher-info">
                <div class="teacher-avatar" style="background: ${getRandomColor(teacher.id)};">
                    ${initials}
                </div>
                <div class="teacher-details">
                    <div class="teacher-name">${fullName}</div>
                    <div class="teacher-email">${teacher.email || 'No email'}</div>
                </div>
            </div>
        </td>
        <td>${teacher.domain || '-'}</td>
        <td>${teacher.grade || '-'}</td>
        <td><span class="status-badge ${teacher.isVerified ? 'active' : 'inactive'}">${teacher.isVerified ? 'Active' : 'Inactive'}</span></td>
        <td>
            <div class="action-buttons">
                <button class="action-btn edit-btn" title="Edit" data-id="${teacher.id}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn delete-btn" title="Delete" data-id="${teacher.id}" data-name="${fullName}">
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
    document.getElementById('addTeacherBtn').addEventListener('click', () => {
        document.getElementById('addTeacherModal').style.display = 'flex';
    });
    
    document.getElementById('addTeacherForm').addEventListener('submit', handleAddTeacher);
    document.getElementById('editTeacherForm').addEventListener('submit', handleEditTeacher);
    
    document.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.edit-btn');
        if (editBtn) {
            const teacherId = parseInt(editBtn.dataset.id);
            await openEditTeacherModal(teacherId);
        }
        
        const deleteBtn = e.target.closest('.delete-btn');
        if (deleteBtn) {
            currentDeleteTeacherId = parseInt(deleteBtn.dataset.id);
            document.getElementById('deleteTeacherName').textContent = deleteBtn.dataset.name;
            document.getElementById('deleteTeacherModal').style.display = 'flex';
        }
    });
    
    document.getElementById('confirmDeleteBtn').addEventListener('click', handleDeleteTeacher);
    
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
    
    document.getElementById('teacherSearch').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = allTeachers.filter(teacher => 
            (teacher.firstName && teacher.firstName.toLowerCase().includes(query)) ||
            (teacher.lastName && teacher.lastName.toLowerCase().includes(query)) ||
            (teacher.email && teacher.email.toLowerCase().includes(query)) ||
            (teacher.domain && teacher.domain.toLowerCase().includes(query))
        );
        renderTeachersTable(filtered);
    });
    
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    const adminSidebar = document.getElementById('adminSidebar');
    
    if (mobileSidebarToggle && adminSidebar) {
        mobileSidebarToggle.addEventListener('click', () => {
            adminSidebar.classList.toggle('active');
        });
    }
}

async function handleAddTeacher(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('.btn-save');
    const originalHTML = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';
    
    try {
        const teacherData = {
            firstName: document.getElementById('teacherFirstName').value.trim(),
            lastName: document.getElementById('teacherLastName').value.trim(),
            username: document.getElementById('teacherUsername').value.trim(),
            email: document.getElementById('teacherEmail').value.trim(),
            password: document.getElementById('teacherPassword').value,
            domain: document.getElementById('teacherDomain').value.trim() || null,
            grade: document.getElementById('teacherGrade').value.trim() || null
        };
        
        await api.user.createTeacher(teacherData);
        
        alert('Teacher added successfully!');
        closeAddTeacherModal();
        await loadTeachers();
        
    } catch (error) {
        console.error('Error adding teacher:', error);
        alert('Failed to add teacher: ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalHTML;
    }
}

async function openEditTeacherModal(teacherId) {
    try {
        currentEditTeacherId = teacherId;
        const teacher = await api.user.getById(teacherId);
        
        document.getElementById('editTeacherFirstName').value = teacher.firstName || '';
        document.getElementById('editTeacherLastName').value = teacher.lastName || '';
        document.getElementById('editTeacherUsername').value = teacher.username || '';
        document.getElementById('editTeacherEmail').value = teacher.email || '';
        document.getElementById('editTeacherDomain').value = teacher.domain || '';
        document.getElementById('editTeacherGrade').value = teacher.grade || '';
        
        document.getElementById('editTeacherModal').style.display = 'flex';
        
    } catch (error) {
        console.error('Error loading teacher details:', error);
        alert('Failed to load teacher details: ' + error.message);
    }
}

async function handleEditTeacher(e) {
    e.preventDefault();
    
    const submitBtn = e.target.querySelector('.btn-save');
    const originalHTML = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
    
    try {
        const teacherData = {
            userId: currentEditTeacherId,
            firstName: document.getElementById('editTeacherFirstName').value.trim(),
            lastName: document.getElementById('editTeacherLastName').value.trim(),
            username: document.getElementById('editTeacherUsername').value.trim(),
            email: document.getElementById('editTeacherEmail').value.trim(),
            domain: document.getElementById('editTeacherDomain').value.trim() || null,
            grade: document.getElementById('editTeacherGrade').value.trim() || null
        };
        
        await api.user.updateTeacher(teacherData);
        
        alert('Teacher updated successfully!');
        closeEditTeacherModal();
        await loadTeachers();
        
    } catch (error) {
        console.error('Error updating teacher:', error);
        alert('Failed to update teacher: ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalHTML;
    }
}

async function handleDeleteTeacher() {
    const btn = document.getElementById('confirmDeleteBtn');
    const originalHTML = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
    
    try {
        await api.user.deleteUser(currentDeleteTeacherId);
        
        alert('Teacher deleted successfully!');
        closeDeleteTeacherModal();
        await loadTeachers();
        
    } catch (error) {
        console.error('Error deleting teacher:', error);
        alert('Failed to delete teacher: ' + error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHTML;
    }
}

window.closeAddTeacherModal = function() {
    document.getElementById('addTeacherModal').style.display = 'none';
    document.getElementById('addTeacherForm').reset();
};

window.closeEditTeacherModal = function() {
    document.getElementById('editTeacherModal').style.display = 'none';
    document.getElementById('editTeacherForm').reset();
    currentEditTeacherId = null;
};

window.closeDeleteTeacherModal = function() {
    document.getElementById('deleteTeacherModal').style.display = 'none';
    currentDeleteTeacherId = null;
};
