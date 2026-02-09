import api from '../../../api.js';

let currentUser = null;

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
    
    await loadDashboardStats();
    await loadRecentRegistrations();
    
    setupEventListeners();
});

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('adminNameSubtitle').textContent = user.firstName;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}

async function loadDashboardStats() {
    try {
        const studentsResponse = await api.user.getStudentsCount();
        const teachersResponse = await api.user.getTeachersCount();
        const coursesResponse = await api.course.getMyCoursesTeacher();
        console.log('Dashboard Stats:', { studentsResponse, teachersResponse, coursesResponse });
        const studentsCount = studentsResponse.studentCount ;
        const teachersCount = teachersResponse.teacherCount ;
        const coursesCount = Array.isArray(coursesResponse) ? coursesResponse.length : 0;
        console.log('Parsed Counts:', { studentsCount, teachersCount, coursesCount });
        document.getElementById('totalStudents').textContent = studentsCount;
        document.getElementById('totalTeachers').textContent = teachersCount;
        document.getElementById('totalCourses').textContent = coursesCount;
        
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
        document.getElementById('totalStudents').textContent = '0';
        document.getElementById('totalTeachers').textContent = '0';
        document.getElementById('totalCourses').textContent = '0';
    }
}

async function loadRecentRegistrations() {
    const container = document.getElementById('registrationsList');
    
    try {
        const students = await api.user.getAllStudents();
        
        container.innerHTML = '';
        
        if (!students || students.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-user-slash fa-2x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No recent registrations</p>
                </div>
            `;
            return;
        }
        
        const sortedStudents = students.sort((a, b) => {
            const dateA = new Date(a.createdAt || 0);
            const dateB = new Date(b.createdAt || 0);
            return dateB - dateA;
        });
        
        const recentStudents = sortedStudents.slice(0, 5);
        
        recentStudents.forEach(student => {
            const item = createRegistrationItem(student);
            container.appendChild(item);
        });
        
    } catch (error) {
        console.error('Error loading registrations:', error);
        container.innerHTML = `
            <div class="error-state">
                <i class="fas fa-exclamation-circle fa-2x" style="color: #ef4444; margin-bottom: 1rem;"></i>
                <p>Failed to load registrations</p>
            </div>
        `;
    }
}

function createRegistrationItem(student) {
    const item = document.createElement('div');
    item.className = 'registration-item';
    
    const initials = `${student.firstName?.charAt(0) || ''}${student.lastName?.charAt(0) || ''}`;
    const fullName = `${student.firstName || ''} ${student.lastName || ''}`;
    const email = student.email || 'No email';
    
    const isRecent = isRecentRegistration(student.createdAt);
    
    item.innerHTML = `
        <div class="registration-avatar" style="background: ${getRandomColor(student.id)}; color: white;">
            ${initials || '<i class="fas fa-user"></i>'}
        </div>
        <div class="registration-info">
            <div class="registration-name">${fullName}</div>
            <div class="registration-email">${email}</div>
        </div>
        ${isRecent ? '<span class="registration-badge new-badge">New</span>' : ''}
    `;
    
    return item;
}

function isRecentRegistration(createdAt) {
    if (!createdAt) return false;
    
    const created = new Date(createdAt);
    const now = new Date();
    const diffDays = (now - created) / (1000 * 60 * 60 * 24);
    
    return diffDays <= 7;
}

function getRandomColor(id) {
    const colors = ['#5b4acf', '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];
    return colors[id % colors.length] || colors[0];
}

function setupEventListeners() {
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
    
    const mobileSidebarToggle = document.getElementById('mobileSidebarToggle');
    const adminSidebar = document.getElementById('adminSidebar');
    
    if (mobileSidebarToggle && adminSidebar) {
        mobileSidebarToggle.addEventListener('click', () => {
            adminSidebar.classList.toggle('active');
        });
    }
    
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const query = searchInput.value.trim();
                if (query) {
                    console.log('Search query:', query);
                }
            }
        });
    }
}
