import api from '../../../api.js';

const IMAGE_BASE_URL = `http://192.168.100.3:8080/api/`;
let currentCourseId = null;
let currentCourse = null;
let moduleToDelete = null;
let chapterToDelete = null;
let moduleToEdit = null;
let currentModuleData = null;
let moduleToAddChapter = null; // Store module ID for adding chapter

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }
    const user = JSON.parse(userJson);
    updateUserProfile(user);

    // 2. Get Course ID from URL
    const params = new URLSearchParams(window.location.search);
    currentCourseId = params.get('id');
    
    if (!currentCourseId) {
        alert("No course ID specified");
        window.location.href = '../teacher-dashboard/teacher-dashboard.html';
        return;
    }

    // 3. Load Course Data
    await loadCourseDetails();
    await loadModules();

    // 4. Setup Listeners
    setupEventListeners();
    setupFormListeners();
    setupImagePreviewListeners();
});

async function loadCourseDetails() {
    try {
        currentCourse = await api.course.getDetails(currentCourseId);
        
        document.getElementById('courseTitle').textContent = currentCourse.title;
        document.getElementById('courseDescription').textContent = currentCourse.description || 'No description provided';
        document.getElementById('enrollmentKey').textContent = currentCourse.enrollmentKey || 'N/A';
        
    } catch (error) {
        console.error("Error loading course:", error);
        alert("Failed to load course details: " + error.message);
    }
}

async function loadModules() {
    const modulesList = document.getElementById('modulesList');
    
    try {
        const modules = await api.module.getByCourse(currentCourseId);
        
        modulesList.innerHTML = '';
        
        if (!modules || modules.length === 0) {
            modulesList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-folder-open fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No modules yet. Click "Add Module" to create one.</p>
                </div>
            `;
            return;
        }

        for (const module of modules) {
            const moduleCard = await createModuleCard(module);
            modulesList.appendChild(moduleCard);
        }
        
    } catch (error) {
        console.error("Error loading modules:", error);
        modulesList.innerHTML = `<p style="color:red; text-align:center;">Error: ${error.message}</p>`;
    }
}

async function createModuleCard(module) {
    const card = document.createElement('div');
    card.className = 'module-card';
    
    // Add thumbnail if exists
    let thumbnailHTML = '';
    if (module.thumbnailPath) {
        const thumbSrc = module.thumbnailPath.startsWith('http') 
            ? module.thumbnailPath 
            : IMAGE_BASE_URL + module.thumbnailPath;
        
        thumbnailHTML = `
            <img src="${thumbSrc}" 
                 class="module-thumbnail" 
                 alt="${module.title}"
                 onerror="this.style.display='none'">
        `;
        card.classList.add('module-card-with-image');
    }
    
    // Load chapters for this module
    let chaptersHTML = '';
    try {
        const chapters = await api.chapter.getByModule(module.id);
        console.log(`Chapters for module ${module.id}:`, chapters);
        
        if (chapters && chapters.length > 0) {
            // Use Promise.all to resolve all chapter HTML promises
            const chapterHTMLPromises = chapters.map(async chapter => {
                const materialsHTML = await renderMaterialsBadges(chapter.id);
                return `
                    <div class="chapter-item">
                        <div class="chapter-header">
                            <div class="chapter-title-section">
                                <h4 class="chapter-title">${chapter.title}</h4>
                                <p class="chapter-meta">Order: ${chapter.orderIndex || 0}</p>
                            </div>
                            <div class="chapter-actions">
                                <button class="chapter-action-btn edit-chapter-btn" 
                                        data-chapter-id="${chapter.id}" 
                                        title="Edit Chapter">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="chapter-action-btn delete-chapter-btn" 
                                        data-chapter-id="${chapter.id}" 
                                        data-chapter-name="${chapter.title}" 
                                        title="Delete Chapter">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </div>
                        </div>
                        ${materialsHTML}
                    </div>
                `;
            });
            
            // Wait for all promises to resolve
            const chapterHTMLArray = await Promise.all(chapterHTMLPromises);
            chaptersHTML = chapterHTMLArray.join('');
        } else {
            chaptersHTML = '<div class="empty-state">No chapters yet</div>';
        }
    } catch (error) {
        console.error("Error loading chapters:", error);
        chaptersHTML = '<div class="empty-state" style="color:red;">Error loading chapters</div>';
    }
    
    card.innerHTML = `
        ${thumbnailHTML}
        <div class="module-header">
            <div class="module-info">
                <h3 class="module-title">${module.title}</h3>
                <p class="module-description">${module.description || 'No description'}</p>
            </div>
            <div class="module-actions">
                <button class="module-action-btn edit-module-btn" 
                        data-module-id="${module.id}"
                        data-module-title="${module.title}"
                        data-module-description="${module.description || ''}"
                        data-module-order="${module.orderIndex || 0}"
                        data-module-thumbnail="${module.thumbnailPath || ''}"
                        title="Edit Module">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="module-action-btn delete-module-btn" 
                        data-module-id="${module.id}" 
                        data-module-name="${module.title}" 
                        title="Delete Module">
                    <i class="fas fa-trash-alt"></i>
                </button>
                <button class="add-chapter-btn" 
                        data-module-id="${module.id}">
                    <i class="fas fa-plus"></i> Add Chapter
                </button>
            </div>
        </div>
        <div class="chapters-list">
            ${chaptersHTML}
        </div>
    `;
    
    return card;
}

async function renderMaterialsBadges(chapterId) {
    try {
        const materials = await api.material.getByChapter(chapterId);
        
        if (!materials || materials.length === 0) {
            return '<div class="materials-list" style="color: var(--text-light); font-size: 0.85rem;">No materials</div>';
        }
        
        const badges = materials.map(material => {
            const type = material.type.toLowerCase();
            const icon = type === 'video' ? 'fa-video' : 
                        type === 'pdf' ? 'fa-file-pdf' : 
                        type === 'quiz' ? 'fa-question-circle' : 'fa-file';
            
            return `<span class="material-badge ${type}">
                <i class="fas ${icon}"></i>${material.title}
            </span>`;
        }).join('');
        
        return `<div class="materials-list">${badges}</div>`;
        
    } catch (error) {
        return '<div class="materials-list" style="color: var(--text-light); font-size: 0.85rem;">Error loading materials</div>';
    }
}

function setupEventListeners() {
    const modulesList = document.getElementById('modulesList');
    
    // Add Module Button - Open Modal
    document.getElementById('addModuleBtn').onclick = () => {
        openAddModuleModal();
    };
    
    // Edit Course Button - Open Modal
    document.getElementById('editCourseBtn').onclick = () => {
        openEditCourseModal();
    };
    
    // Event Delegation for Module/Chapter Actions
    modulesList.addEventListener('click', (e) => {
        // Edit Module - Open Modal
        const editModuleBtn = e.target.closest('.edit-module-btn');
        if (editModuleBtn) {
            openEditModuleModal(editModuleBtn);
            return;
        }
        
        // Delete Module
        const deleteModuleBtn = e.target.closest('.delete-module-btn');
        if (deleteModuleBtn) {
            moduleToDelete = deleteModuleBtn.dataset.moduleId;
            document.getElementById('deleteModuleName').textContent = deleteModuleBtn.dataset.moduleName;
            document.getElementById('deleteModuleModal').style.display = 'flex';
            return;
        }
        
        // Add Chapter - Open Modal
        const addChapterBtn = e.target.closest('.add-chapter-btn');
        if (addChapterBtn) {
            const moduleId = addChapterBtn.dataset.moduleId;
            const moduleName = addChapterBtn.closest('.module-card').querySelector('.module-title').textContent;
            openAddChapterModal(moduleId, moduleName);
            return;
        }
        
        // Edit Chapter
        const editChapterBtn = e.target.closest('.edit-chapter-btn');
        if (editChapterBtn) {
            const chapterId = editChapterBtn.dataset.chapterId;
            window.location.href = `../edit-chapters/edit-chapters.html?chapterId=${chapterId}`;
            return;
        }
        
        // Delete Chapter
        const deleteChapterBtn = e.target.closest('.delete-chapter-btn');
        if (deleteChapterBtn) {
            chapterToDelete = deleteChapterBtn.dataset.chapterId;
            document.getElementById('deleteChapterName').textContent = deleteChapterBtn.dataset.chapterName;
            document.getElementById('deleteChapterModal').style.display = 'flex';
            return;
        }
    });
    
    // Confirm Delete Module
    document.getElementById('confirmDeleteModule').onclick = async () => {
        if (!moduleToDelete) return;
        
        const btn = document.getElementById('confirmDeleteModule');
        const originalHTML = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        
        try {
            await api.request(`/module?moduleId=${moduleToDelete}`, 'DELETE');
            alert("Module deleted successfully");
            closeDeleteModuleModal();
            await loadModules();
        } catch (error) {
            alert("Failed to delete module: " + error.message);
            btn.disabled = false;
            btn.innerHTML = originalHTML;
        }
    };
    
    // Confirm Delete Chapter
    document.getElementById('confirmDeleteChapter').onclick = async () => {
        if (!chapterToDelete) return;
        
        const btn = document.getElementById('confirmDeleteChapter');
        const originalHTML = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        
        try {
            await api.request(`/chapter?chapterId=${chapterToDelete}`, 'DELETE');
            alert("Chapter deleted successfully");
            closeDeleteChapterModal();
            await loadModules();
        } catch (error) {
            alert("Failed to delete chapter: " + error.message);
            btn.disabled = false;
            btn.innerHTML = originalHTML;
        }
    };
    
    // Logout
    document.getElementById('userProfile').onclick = async () => {
        if (confirm("Log out?")) {
            try { await api.auth.logout(); } catch (e) {}
            localStorage.removeItem('user');
            window.location.href = '../../../auth/login.html';
        }
    };
}

// ===========================
// IMAGE PREVIEW LISTENERS
// ===========================
function setupImagePreviewListeners() {
    // Add Module Image Preview
    const addModuleThumbnail = document.getElementById('addModuleThumbnail');
    addModuleThumbnail.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (event) => {
                document.getElementById('addModulePreviewImg').src = event.target.result;
                document.getElementById('addModuleImagePreview').style.display = 'block';
            };
            reader.readAsDataURL(file);
        }
    });
    
    // Edit Module Image Preview
    const editModuleThumbnail = document.getElementById('editModuleThumbnail');
    editModuleThumbnail.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (event) => {
                document.getElementById('editModulePreviewImg').src = event.target.result;
                document.getElementById('editModuleImagePreview').style.display = 'block';
            };
            reader.readAsDataURL(file);
        }
    });
}

// Remove preview functions
window.removeAddModulePreview = function() {
    document.getElementById('addModuleThumbnail').value = '';
    document.getElementById('addModuleImagePreview').style.display = 'none';
};

window.removeEditModulePreview = function() {
    document.getElementById('editModuleThumbnail').value = '';
    document.getElementById('editModuleImagePreview').style.display = 'none';
};

// ===========================
// ADD MODULE MODAL
// ===========================
function openAddModuleModal() {
    document.getElementById('addModuleModal').style.display = 'flex';
    document.getElementById('addModuleOrder').value = '0';
}

function closeAddModuleModal() {
    document.getElementById('addModuleModal').style.display = 'none';
    document.getElementById('addModuleForm').reset();
    document.getElementById('addModuleImagePreview').style.display = 'none';
}

window.closeAddModuleModal = closeAddModuleModal;

// ===========================
// ADD CHAPTER MODAL
// ===========================
function openAddChapterModal(moduleId, moduleName) {
    moduleToAddChapter = moduleId;
    document.getElementById('addChapterModuleName').textContent = moduleName;
    document.getElementById('addChapterOrder').value = '0';
    document.getElementById('addChapterModal').style.display = 'flex';
}

function closeAddChapterModal() {
    document.getElementById('addChapterModal').style.display = 'none';
    document.getElementById('addChapterForm').reset();
    moduleToAddChapter = null;
}

window.closeAddChapterModal = closeAddChapterModal;

// ===========================
// EDIT COURSE MODAL
// ===========================
function openEditCourseModal() {
    if (!currentCourse) return;
    
    document.getElementById('editCourseTitle').value = currentCourse.title;
    document.getElementById('editCourseDescription').value = currentCourse.description || '';
    document.getElementById('editTargetAudience').value = currentCourse.targetAudience || '';
    document.getElementById('editEnrollmentKey').value = currentCourse.enrollmentKey || '';
    
    document.getElementById('editCourseModal').style.display = 'flex';
}

function closeEditCourseModal() {
    document.getElementById('editCourseModal').style.display = 'none';
    document.getElementById('editCourseForm').reset();
}

window.closeEditCourseModal = closeEditCourseModal;

// ===========================
// EDIT MODULE MODAL
// ===========================
function openEditModuleModal(btn) {
    moduleToEdit = btn.dataset.moduleId;
    
    // Store current module data
    currentModuleData = {
        thumbnailPath: btn.dataset.moduleThumbnail
    };
    
    // Populate form
    document.getElementById('editModuleTitle').value = btn.dataset.moduleTitle;
    document.getElementById('editModuleDescription').value = btn.dataset.moduleDescription;
    document.getElementById('editModuleOrder').value = btn.dataset.moduleOrder;
    
    // Show current thumbnail if exists
    const currentImageDiv = document.getElementById('editModuleCurrentImage');
    const currentImg = document.getElementById('editModuleCurrentImg');
    
    if (currentModuleData.thumbnailPath) {
        const thumbSrc = currentModuleData.thumbnailPath.startsWith('http')
            ? currentModuleData.thumbnailPath
            : IMAGE_BASE_URL + currentModuleData.thumbnailPath;
        
        currentImg.src = thumbSrc;
        currentImageDiv.style.display = 'block';
    } else {
        currentImageDiv.style.display = 'none';
    }
    
    // Hide new preview
    document.getElementById('editModuleImagePreview').style.display = 'none';
    
    document.getElementById('editModuleModal').style.display = 'flex';
}

function closeEditModuleModal() {
    document.getElementById('editModuleModal').style.display = 'none';
    document.getElementById('editModuleForm').reset();
    document.getElementById('editModuleImagePreview').style.display = 'none';
    moduleToEdit = null;
    currentModuleData = null;
}

window.closeEditModuleModal = closeEditModuleModal;

// ===========================
// FORM SUBMISSIONS
// ===========================
function setupFormListeners() {
    // Add Chapter Form
    document.getElementById('addChapterForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if (!moduleToAddChapter) return;
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';
        
        try {
            const chapterData = {
                moduleId: moduleToAddChapter,
                title: document.getElementById('addChapterTitle').value.trim(),
                content: document.getElementById('addChapterDescription').value.trim(),
                orderIndex: parseInt(document.getElementById('addChapterOrder').value) || 0
            };
            
            await api.request('/chapter', 'POST', chapterData);
            
            alert("Chapter created successfully!");
            closeAddChapterModal();
            await loadModules();
            
        } catch (error) {
            console.error("Error creating chapter:", error);
            alert("Failed to create chapter: " + error.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Add Module Form
    document.getElementById('addModuleForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';
        
        try {
            let thumbnailPath = '';
            
            // Upload thumbnail if selected
            const fileInput = document.getElementById('addModuleThumbnail');
            if (fileInput.files.length > 0) {
                const uploadResult = await api.uploadFile(fileInput.files[0], 'course_content');
                thumbnailPath = uploadResult.filePath;
            }
            
            const moduleData = {
                courseId: currentCourseId,
                title: document.getElementById('addModuleTitle').value.trim(),
                description: document.getElementById('addModuleDescription').value.trim(),
                thumbnailPath: thumbnailPath,
                orderIndex: parseInt(document.getElementById('addModuleOrder').value) || 0
            };
            
            await api.request('/module', 'POST', moduleData);
            
            alert("Module created successfully!");
            closeAddModuleModal();
            await loadModules();
            
        } catch (error) {
            console.error("Error creating module:", error);
            alert("Failed to create module: " + error.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Edit Course Form
    document.getElementById('editCourseForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
        
        try {
            let thumbnailPath = currentCourse.thumbnailPath;
            
            const fileInput = document.getElementById('editCourseThumbnail');
            if (fileInput.files.length > 0) {
                const uploadResult = await api.uploadFile(fileInput.files[0], 'course_content');
                thumbnailPath = uploadResult.filePath;
            }
            
            const courseData = {
                courseId: currentCourseId,
                title: document.getElementById('editCourseTitle').value.trim(),
                description: document.getElementById('editCourseDescription').value.trim(),
                targetAudience: document.getElementById('editTargetAudience').value.trim(),
                enrollmentKey: document.getElementById('editEnrollmentKey').value.trim(),
                thumbnailPath: thumbnailPath
            };
            
            await api.request('/course', 'PUT', courseData);
            
            alert("Course updated successfully!");
            closeEditCourseModal();
            await loadCourseDetails();
            
        } catch (error) {
            console.error("Error updating course:", error);
            alert("Failed to update course: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Edit Module Form
    document.getElementById('editModuleForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if (!moduleToEdit) return;
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
        
        try {
            let thumbnailPath = currentModuleData?.thumbnailPath || '';
            
            // Upload new thumbnail if selected
            const fileInput = document.getElementById('editModuleThumbnail');
            if (fileInput.files.length > 0) {
                const uploadResult = await api.uploadFile(fileInput.files[0], 'course_content');
                thumbnailPath = uploadResult.filePath;
            }
            
            const moduleData = {
                moduleId: moduleToEdit,
                title: document.getElementById('editModuleTitle').value.trim(),
                description: document.getElementById('editModuleDescription').value.trim(),
                thumbnailPath: thumbnailPath,
                orderIndex: parseInt(document.getElementById('editModuleOrder').value) || 0
            };
            
            await api.request('/module', 'PUT', moduleData);
            
            alert("Module updated successfully!");
            closeEditModuleModal();
            await loadModules();
            
        } catch (error) {
            console.error("Error updating module:", error);
            alert("Failed to update module: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
}

function closeDeleteModuleModal() {
    document.getElementById('deleteModuleModal').style.display = 'none';
    moduleToDelete = null;
}

function closeDeleteChapterModal() {
    document.getElementById('deleteChapterModal').style.display = 'none';
    chapterToDelete = null;
}

window.closeDeleteModuleModal = closeDeleteModuleModal;
window.closeDeleteChapterModal = closeDeleteChapterModal;

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}
