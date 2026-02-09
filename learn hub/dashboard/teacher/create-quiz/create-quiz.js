import api from '../../../api.js';

const urlParams = new URLSearchParams(window.location.search);
const chapterId = urlParams.get('chapterId');

let currentUser = null;
let questionCount = 0;

document.addEventListener("DOMContentLoaded", async () => {
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }
    currentUser = JSON.parse(userJson);
    updateUserProfile(currentUser);

    if (!chapterId) {
        alert("No chapter ID specified.");
        window.history.back();
        return;
    }

    setupQuizTypeToggle();
    setupAddQuestion();
    setupFormSubmit();
    setupLogoutListener();
    
    document.getElementById('backBtn').addEventListener('click', () => {
        window.history.back();
    });
    
    document.getElementById('cancelBtn').addEventListener('click', () => {
        if (confirm("Discard changes?")) {
            window.history.back();
        }
    });
});

function setupQuizTypeToggle() {
    const mcqRadio = document.getElementById('mcqType');
    const fileRadio = document.getElementById('fileType');
    const mcqSection = document.getElementById('mcqSection');
    const fileSection = document.getElementById('fileSection');
    
    mcqRadio.addEventListener('change', () => {
        if (mcqRadio.checked) {
            mcqSection.style.display = 'block';
            fileSection.style.display = 'none';
        }
    });
    
    fileRadio.addEventListener('change', () => {
        if (fileRadio.checked) {
            mcqSection.style.display = 'none';
            fileSection.style.display = 'block';
        }
    });
}

function setupAddQuestion() {
    document.getElementById('addQuestionBtn').addEventListener('click', () => {
        addQuestionCard();
    });
    
    addQuestionCard();
}

function addQuestionCard() {
    questionCount++;
    const container = document.getElementById('questionsContainer');
    
    const card = document.createElement('div');
    card.className = 'question-card';
    card.dataset.questionId = questionCount;
    
    card.innerHTML = `
        <div class="question-header">
            <span class="question-number">Question ${questionCount}</span>
            <button type="button" class="btn-remove-question" onclick="removeQuestion(${questionCount})">
                <i class="fas fa-trash"></i> Remove
            </button>
        </div>
        
        <div class="form-group">
            <label class="form-label">Question Text *</label>
            <textarea 
                class="form-textarea question-text-input" 
                rows="3"
                placeholder="Enter your question here..."
                required
            ></textarea>
        </div>
        
        <div class="form-group">
            <label class="form-label">Question Score *</label>
            <input 
                type="number" 
                class="form-input question-score-input" 
                placeholder="e.g., 5"
                min="1"
                value="5"
                required
            >
        </div>
        
        <div class="form-group">
            <label class="form-label">Question Image (Optional)</label>
            <input 
                type="file" 
                class="form-input question-image-input" 
                accept="image/*"
            >
            <small style="color: var(--text-gray);">Upload an image if needed for this question</small>
        </div>
        
        <div class="form-group">
            <label class="form-label">Answer Options</label>
            <div class="answers-container" id="answers-${questionCount}">
            </div>
            <button type="button" class="btn-add-answer" onclick="addAnswer(${questionCount})">
                <i class="fas fa-plus"></i> Add Answer Option
            </button>
        </div>
    `;
    
    container.appendChild(card);
    
    for (let i = 0; i < 4; i++) {
        addAnswer(questionCount);
    }
}

window.removeQuestion = function(questionId) {
    if (questionCount === 1) {
        alert("You must have at least one question.");
        return;
    }
    
    const card = document.querySelector(`[data-question-id="${questionId}"]`);
    if (card) {
        card.remove();
        questionCount--;
        renumberQuestions();
    }
};

function renumberQuestions() {
    const cards = document.querySelectorAll('.question-card');
    cards.forEach((card, index) => {
        card.querySelector('.question-number').textContent = `Question ${index + 1}`;
    });
}

window.addAnswer = function(questionId) {
    const container = document.getElementById(`answers-${questionId}`);
    const answerCount = container.children.length + 1;
    
    const answerDiv = document.createElement('div');
    answerDiv.className = 'answer-item';
    
    answerDiv.innerHTML = `
        <input 
            type="text" 
            class="form-input answer-text-input" 
            placeholder="Answer option ${answerCount}"
            required
        >
        <input 
            type="checkbox" 
            class="answer-correct-checkbox"
            title="Mark as correct"
        >
        <label>Correct</label>
        <button type="button" class="btn-remove-answer" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    container.appendChild(answerDiv);
};

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

function setupFormSubmit() {
    document.getElementById('createQuizForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const submitBtn = document.querySelector('.btn-create');
        const originalHTML = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating...';
        
        try {
            const quizType = document.querySelector('input[name="quizType"]:checked').value;
            
            const availableFromValue = document.getElementById('availableFrom').value;
            const availableToValue = document.getElementById('availableTo').value;
            const totalPoints = parseInt(document.getElementById('quizTotalPoints').value) || 20;
            console.log("totalPoints:", totalPoints);
            const quizData = {
                chapterId: parseInt(chapterId),
                title: document.getElementById('quizTitle').value.trim(),
                description: document.getElementById('quizDescription').value.trim(),
                totalPoints: totalPoints,
                availableFrom: formatDateTimeForBackend(availableFromValue),
                availableTo: formatDateTimeForBackend(availableToValue)
            };
            
            const quizResult = await api.quiz.create(quizData);
            const quizId = quizResult.quizId || quizResult.id;
            
            if (!quizId) {
                throw new Error("Failed to retrieve quiz ID from server");
            }
            
            if (quizType === 'MCQ') {
                await createMCQQuestions(quizId);
            } else {
                await createFileAssignment(quizId, totalPoints);
            }
            
            alert("Quiz created successfully!");
            window.history.back();
            
        } catch (error) {
            console.error("Quiz Creation Error:", error);
            alert("Failed to create quiz: " + error.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHTML;
        }
    });
}

async function createMCQQuestions(quizId) {
    const questionCards = document.querySelectorAll('.question-card');
    
    for (const card of questionCards) {
        const questionText = card.querySelector('.question-text-input').value.trim();
        const questionScore = parseInt(card.querySelector('.question-score-input').value);
        const imageInput = card.querySelector('.question-image-input');
        
        if (!questionText) continue;
        
        let materialPath = '';
        if (imageInput.files && imageInput.files.length > 0) {
            const uploadResult = await api.uploadFile(imageInput.files[0], 'question_image');
            materialPath = uploadResult.filePath;
        }
        
        const questionData = {
            quizId: quizId,
            text: questionText,
            score: questionScore,
            materialPath: materialPath
        };
        
        const questionResult = await api.question.create(questionData);
        const questionId = questionResult.questionId || questionResult.id;
        
        if (!questionId) {
            throw new Error("Failed to retrieve question ID from server");
        }
        
        const answersContainer = card.querySelector('.answers-container');
        const answerItems = answersContainer.querySelectorAll('.answer-item');
        
        for (const answerItem of answerItems) {
            const answerText = answerItem.querySelector('.answer-text-input').value.trim();
            const isCorrect = answerItem.querySelector('.answer-correct-checkbox').checked;
            
            if (!answerText) continue;
            
            const answerData = {
                questionId: questionId,
                text: answerText,
                isCorrect: isCorrect
            };
            
            await api.answer.create(answerData);
        }
    }
}

async function createFileAssignment(quizId, totalPoints) {
    const fileInput = document.getElementById('assignmentFile');
    
    if (fileInput.files && fileInput.files.length > 0) {
        const uploadResult = await api.uploadFile(fileInput.files[0], 'assignment');
        
        const questionData = {
            quizId: quizId,
            text: "Please download the assignment file, complete it, and upload your solution.",
            score: totalPoints,
            materialPath: uploadResult.filePath
        };
        
        await api.question.create(questionData);
    } else {
        const questionData = {
            quizId: quizId,
            text: "Upload your completed assignment file.",
            score: totalPoints,
            materialPath: ''
        };
        
        await api.question.create(questionData);
    }
}

function setupLogoutListener() {
    document.getElementById('userProfile').addEventListener('click', async () => {
        if (confirm("Log out?")) {
            try { await api.auth.logout(); } catch(e) {}
            localStorage.removeItem('user');
            window.location.href = '../../../auth/login.html';
        }
    });
}

function updateUserProfile(user) {
    document.getElementById('userName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userRole').textContent = user.role;
    document.getElementById('userAvatar').innerHTML = `<span>${(user.firstName || 'U').charAt(0)}${(user.lastName || 'S').charAt(0)}</span>`;
}
