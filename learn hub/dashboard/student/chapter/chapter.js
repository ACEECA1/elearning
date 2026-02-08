import api from '../../../api.js';

const IMAGE_BASE_URL = `http://192.168.100.3:8080/api/`;
const urlParams = new URLSearchParams(window.location.search);
const chapterId = urlParams.get('id');

// Global State
let currentForumId = null;
let currentUser = null; // Store logged-in user
let userCache = {}; 

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Auth Check
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../auth/login.html';
        return;
    }
    currentUser = JSON.parse(userJson); // Store globally
    updateUserProfile(currentUser);

    if (!chapterId) {
        alert("No chapter ID specified.");
        window.history.back();
        return;
    }

    try {
        await loadChapterContent(chapterId);
        await loadMaterials(chapterId);
        
        setupTabs();
        loadDiscussion(chapterId); 
        loadQuiz(chapterId);       

        setupLogoutListener();
        setupSidebarToggle();
    } catch (error) {
        console.error("Page Load Error:", error);
    }
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

// --- DISCUSSION LOGIC ---

async function loadDiscussion(chapterId) {
    const list = document.getElementById('commentsList');
    try {
        let forums = await api.forum.getByChapter(chapterId);
        
        if (!forums || forums.length === 0) {
            try {
                const newForum = await api.forum.create(parseInt(chapterId), "General Discussion");
                currentForumId = newForum.id || newForum.forumId;
            } catch (e) {
                list.innerHTML = '<div style="text-align:center; padding:1rem; color:#888;">Discussion unavailable.</div>';
                return;
            }
        } else {
            currentForumId = forums[0].id;
        }

        const comments = await api.comment.getByForum(currentForumId);
        
        list.innerHTML = '';
        if (!comments || comments.length === 0) {
            list.innerHTML = '<p style="text-align:center; color:#999; margin-top:1rem;">Be the first to comment!</p>';
        } else {
            await fetchUsersForComments(comments);
            comments.forEach(rootComment => {
                const commentNode = createCommentNode(rootComment);
                list.appendChild(commentNode);
            });
        }

        setupMainCommentBox();

    } catch (error) {
        console.error("Forum Error:", error);
        list.innerHTML = `<p style="color:red; text-align:center;">Failed to load discussion: ${error.message}</p>`;
    }
}

// Recursive Render Function
function createCommentNode(comment) {
    const user = userCache[comment.userId] || { firstName: 'User', lastName: 'Unknown' };
    const initials = (user.firstName.charAt(0) || '?').toUpperCase();
    const fullName = `${user.firstName} ${user.lastName}`;
    const timeAgo = new Date(comment.createdAt).toLocaleString();
    
    // Check Ownership
    const isOwner = currentUser.id === comment.userId;

    const container = document.createElement('div');
    container.className = 'comment-thread';
    container.id = `thread-${comment.id}`;

    // --- HTML Structure ---
    const commentHTML = `
        <div class="comment-item" id="comment-item-${comment.id}">
            <div class="comment-avatar-img" style="background:${getRandomColor(comment.userId)};">${initials}</div>
            <div class="comment-content">
                <div class="comment-header">
                    <span class="comment-author">${fullName}</span> 
                    <span class="comment-time">${timeAgo}</span>
                    ${comment.modified ? '<span style="font-size:0.7rem; color:#999; margin-left:5px;">(edited)</span>' : ''}
                    
                    ${isOwner ? `
                    <div class="comment-owner-actions">
                        <button class="btn-icon-action btn-edit" title="Edit" data-id="${comment.id}"><i class="fas fa-pencil-alt"></i></button>
                        <button class="btn-icon-action btn-delete" title="Delete" data-id="${comment.id}"><i class="fas fa-trash"></i></button>
                    </div>` : ''}
                </div>

                <div id="content-display-${comment.id}">
                    <p class="comment-text">${comment.content}</p>
                </div>

                <div id="content-edit-${comment.id}" class="edit-comment-wrapper" style="display:none;">
                    <textarea class="edit-comment-input">${comment.content}</textarea>
                    <div class="edit-actions">
                        <button class="btn-cancel" data-id="${comment.id}">Cancel</button>
                        <button class="btn-save" data-id="${comment.id}">Save</button>
                    </div>
                </div>

                <div class="comment-actions">
                    <button class="comment-action-btn reply-trigger" data-id="${comment.id}">
                        <i class="fas fa-reply"></i> Reply
                    </button>
                </div>
                
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

    // Render Children
    if (comment.replies && comment.replies.length > 0) {
        const repliesContainer = document.createElement('div');
        repliesContainer.className = 'replies-container';
        comment.replies.forEach(child => {
            const childNode = createCommentNode(child);
            repliesContainer.appendChild(childNode);
        });
        container.appendChild(repliesContainer);
    }

    // Attach Listeners
    attachCommentListeners(container, comment);

    return container;
}

function attachCommentListeners(element, comment) {
    const commentId = comment.id;

    // --- Reply Logic ---
    const replyBtn = element.querySelector(`.reply-trigger[data-id="${commentId}"]`);
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
        try {
            await api.comment.add(currentForumId, text, commentId);
            loadDiscussion(urlParams.get('id'));
        } catch (e) {
            alert("Failed to reply: " + e.message);
            replySubmit.disabled = false;
        }
    });

    // --- Owner Actions (Edit/Delete) ---
    // Only attach if elements exist (meaning user owns the comment)
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
            editInput.value = displayDiv.querySelector('p').textContent; // Reset to current
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
                // If empty or same, just cancel
                displayDiv.style.display = 'block';
                editDiv.style.display = 'none';
                return;
            }

            saveBtn.disabled = true;
            try {
                await api.comment.update(commentId, newContent);
                // Update UI directly without reload
                displayDiv.querySelector('p').textContent = newContent;
                displayDiv.style.display = 'block';
                editDiv.style.display = 'none';
                comment.content = newContent; // Update local obj
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
                    // Remove from DOM
                    const threadElement = document.getElementById(`thread-${commentId}`);
                    // If it's a child in a reply list or root
                    if (threadElement) {
                        threadElement.remove(); 
                    } else {
                        // Fallback: reload (rare case of ID mismatch)
                        loadDiscussion(urlParams.get('id'));
                    }
                } catch (e) {
                    alert("Delete failed: " + e.message);
                }
            }
        });
    }
}

function setupMainCommentBox() {
    const postBtn = document.getElementById('sendCommentBtn');
    const newBtn = postBtn.cloneNode(true);
    postBtn.parentNode.replaceChild(newBtn, postBtn);
    
    newBtn.addEventListener('click', async () => {
        const input = document.getElementById('commentInput');
        const content = input.value.trim();
        if (!content) return;

        newBtn.disabled = true;
        newBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

        try {
            await api.comment.add(currentForumId, content, 0);
            input.value = '';
            loadDiscussion(urlParams.get('id'));
        } catch (e) {
            alert("Failed to post: " + e.message);
        } finally {
            newBtn.disabled = false;
            newBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Post';
        }
    });
}

// --- STANDARD PAGE FUNCTIONS (Same as before) ---

async function loadChapterContent(id) {
    const chapter = await api.chapter.getDetails(id);
    document.getElementById('chapterTitle').textContent = chapter.title;
    document.getElementById('chapterSubtitle').textContent = `Chapter ${chapter.orderIndex || ''}`;

    let content = chapter.content || "";
    const videoContainer = document.getElementById('videoContainer');
    const videoFrame = document.getElementById('videoFrame');
    const contentEl = document.getElementById('chapterContent');

    const isLocalVideo = content.startsWith("uploads/") && (content.endsWith(".mp4") || content.endsWith(".webm"));
    const isExternalVideo = content.match(/^(http|https):\/\/.*\.(mp4|webm|ogg)$/i) || content.includes("youtube.com") || content.includes("youtu.be");

    if (isLocalVideo || isExternalVideo) {
        videoContainer.style.display = 'block';
        contentEl.style.display = 'none';
        videoFrame.innerHTML = '';

        if (isLocalVideo) {
            const videoUrl = `${IMAGE_BASE_URL}${content}`;
            videoFrame.innerHTML = `<video width="100%" height="100%" controls controlsList="nodownload"><source src="${videoUrl}" type="video/mp4"></video>`;
        } else if (content.includes("youtube.com") || content.includes("youtu.be")) {
            let videoId = content.includes("v=") ? content.split('v=')[1].split('&')[0] : content.split('/').pop();
            videoFrame.innerHTML = `<iframe width="100%" height="500" src="https://www.youtube.com/embed/${videoId}" frameborder="0" allowfullscreen></iframe>`;
        } else {
            videoFrame.innerHTML = `<video width="100%" height="100%" controls><source src="${content}" type="video/mp4"></video>`;
        }
    } else {
        videoContainer.style.display = 'none';
        contentEl.style.display = 'block';
        contentEl.innerHTML = content.replace(/\n/g, '<br>');
    }
}

async function loadMaterials(id) {
    const list = document.getElementById('materialsList');
    const materials = await api.material.getByChapter(id);
    list.innerHTML = '';

    if (!materials || materials.length === 0) {
        list.innerHTML = '<p class="no-materials">No materials available.</p>';
        return;
    }

    materials.forEach(mat => {
        const isVideo = mat.type === 'VIDEO' || mat.path.endsWith('.mp4');
        const icon = isVideo ? 'fa-play-circle' : 'fa-file-pdf';
        const fileUrl = `${IMAGE_BASE_URL}${mat.path}`;

        const item = document.createElement('div');
        item.className = 'material-item';
        item.innerHTML = `
            <div class="material-info"><i class="fas ${icon}"></i><span>${mat.title}</span></div>
            <button class="btn-action">${isVideo ? 'Watch' : 'Download'}</button>
        `;
        item.querySelector('button').addEventListener('click', () => {
            if (isVideo) {
                const vf = document.getElementById('videoFrame');
                document.getElementById('videoContainer').style.display = 'block';
                vf.innerHTML = `<video width="100%" height="100%" controls autoplay><source src="${fileUrl}" type="video/mp4"></video>`;
                window.scrollTo({ top: 0, behavior: 'smooth' });
            } else {
                window.open(fileUrl, '_blank');
            }
        });
        list.appendChild(item);
    });
}

function setupTabs() {
    const tabs = document.querySelectorAll('.tab-btn');
    const contents = document.querySelectorAll('.tab-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            contents.forEach(c => c.classList.remove('active'));

            tab.classList.add('active');
            document.getElementById(tab.getAttribute('data-tab')).classList.add('active');
        });
    });
}

async function loadQuiz(chapterId) {
    const container = document.getElementById('quizContainer');
    const userJson = localStorage.getItem('user');
    const currentUser = JSON.parse(userJson);

    try {
        // 1. Get Quiz
        const quizzes = await api.quiz.getByChapter(chapterId);
        container.innerHTML = '';
        
        if (!quizzes || quizzes.length === 0) {
            container.innerHTML = '<div style="text-align:center; padding:2rem; color:#888;"><i class="fas fa-check-circle" style="font-size:2rem; margin-bottom:10px;"></i><br>No quiz for this chapter.</div>';
            return;
        }

        const quiz = quizzes[0];
        
        // 2. Check for Previous Submission (Optional UX improvement)
        try {
            const existingSub = await api.submission.get(quiz.id, currentUser.id);
            if (existingSub && !existingSub.error) {
                container.innerHTML = `
                    <div class="quiz-card" style="text-align:center; border-color: #10b981;">
                        <h3 style="color:#10b981;">Quiz Submitted!</h3>
                        <p>Grade: <strong>${existingSub.grade !== undefined ? existingSub.grade : 'Pending'}</strong></p>
                        ${existingSub.feedback ? `<p class="feedback">Feedback: ${existingSub.feedback}</p>` : ''}
                    </div>`;
                return;
            }
        } catch (e) { /* No previous submission, continue */ }

        // 3. Check for Questions (MCQ vs File)
        const questions = await api.quiz.getQuestions(quiz.id);
        const isMCQ = questions && questions.length > 0;

        const form = document.createElement('form');
        form.id = 'quizForm';

        // === RENDER MCQ ===
        if (isMCQ) {
            for (const [index, q] of questions.entries()) {
                const qDiv = document.createElement('div');
                qDiv.className = 'quiz-card';

                // Fetch Answers
                let answers = [];
                try { answers = await api.answer.getByQuestion(q.id); } catch (e) {}

                // Image Logic
                let imageHtml = '';
                if (q.materialPath && q.materialPath.trim() !== "") {
                    const imgSrc = q.materialPath.startsWith('http') ? q.materialPath : `${IMAGE_BASE_URL}${q.materialPath}`;
                    imageHtml = `<div class="question-image-container"><img src="${imgSrc}" class="question-img"></div>`;
                }

                // Options Logic
                let optionsHtml = '';
                if (answers && answers.length > 0) {
                    optionsHtml = answers.map(ans => `
                        <label class="option-label">
                            <input type="radio" name="q_${q.id}" value="${ans.id}" required> 
                            <span>${ans.text}</span>
                        </label>
                    `).join('');
                }

                qDiv.innerHTML = `
                    <div class="question-text">${index + 1}. ${q.text}</div>
                    ${imageHtml}
                    <div class="options-group">${optionsHtml}</div>
                `;
                form.appendChild(qDiv);
            }
        } 
        // === RENDER FILE UPLOAD (Assignment) ===
        else {
            const uploadDiv = document.createElement('div');
            uploadDiv.className = 'quiz-card';
            uploadDiv.innerHTML = `
                <div class="question-text">Assignment Submission</div>
                <p style="margin-bottom:1rem; color:#666;">Please upload your solution file (PDF, ZIP).</p>
                <div class="upload-area" id="uploadArea" style="border: 2px dashed #ccc; padding: 2rem; text-align: center; border-radius: 8px; cursor: pointer;">
                    <i class="fas fa-cloud-upload-alt" style="font-size: 2rem; color: #aaa;"></i>
                    <p id="fileNameDisplay" style="margin-top: 10px;">Click to select file</p>
                    <input type="file" id="fileInput" hidden>
                </div>
            `;
            form.appendChild(uploadDiv);

            // File Selection Logic
            setTimeout(() => {
                const uploadArea = document.getElementById('uploadArea');
                const fileInput = document.getElementById('fileInput');
                const nameDisplay = document.getElementById('fileNameDisplay');

                if(uploadArea && fileInput) {
                    uploadArea.addEventListener('click', () => fileInput.click());
                    fileInput.addEventListener('change', () => {
                        if (fileInput.files.length > 0) {
                            nameDisplay.textContent = fileInput.files[0].name;
                            uploadArea.style.borderColor = '#5b4acf';
                            uploadArea.style.backgroundColor = '#f3f4f6';
                        }
                    });
                }
            }, 0);
        }

        // === SUBMIT BUTTON ===
        const submitBtn = document.createElement('button');
        submitBtn.className = 'btn-submit-quiz';
        submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit';
        submitBtn.type = 'submit';
        form.appendChild(submitBtn);
        container.appendChild(form);

        // === HANDLE SUBMISSION ===
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';

            try {
                // CASE A: MCQ Submission
                if (isMCQ) {
                    const formData = new FormData(form);
                    const answersMap = {};
                    
                    for (let [key, value] of formData.entries()) {
                        const questionId = key.replace('q_', '');
                        answersMap[questionId] = parseInt(value);
                    }

                    const result = await api.submission.submitMCQ(quiz.id, answersMap);
                    
                    container.innerHTML = `
                        <div class="quiz-card" style="text-align:center; border-color: #10b981; background: #ecfdf5;">
                            <i class="fas fa-check-circle" style="font-size:3rem; color:#10b981; margin-bottom:1rem;"></i>
                            <h3>Quiz Submitted!</h3>
                            <p style="font-size:1.2rem;">Your Score: <strong>${result.grade} / 20</strong></p>
                        </div>`;
                } 
                // CASE B: File Submission
                else {
                    const fileInput = document.getElementById('fileInput');
                    if (!fileInput.files || fileInput.files.length === 0) {
                        throw new Error("Please select a file to upload.");
                    }

                    submitBtn.textContent = 'Uploading File...';
                    const file = fileInput.files[0];
                    
                    // 1. Upload File
                    const uploadResult = await api.uploadFile(file, 'submission');
                    
                    // 2. Submit Record
                    submitBtn.textContent = 'Finalizing...';
                    await api.submission.submitFile(quiz.id, uploadResult.filePath);

                    container.innerHTML = `
                        <div class="quiz-card" style="text-align:center; border-color: #10b981;">
                            <i class="fas fa-file-check" style="font-size:3rem; color:#10b981; margin-bottom:1rem;"></i>
                            <h3>Assignment Submitted!</h3>
                            <p>Your file has been uploaded successfully. The teacher will grade it soon.</p>
                        </div>`;
                }

            } catch (error) {
                console.error("Submission Error:", error);
                alert("Failed to submit: " + error.message);
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Submit';
            }
        });

    } catch (error) {
        console.error("Quiz Load Error:", error);
        container.innerHTML = `<p style="color:red;">Failed to load quiz content.</p>`;
    }
}

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${(user.firstName || 'U').charAt(0)}</span>`;
    const commentAv = document.getElementById('commentUserAvatar');
    if(commentAv) commentAv.textContent = user.firstName.charAt(0);
}

function setupLogoutListener() {
    document.getElementById('userProfile').addEventListener('click', async () => {
        if (confirm("Log out?")) {
            try { await api.auth.logout(); } catch(e){}
            localStorage.removeItem('user');
            window.location.href = '../auth/login.html';
        }
    });
}

function setupSidebarToggle() {
    const btn = document.getElementById('mobileSidebarToggle');
    const sb = document.getElementById('sidebar');
    if(btn && sb) btn.addEventListener('click', () => sb.style.display = sb.style.display === 'block' ? 'none' : 'block');
}

function getRandomColor(id) {
    const colors = ['#5b4acf', '#10b981', '#f59e0b', '#ef4444', '#3b82f6'];
    return colors[id % colors.length] || colors[0];
}