import api from '../../../api.js'; 

const IMAGE_BASE_URL = `http://192.168.100.3:8080/api/`;

const urlParams = new URLSearchParams(window.location.search);
const courseId = urlParams.get('id');

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../auth/login.html';
        return;
    }
    const user = JSON.parse(userJson);
    updateUserProfile(user);

    if (!courseId) {
        alert("No course ID specified.");
        window.location.href = '../courses/courses.html';
        return;
    }

    // 2. Initialize
    try {
        await loadCourseDetails(courseId);
        await loadModules(courseId);
        
        setupModuleListeners();
        setupLogoutListener();

    } catch (error) {
        console.error("Critical Error:", error);
    }
});

// --- DATA LOADING ---

async function loadCourseDetails(id) {
    try {
        const course = await api.course.getDetails(id);
        
        safeSetText('courseTitle', course.title);
        safeSetText('courseDescription', course.description);
        safeSetText('courseCategory', course.targetAudience);
        safeSetText('courseIdDisplay', `ID: ${course.id}`);

        let teacher = course.teacher;
        if (!teacher && course.teacherId) {
            teacher = await api.user.getById(course.teacherId).catch(e => console.warn(e));
        }

        if (teacher) {
            safeSetText('instructorName', `${teacher.firstName} ${teacher.lastName}`);
            safeSetText('instructorEmail', teacher.email);
            const img = document.getElementById('instructorImage');
            if (img) {
                img.src = `https://ui-avatars.com/api/?name=${teacher.firstName}+${teacher.lastName}&background=random`;
                img.style.display = 'block';
            }
        }
    } catch (error) {
        console.error("Failed to load details", error);
    }
}

async function loadModules(id) {
    const container = document.getElementById('modulesList');
    const countLabel = document.getElementById('moduleCount');

    try {
        const modules = await api.module.getByCourse(id);
        
        if (countLabel) countLabel.textContent = `${modules.length} Modules`;
        container.innerHTML = '';

        if (!modules || modules.length === 0) {
            container.innerHTML = '<p style="text-align:center; padding:1rem;">No modules found.</p>';
            return;
        }

        modules.forEach(mod => {
            let thumbSrc = mod.thumbnailPath || "";
            if (thumbSrc && !thumbSrc.startsWith('http')) {
                thumbSrc = IMAGE_BASE_URL + thumbSrc;
            } else if (!thumbSrc) {
                thumbSrc = 'https://via.placeholder.com/80?text=Module';
            }

            const div = document.createElement('div');
            div.className = 'module-item';
            div.innerHTML = `
                <div class="module-header">
                    <div style="display:flex; align-items:center; gap:15px;">
                        <img class="module-icon" src="${thumbSrc}" style="width:50px; height:50px; border-radius:5px; object-fit:cover;" onerror="this.style.display='none'">
                        <div class="module-info">
                            <h3 class="module-title">${mod.title}</h3>
                            <p class="module-description">${mod.description || ''}</p>
                        </div>
                    </div>
                    <button class="module-toggle-btn" data-module-id="${mod.id}">View</button>
                </div>
                <div class="module-content" id="module-content-${mod.id}" style="display: none;">
                    <div class="loading-chapters" style="padding:1rem; text-align:center;">
                        <i class="fas fa-spinner fa-spin"></i> Loading chapters...
                    </div>
                </div>
            `;
            container.appendChild(div);
        });

    } catch (error) {
        console.error("Failed to load modules", error);
        if(container) container.innerHTML = `<p class="error">Error: ${error.message}</p>`;
    }
}

// --- INTERACTION ---

function setupModuleListeners() {
    const container = document.getElementById('modulesList');
    if (!container) return;

    container.addEventListener('click', (e) => {
        const btn = e.target.closest('.module-toggle-btn');
        if (btn) {
            const moduleId = btn.getAttribute('data-module-id');
            toggleModule(moduleId, btn);
        }
    });
}

async function toggleModule(moduleId, btn) {
    const contentDiv = document.getElementById(`module-content-${moduleId}`);
    if (!contentDiv) return;

    const isHidden = contentDiv.style.display === 'none';

    if (isHidden) {
        contentDiv.style.display = 'block';
        btn.textContent = 'Hide';
        btn.style.backgroundColor = '#e5e7eb';
        btn.style.color = '#333';

        if (contentDiv.getAttribute('data-loaded') !== 'true') {
            await loadChapters(moduleId, contentDiv);
        }
    } else {
        contentDiv.style.display = 'none';
        btn.textContent = 'View';
        btn.style.backgroundColor = '';
        btn.style.color = '';
    }
}

async function loadChapters(moduleId, container) {
    try {
        const chapters = await api.chapter.getByModule(moduleId);
        container.innerHTML = ''; 

        if (!Array.isArray(chapters) || chapters.length === 0) {
            container.innerHTML = '<div style="padding:1rem; color:#666; font-style:italic;">No chapters found.</div>';
            return;
        }

        chapters.forEach((chap, idx) => {
            const row = document.createElement('div');
            row.className = 'lesson-item';
            
            // Flex container
            row.style.display = 'flex';
            row.style.justifyContent = 'space-between';
            row.style.alignItems = 'center';
            row.style.padding = '10px';
            row.style.borderBottom = '1px solid #eee';

            // Left side
            const leftDiv = document.createElement('div');
            leftDiv.className = 'lesson-info';
            leftDiv.innerHTML = `<i class="fas fa-file-alt" style="margin-right:10px; color:#6b5dd8;"></i>`;
            
            const titleSpan = document.createElement('span');
            titleSpan.className = 'lesson-title';
            titleSpan.textContent = `Chapter ${idx + 1}: ${chap.title}`;
            leftDiv.appendChild(titleSpan);

            // Right side
            const rightDiv = document.createElement('div');
            rightDiv.className = 'lesson-meta';
            
            const readBtn = document.createElement('button');
            readBtn.textContent = 'Read';
            readBtn.style.padding = '5px 15px';
            readBtn.style.background = '#5b4acf';
            readBtn.style.color = 'white';
            readBtn.style.border = 'none';
            readBtn.style.borderRadius = '4px';
            readBtn.style.cursor = 'pointer';

            // --- REDIRECT TO CHAPTER PAGE ---
            readBtn.onclick = () => {
                window.location.href = `../chapter/chapter.html?id=${chap.id}`;
            };

            rightDiv.appendChild(readBtn);
            row.appendChild(leftDiv);
            row.appendChild(rightDiv);
            container.appendChild(row);
        });

        container.setAttribute('data-loaded', 'true');

    } catch (error) {
        console.error("Error loading chapters:", error);
        container.innerHTML = `<p style="color:red; padding:1rem;">Error loading chapters.</p>`;
    }
}

// --- UTILS ---

function safeSetText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text || '';
}

function updateUserProfile(user) {
    safeSetText('userName', `${user.firstName} ${user.lastName}`);
    safeSetText('userRole', user.role);
    const av = document.getElementById('userAvatar');
    if(av) av.innerHTML = `<span>${(user.firstName || 'U').charAt(0)}</span>`;
}

function setupLogoutListener() {
    const btn = document.getElementById('userProfile');
    if(btn) btn.addEventListener('click', async () => {
        if(confirm("Log out?")) {
            try { await api.auth.logout(); } catch(e){}
            localStorage.removeItem('user');
            window.location.href = '../../auth/login.html';
        }
    });
}