import api from '../../../api.js';

const IMAGE_BASE_URL = `http://192.168.100.3:8080/api/`;

let currentChapterId = null;
let currentChapter = null;
let deleteItemId = null;
let deleteItemType = null;
let currentForumId = null;
let currentForumData = null;
let commentToEdit = null;
let commentToDelete = null;
let currentUser = null;
let userCache = {};
let currentQuizId = null;
let currentQuiz = null;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }
    currentUser = JSON.parse(userJson);
    updateUserProfile(currentUser);

    // 2. Get Chapter ID from URL
    const params = new URLSearchParams(window.location.search);
    currentChapterId = params.get('chapterId');
    
    if (!currentChapterId) {
        alert("No chapter ID specified");
        window.history.back();
        return;
    }

    // 3. Load Chapter Data
    await loadChapterDetails();
    await loadMaterials();
    await loadQuizzes();
    await loadForums();

    // 4. Setup Listeners
    setupEventListeners();
    setupFormListeners();
    setupTabListeners();
});

// --- HELPER: USER FETCHING ---
function collectUserIds(comments, idSet) {
    comments.forEach(c => {
        idSet.add(c.userId);
        if (c.replies && c.replies.length > 0) {
            collectUserIds(c.replies, idSet);
        }
    });
}

async function fetchUsersForComments(comments) {
    const idSet = new Set();
    collectUserIds(comments, idSet);
    
    // Always ensure current user is cached
    userCache[currentUser.id] = currentUser;

    const idsToFetch = [...idSet].filter(id => !userCache[id]);
    
    if (idsToFetch.length > 0) {
        await Promise.all(idsToFetch.map(async (id) => {
            try {
                const user = await api.user.getById(id);
                userCache[id] = user;
            } catch (e) {
                userCache[id] = { firstName: 'Unknown', lastName: 'User' };
            }
        }));
    }
}

function getRandomColor(id) {
    const colors = ['#5b4acf', '#10b981', '#f59e0b', '#ef4444', '#3b82f6'];
    return colors[id % colors.length] || colors[0];
}

// --- DATE FORMATTING HELPERS ---
function parseBackendDate(dateString) {
    if (!dateString) return '';
    
    try {
        // Backend format: "dd/MM/yyyy HH:mm:ss"
        const parts = dateString.split(' ');
        if (parts.length !== 2) return '';
        
        const dateParts = parts[0].split('/');
        const timeParts = parts[1].split(':');
        
        if (dateParts.length !== 3 || timeParts.length !== 3) return '';
        
        const day = dateParts[0];
        const month = dateParts[1];
        const year = dateParts[2];
        const hours = timeParts[0];
        const minutes = timeParts[1];
        
        // Format for datetime-local: "YYYY-MM-DDTHH:mm"
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    } catch (e) {
        console.error('Error parsing date:', e);
        return '';
    }
}

function formatDateTimeForBackend(datetimeLocalValue) {
    if (!datetimeLocalValue) return null;
    
    const date = new Date(datetimeLocalValue);
    if (isNaN(date.getTime())) return null;
    
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = '00';
    
    return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`;
}

async function loadChapterDetails() {
    try {
        currentChapter = await api.chapter.getDetails(currentChapterId);
        
        document.getElementById('chapterTitle').textContent = currentChapter.title;
        document.getElementById('chapterDescription').textContent = currentChapter.content || 'No description provided';
        
    } catch (error) {
        console.error("Error loading chapter:", error);
        alert("Failed to load chapter details: " + error.message);
    }
}

async function loadMaterials() {
    const materialsGrid = document.getElementById('materialsGrid');
    
    try {
        const materials = await api.material.getByChapter(currentChapterId);
        
        materialsGrid.innerHTML = '';
        
        if (!materials || materials.length === 0) {
            materialsGrid.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-folder-open fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No materials yet. Add a video or PDF to get started.</p>
                </div>
            `;
            return;
        }

        materials.forEach(material => {
            const card = createMaterialCard(material);
            materialsGrid.appendChild(card);
        });
        
    } catch (error) {
        console.error("Error loading materials:", error);
        materialsGrid.innerHTML = `<p style="color:red; text-align:center;">Error: ${error.message}</p>`;
    }
}

function createMaterialCard(material) {
    const card = document.createElement('div');
    card.className = 'material-card';
    
    const type = material.type.toUpperCase();
    const iconClass = type === 'VIDEO' ? 'fa-video' : 'fa-file-pdf';
    const typeLabel = type === 'VIDEO' ? 'video' : 'pdf';
    
    card.innerHTML = `
        <div class="material-card-header">
            <div class="material-icon ${typeLabel}">
                <i class="fas ${iconClass}"></i>
            </div>
            <div class="material-actions">
                <button class="material-action-btn edit-btn" 
                        data-id="${material.id}" 
                        data-title="${material.title}"
                        data-type="${type}"
                        title="Edit">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="material-action-btn delete-btn" 
                        data-id="${material.id}" 
                        data-name="${material.title}"
                        data-type="material"
                        title="Delete">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>
        </div>
        <h3 class="material-title">${material.title}</h3>
        <p class="material-type">${type}</p>
    `;
    
    return card;
}

async function loadQuizzes() {
    const quizzesList = document.getElementById('quizzesList');
    
    try {
        const quizzes = await api.quiz.getByChapter(currentChapterId);
        
        quizzesList.innerHTML = '';
        
        if (!quizzes || quizzes.length === 0) {
            quizzesList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-question-circle fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No quizzes yet. Create a quiz to test your students.</p>
                </div>
            `;
            return;
        }

        quizzes.forEach(quiz => {
            const card = createQuizCard(quiz);
            quizzesList.appendChild(card);
        });
        
    } catch (error) {
        console.error("Error loading quizzes:", error);
        quizzesList.innerHTML = `<p style="color:red; text-align:center;">Error: ${error.message}</p>`;
    }
}

function createQuizCard(quiz) {
    const card = document.createElement('div');
    card.className = 'quiz-card';
    
    card.innerHTML = `
        <div class="quiz-card-header">
            <div class="quiz-info">
                <h3>${quiz.title}</h3>
                <div class="quiz-meta">
                    <span><i class="fas fa-calendar"></i> ${quiz.availableFrom || 'Not set'}</span>
                    <span><i class="fas fa-clock"></i> ${quiz.availableTo || 'No deadline'}</span>
                </div>
            </div>
            <div class="quiz-actions">
                <button class="material-action-btn edit-btn edit-quiz-btn" 
                        data-id="${quiz.id}" 
                        title="Edit Quiz">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="material-action-btn delete-btn" 
                        data-id="${quiz.id}" 
                        data-name="${quiz.title}"
                        data-type="quiz"
                        title="Delete">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>
        </div>
        <p style="color: var(--text-gray); margin-top: 0.5rem;">${quiz.description || 'No description'}</p>
    `;
    
    return card;
}

async function openEditQuizModal(quizId) {
    try {
        currentQuizId = quizId;
        
        currentQuiz = await api.quiz.getDetails(quizId);
        
        document.getElementById('editQuizTitle').value = currentQuiz.title || '';
        document.getElementById('editQuizDescription').value = currentQuiz.description || '';
        document.getElementById('editQuizTotalPoints').value = currentQuiz.totalPoints || 20;
        document.getElementById('editAvailableFrom').value = parseBackendDate(currentQuiz.availableFrom);
        document.getElementById('editAvailableTo').value = parseBackendDate(currentQuiz.availableTo);
        
        document.getElementById('editQuizModal').style.display = 'flex';
        
    } catch (error) {
        console.error('Error loading quiz:', error);
        alert('Failed to load quiz details: ' + error.message);
    }
}

function closeEditQuizModal() {
    document.getElementById('editQuizModal').style.display = 'none';
    document.getElementById('editQuizForm').reset();
    currentQuizId = null;
    currentQuiz = null;
}

async function loadForums() {
    const forumsList = document.getElementById('forumsList');
    
    try {
        const forums = await api.forum.getByChapter(currentChapterId);
        
        forumsList.innerHTML = '';
        
        if (!forums || forums.length === 0) {
            forumsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-comments fa-3x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No forums yet. Create a forum for discussions.</p>
                </div>
            `;
            return;
        }

        forums.forEach(forum => {
            const card = createForumCard(forum);
            forumsList.appendChild(card);
        });
        
    } catch (error) {
        console.error("Error loading forums:", error);
        forumsList.innerHTML = `<p style="color:red; text-align:center;">Error: ${error.message}</p>`;
    }
}

function createForumCard(forum) {
    const card = document.createElement('div');
    card.className = 'forum-card';
    
    card.innerHTML = `
        <div class="forum-info">
            <h3 class="forum-title">${forum.title}</h3>
            <p class="forum-meta">Forum ID: ${forum.id}</p>
        </div>
        <div class="forum-actions">
            <button class="material-action-btn view-btn" 
                    data-id="${forum.id}" 
                    data-title="${forum.title}"
                    title="View Discussion">
                <i class="fas fa-eye"></i>
            </button>
            <button class="material-action-btn edit-btn" 
                    data-id="${forum.id}" 
                    data-title="${forum.title}"
                    title="Edit Forum">
                <i class="fas fa-edit"></i>
            </button>
            <button class="material-action-btn delete-btn" 
                    data-id="${forum.id}" 
                    data-name="${forum.title}"
                    data-type="forum"
                    title="Delete">
                <i class="fas fa-trash-alt"></i>
            </button>
        </div>
    `;
    
    return card;
}

async function openForumDiscussion(forumId, forumTitle) {
    currentForumId = forumId;
    currentForumData = { id: forumId, title: forumTitle };
    
    // Show forum discussion modal
    document.getElementById('forumDiscussionModal').style.display = 'flex';
    document.getElementById('forumDiscussionTitle').textContent = forumTitle;
    
    await loadForumComments();
}

async function loadForumComments() {
    const commentsList = document.getElementById('forumCommentsList');
    
    try {
        commentsList.innerHTML = '<div class="loading-state"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Loading comments...</p></div>';
        
        const comments = await api.comment.getByForum(currentForumId);
        
        commentsList.innerHTML = '';
        
        if (!comments || comments.length === 0) {
            commentsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-comment-slash fa-2x" style="color: var(--text-light); margin-bottom: 1rem;"></i>
                    <p>No comments yet. Be the first to start the discussion!</p>
                </div>
            `;
            document.getElementById('forumCommentCount').textContent = '0';
            return;
        }

        // Fetch user data for all comments
        await fetchUsersForComments(comments);

        // Separate top-level comments and replies
        const topLevelComments = comments.filter(c => !c.isReply || c.parentCommentId === 0);
        const replies = comments.filter(c => c.isReply && c.parentCommentId > 0);
        
        document.getElementById('forumCommentCount').textContent = topLevelComments.length;
        
        topLevelComments.forEach(comment => {
            const commentElement = createCommentNode(comment);
            commentsList.appendChild(commentElement);
            
            // Add replies
            const commentReplies = replies.filter(r => r.parentCommentId === comment.id);
            if (commentReplies.length > 0) {
                const repliesContainer = document.createElement('div');
                repliesContainer.className = 'replies-container';
                commentReplies.forEach(reply => {
                    repliesContainer.appendChild(createCommentNode(reply, true));
                });
                commentElement.appendChild(repliesContainer);
            }
        });
        
    } catch (error) {
        console.error("Error loading comments:", error);
        commentsList.innerHTML = `<p style="color:red; text-align:center;">Error: ${error.message}</p>`;
    }
}

// Recursive Render Function
function createCommentNode(comment, isReply = false) {
    const user = userCache[comment.userId] || { firstName: 'User', lastName: 'Unknown' };
    const initials = (user.firstName.charAt(0) || '?').toUpperCase();
    const fullName = `${user.firstName} ${user.lastName}`;
    const timeAgo = formatDate(comment.createdAt);
    
    // Check Ownership
    const isOwner = currentUser.id === comment.userId;

    const container = document.createElement('div');
    container.className = 'comment-thread';
    container.id = `thread-${comment.id}`;

    // --- HTML Structure ---
    const commentHTML = `
        <div class="comment-item ${isReply ? 'reply' : ''}" id="comment-item-${comment.id}">
            <div class="comment-avatar-img" style="background:${getRandomColor(comment.userId)};">${initials}</div>
            <div class="comment-content-wrapper">
                <div class="comment-header">
                    <div class="comment-author">
                        <div class="author-info">
                            <span class="author-name">${fullName}</span>
                            <span class="comment-time">${timeAgo}</span>
                            ${comment.modified ? '<span style="font-size:0.7rem; color:#999; margin-left:5px;">(edited)</span>' : ''}
                        </div>
                    </div>
                    
                    ${isOwner ? `
                    <div class="comment-owner-actions">
                        <button class="btn-icon-action btn-edit" title="Edit" data-id="${comment.id}"><i class="fas fa-pencil-alt"></i></button>
                        <button class="btn-icon-action btn-delete" title="Delete" data-id="${comment.id}"><i class="fas fa-trash"></i></button>
                    </div>` : ''}
                </div>

                <div id="content-display-${comment.id}">
                    <p class="comment-text">${escapeHtml(comment.content)}</p>
                </div>

                <div id="content-edit-${comment.id}" class="edit-comment-wrapper" style="display:none;">
                    <textarea class="edit-comment-input">${comment.content}</textarea>
                    <div class="edit-actions">
                        <button class="btn-cancel" data-id="${comment.id}">Cancel</button>
                        <button class="btn-save" data-id="${comment.id}">Save</button>
                    </div>
                </div>

                ${!isReply ? `
                <div class="comment-actions">
                    <button class="comment-action-btn reply-trigger" data-id="${comment.id}">
                        <i class="fas fa-reply"></i> Reply
                    </button>
                </div>
                ` : ''}
                
                <div class="reply-form-container" id="reply-form-${comment.id}" style="display:none; margin-top:10px;">
                    <textarea class="reply-input" placeholder="Write a reply..."></textarea>
                    <div style="margin-top:5px; text-align:right;">
                        <button class="btn-cancel-reply" data-id="${comment.id}">Cancel</button>
                        <button class="btn-submit-reply" data-id="${comment.id}">Reply</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    container.innerHTML = commentHTML;

    // Attach Listeners
    attachCommentListeners(container, comment);

    return container;
}

function attachCommentListeners(element, comment) {
    const commentId = comment.id;

    // --- Reply Logic ---
    const replyBtn = element.querySelector(`.reply-trigger[data-id="${commentId}"]`);
    if (replyBtn) {
        const replyForm = element.querySelector(`#reply-form-${commentId}`);
        const replyCancel = element.querySelector(`.btn-cancel-reply[data-id="${commentId}"]`);
        const replySubmit = element.querySelector(`.btn-submit-reply[data-id="${commentId}"]`);
        const replyInput = replyForm.querySelector('.reply-input');

        replyBtn.addEventListener('click', () => {
            replyForm.style.display = (replyForm.style.display === 'none') ? 'block' : 'none';
            if (replyForm.style.display === 'block') replyInput.focus();
        });

        replyCancel.addEventListener('click', () => {
            replyForm.style.display = 'none';
            replyInput.value = '';
        });

        replySubmit.addEventListener('click', async () => {
            const text = replyInput.value.trim();
            if (!text) return;
            replySubmit.disabled = true;
            replySubmit.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            try {
                await api.comment.add(currentForumId, text, commentId);
                await loadForumComments();
            } catch (e) {
                alert("Failed to reply: " + e.message);
                replySubmit.disabled = false;
                replySubmit.innerHTML = 'Reply';
            }
        });
    }

    // --- Owner Actions (Edit/Delete) ---
    const btnEdit = element.querySelector(`.btn-edit[data-id="${commentId}"]`);
    const btnDelete = element.querySelector(`.btn-delete[data-id="${commentId}"]`);

    if (btnEdit) {
        const displayDiv = element.querySelector(`#content-display-${commentId}`);
        const editDiv = element.querySelector(`#content-edit-${commentId}`);
        const editInput = editDiv.querySelector('.edit-comment-input');
        const saveBtn = editDiv.querySelector('.btn-save');
        const cancelBtn = editDiv.querySelector('.btn-cancel');

        // Toggle Edit Mode
        btnEdit.addEventListener('click', () => {
            displayDiv.style.display = 'none';
            editDiv.style.display = 'block';
            editInput.value = comment.content;
            editInput.focus();
        });

        // Cancel Edit
        cancelBtn.addEventListener('click', () => {
            displayDiv.style.display = 'block';
            editDiv.style.display = 'none';
        });

        // Save Edit
        saveBtn.addEventListener('click', async () => {
            const newContent = editInput.value.trim();
            if (!newContent || newContent === comment.content) {
                displayDiv.style.display = 'block';
                editDiv.style.display = 'none';
                return;
            }

            saveBtn.disabled = true;
            try {
                await api.comment.update(commentId, newContent);
                displayDiv.querySelector('p').textContent = newContent;
                displayDiv.style.display = 'block';
                editDiv.style.display = 'none';
                comment.content = newContent;
            } catch (e) {
                alert("Update failed: " + e.message);
            } finally {
                saveBtn.disabled = false;
            }
        });
    }

    if (btnDelete) {
        btnDelete.addEventListener('click', async () => {
            if (confirm("Are you sure you want to delete this comment?")) {
                try {
                    await api.comment.delete(commentId);
                    const threadElement = document.getElementById(`thread-${commentId}`);
                    if (threadElement) {
                        threadElement.remove();
                    } else {
                        await loadForumComments();
                    }
                } catch (e) {
                    alert("Delete failed: " + e.message);
                }
            }
        });
    }
}

function formatDate(dateString) {
    if (!dateString) return 'Just now';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function setupEventListeners() {
    // Back Button
    document.getElementById('backBtn').onclick = () => {
        window.history.back();
    };
    
    // Edit Chapter Info Button
    document.getElementById('editChapterInfoBtn').onclick = () => {
        openEditChapterModal();
    };
    
    // Add Material Buttons
    document.getElementById('addVideoBtn').onclick = () => {
        document.getElementById('addVideoModal').style.display = 'flex';
    };
    
    document.getElementById('addPdfBtn').onclick = () => {
        document.getElementById('addPdfModal').style.display = 'flex';
    };
    
    // Add Quiz Button
    document.getElementById('addQuizBtn').onclick = () => {
        window.location.href = `../create-quiz/create-quiz.html?chapterId=${currentChapterId}`;
    };
    
    // Add Forum Button
    document.getElementById('addForumBtn').onclick = () => {
        document.getElementById('addForumModal').style.display = 'flex';
    };
    
    // Event Delegation
    document.addEventListener('click', async (e) => {
        const viewBtn = e.target.closest('.view-btn');
        if (viewBtn) {
            const forumId = viewBtn.dataset.id;
            const forumTitle = viewBtn.dataset.title;
            await openForumDiscussion(forumId, forumTitle);
        }
        
        // Edit Quiz Button
        const editQuizBtn = e.target.closest('.edit-quiz-btn');
        if (editQuizBtn) {
            const quizId = editQuizBtn.dataset.id;
            await openEditQuizModal(parseInt(quizId));
        }
        
        // Delete Button (materials, quizzes, forums)
        const deleteBtn = e.target.closest('.delete-btn');
        if (deleteBtn && !deleteBtn.classList.contains('delete-comment-btn')) {
            deleteItemId = deleteBtn.dataset.id;
            deleteItemType = deleteBtn.dataset.type;
            document.getElementById('deleteItemName').textContent = deleteBtn.dataset.name;
            document.getElementById('deleteModal').style.display = 'flex';
        }
    });
    
    // Confirm Delete
    document.getElementById('confirmDeleteBtn').onclick = async () => {
        if (!deleteItemId || !deleteItemType) return;
        
        const btn = document.getElementById('confirmDeleteBtn');
        const originalHTML = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        
        try {
            if (deleteItemType === 'material') {
                await api.material.delete(deleteItemId);
                await loadMaterials();
            } else if (deleteItemType === 'quiz') {
                await api.quiz.delete(deleteItemId);
                await loadQuizzes();
            } else if (deleteItemType === 'forum') {
                await api.forum.delete(deleteItemId);
                await loadForums();
            }
            
            alert(`${deleteItemType.charAt(0).toUpperCase() + deleteItemType.slice(1)} deleted successfully`);
            closeDeleteModal();
            
        } catch (error) {
            alert(`Failed to delete: ${error.message}`);
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

function setupTabListeners() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.dataset.tab;
            
            // Remove active class from all tabs and contents
            tabButtons.forEach(b => b.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));
            
            // Add active class to clicked tab and its content
            btn.classList.add('active');
            document.getElementById(`${targetTab}-tab`).classList.add('active');
        });
    });
}

function setupFormListeners() {
    // Edit Chapter Form
    document.getElementById('editChapterForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
        
        try {
            const chapterData = {
                chapterId: currentChapterId,
                title: document.getElementById('editChapterTitleInput').value.trim(),
                content: document.getElementById('editChapterDescriptionInput').value.trim(),
                orderIndex: parseInt(document.getElementById('editChapterOrder').value) || 0
            };
            
            await api.chapter.update(chapterData);
            
            alert("Chapter updated successfully!");
            closeEditChapterModal();
            await loadChapterDetails();
            
        } catch (error) {
            console.error("Error updating chapter:", error);
            alert("Failed to update chapter: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Add Video Form
    document.getElementById('addVideoForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
        
        try {
            const fileInput = document.getElementById('videoFile');
            const file = fileInput.files[0];
            
            if (!file) throw new Error("Please select a video file");
            
            // Upload video
            const uploadResult = await api.uploadFile(file, 'course_content');
            
            // Create material
            const materialData = {
                chapterId: currentChapterId,
                title: document.getElementById('videoTitle').value.trim(),
                path: uploadResult.filePath,
                type: 'VIDEO'
            };
            
            await api.material.create(materialData);
            
            alert("Video uploaded successfully!");
            closeAddVideoModal();
            await loadMaterials();
            
        } catch (error) {
            console.error("Error uploading video:", error);
            alert("Failed to upload video: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Add PDF Form
    document.getElementById('addPdfForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
        
        try {
            const fileInput = document.getElementById('pdfFile');
            const file = fileInput.files[0];
            
            if (!file) throw new Error("Please select a PDF file");
            
            // Upload PDF
            const uploadResult = await api.uploadFile(file, 'course_content');
            
            // Create material
            const materialData = {
                chapterId: currentChapterId,
                title: document.getElementById('pdfTitle').value.trim(),
                path: uploadResult.filePath,
                type: 'PDF'
            };
            
            await api.material.create(materialData);
            
            alert("PDF uploaded successfully!");
            closeAddPdfModal();
            await loadMaterials();
            
        } catch (error) {
            console.error("Error uploading PDF:", error);
            alert("Failed to upload PDF: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Add Forum Form
    document.getElementById('addForumForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';
        
        try {
            const forumData = {
                chapterId: currentChapterId,
                title: document.getElementById('forumTitle').value.trim()
            };
            
            await api.forum.create(forumData);
            
            alert("Forum created successfully!");
            closeAddForumModal();
            await loadForums();
            
        } catch (error) {
            console.error("Error creating forum:", error);
            alert("Failed to create forum: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    // Forum Comment Form
    document.getElementById('forumCommentForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-post-comment');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Posting...';
        
        try {
            const content = document.getElementById('forumCommentContent').value.trim();
            
            if (!content) throw new Error("Please enter a comment");
            
            await api.comment.add(currentForumId, content, 0);
            
            document.getElementById('forumCommentContent').value = '';
            await loadForumComments();
            
        } catch (error) {
            console.error("Error posting comment:", error);
            alert("Failed to post comment: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
    
    document.getElementById('editQuizForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = e.target.querySelector('.btn-save');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
        
        try {
            const availableFromValue = document.getElementById('editAvailableFrom').value;
            const availableToValue = document.getElementById('editAvailableTo').value;
            
            const quizData = {
                quizId: currentQuizId,
                title: document.getElementById('editQuizTitle').value.trim(),
                description: document.getElementById('editQuizDescription').value.trim(),
                totalPoints: parseInt(document.getElementById('editQuizTotalPoints').value) || 20,
                availableFrom: formatDateTimeForBackend(availableFromValue),
                availableTo: formatDateTimeForBackend(availableToValue)
            };
            
            await api.quiz.update(quizData);
            
            alert("Quiz updated successfully!");
            closeEditQuizModal();
            await loadQuizzes();
            
        } catch (error) {
            console.error("Error updating quiz:", error);
            alert("Failed to update quiz: " + error.message);
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
}

// Modal Functions
function openEditChapterModal() {
    document.getElementById('editChapterTitleInput').value = currentChapter.title;
    document.getElementById('editChapterDescriptionInput').value = currentChapter.content || '';
    document.getElementById('editChapterOrder').value = currentChapter.orderIndex || 0;
    document.getElementById('editChapterModal').style.display = 'flex';
}

function closeEditChapterModal() {
    document.getElementById('editChapterModal').style.display = 'none';
    document.getElementById('editChapterForm').reset();
}

function closeAddVideoModal() {
    document.getElementById('addVideoModal').style.display = 'none';
    document.getElementById('addVideoForm').reset();
}

function closeAddPdfModal() {
    document.getElementById('addPdfModal').style.display = 'none';
    document.getElementById('addPdfForm').reset();
}

function closeAddForumModal() {
    document.getElementById('addForumModal').style.display = 'none';
    document.getElementById('addForumForm').reset();
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    deleteItemId = null;
    deleteItemType = null;
}

function closeForumDiscussionModal() {
    document.getElementById('forumDiscussionModal').style.display = 'none';
    currentForumId = null;
    currentForumData = null;
    document.getElementById('forumCommentContent').value = '';
}

function closeEditCommentModal() {
    document.getElementById('editCommentModal').style.display = 'none';
    document.getElementById('editCommentForm').reset();
    commentToEdit = null;
}

function closeDeleteCommentModal() {
    document.getElementById('deleteCommentModal').style.display = 'none';
    commentToDelete = null;
}

// Make functions globally accessible
window.closeEditChapterModal = closeEditChapterModal;
window.closeAddVideoModal = closeAddVideoModal;
window.closeAddPdfModal = closeAddPdfModal;
window.closeAddForumModal = closeAddForumModal;
window.closeDeleteModal = closeDeleteModal;
window.closeForumDiscussionModal = closeForumDiscussionModal;
window.closeEditCommentModal = closeEditCommentModal;
window.closeDeleteCommentModal = closeDeleteCommentModal;
window.closeEditQuizModal = closeEditQuizModal;

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${user.firstName.charAt(0)}${user.lastName.charAt(0)}</span>`;
}
