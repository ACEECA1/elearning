const API_BASE_URL = 'http://192.168.100.3:8080/api'; // Adjust port/context path as needed

const api = {
    // Helper for making requests
    async request(endpoint, method = 'GET', body = null) {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            method,
            headers,
            credentials: 'include'
        };

        if (body) {
            config.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || 'Something went wrong');
            }
            return data;
        } catch (error) {
            throw error;
        }
    },

    auth: {
        login: (email, password) => {
            return api.request('/auth/login', 'POST', { email, password });
        },
        
        logout: () => {
            return api.request('/auth/logout', 'POST', {});
        },

        sendVerificationCode: (email) => {
            return api.request('/auth/send-code', 'POST', { email });
        },

        registerStudent: (studentData) => {
            return api.request('/auth/register/student', 'POST', studentData);
        },

        registerTeacher: (teacherData) => {
            return api.request('/auth/register/teacher', 'POST', teacherData);
        }
    },
    user: {
        getProfile: () => api.request('/user/profile', 'GET'),
        getById: (id) => api.request(`/user?id=${id}`, 'GET')
    },

    course: {
        // GET /api/course/my-courses
        getMyCourses: () => api.request('/course/my-courses', 'GET'),
        getDetails: (courseId) => api.request(`/course/details?courseId=${courseId}`, 'GET'),
        // GET /api/course/available
        getAvailableCourses: () => api.request('/course/available', 'GET'),

        // POST /api/course/enroll
        // Body: { "courseId": 123, "code": "code" }
        enroll: (courseId, code = "") => api.request('/course/enroll', 'POST', { courseId, code })
    },
    module: {
        getByCourse: (courseId) => api.request(`/module?courseId=${courseId}`, 'GET')
    },
    chapter: {
        getByModule: (moduleId) => api.request(`/chapter?moduleId=${moduleId}`, 'GET'),
        getDetails: (chapterId) => api.request(`/chapter?chapterId=${chapterId}`, 'GET')
    },
    material: {
        // GET /api/material?chapterId=...
        getByChapter: (chapterId) => api.request(`/material?chapterId=${chapterId}`, 'GET'),
        getDetails: (materialId) => api.request(`/material?materialId=${materialId}`, 'GET')
    },
    forum: {
        // Find existing forum for this chapter
        getByChapter: (chapterId) => api.request(`/forum?chapterId=${chapterId}`, 'GET'),
        // Create one if it doesn't exist
        create: (chapterId, title) => api.request('/forum', 'POST', { chapterId, title })
    },
    comment: {
        // Get comments for a forum
        getByForum: (forumId) => api.request(`/comment?forumId=${forumId}`, 'GET'),
        // Post a comment
        add: (forumId, content, parentCommentId = 0) => {
            const isReply = parentCommentId > 0;
            return api.request('/comment', 'POST', { 
                forumId, 
                content, 
                isReply, 
                parentCommentId 
            });
        },
        update: (commentId, content) => api.request('/comment', 'PUT', { commentId, content }),

        delete: (commentId) => api.request('/comment', 'DELETE', { commentId })
    },

    // --- NEW: Quiz ---
    quiz: {
        getByChapter: (chapterId) => api.request(`/quiz?chapterId=${chapterId}`, 'GET'),
        getQuestions: (quizId) => api.request(`/question?quizId=${quizId}`, 'GET'),
        submit: (submissionData) => api.request('/submission', 'POST', submissionData)
    },
    answer: {
        // GET /api/answer?questionId=...
        getByQuestion: (questionId) => api.request(`/answer?questionId=${questionId}`, 'GET')
    },
    submission: {
        // Submit File Assignment
        submitFile: (quizId, filePath) => api.request('/submission', 'POST', {
            quizId,
            type: 'FILE',
            filePath
        }),
        // Submit MCQ Answers
        submitMCQ: (quizId, answersMap) => api.request('/submission', 'POST', {
            quizId,
            type: 'MCQ',
            answers: answersMap
        }),
        // Check previous submission
        get: (quizId, studentId) => api.request(`/submission?quizId=${quizId}&studentId=${studentId}`, 'GET')
    }
};

export default api;