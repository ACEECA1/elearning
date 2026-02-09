import config from './config.js';
const API_BASE_URL = config.API_BASE_URL;

const api = {
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
    
    async uploadFile(file, type = 'course_content') {
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch(`${API_BASE_URL}/uploads?type=${type}`, {
                method: 'POST',
                credentials: 'include',
                body: formData
            });
            const data = await response.json();
            if (!response.ok || data.status === 'error') throw new Error(data.message || "Upload failed");
            return data;
        } catch (error) {
            console.error("Upload Error:", error);
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
        getById: (id) => api.request(`/user?id=${id}`, 'GET'),
        getAllStudents: () => api.request('/user?role=STUDENT', 'GET'),
        getAllTeachers: () => api.request('/user?role=TEACHER', 'GET'),
        getAllUsers: () => api.request('/user', 'GET'),
        getStudentsCount: () => api.request('/user/student/count', 'GET'),
        getTeachersCount: () => api.request('/user/teacher/count', 'GET'),
        getUsersCount: () => api.request('/user/count', 'GET'),
        createStudent: (studentData) => api.request('/user/student', 'POST', studentData),
        createTeacher: (teacherData) => api.request('/user/teacher', 'POST', teacherData),
        updateStudent: (studentData) => api.request('/user/student', 'PUT', studentData),
        updateTeacher: (teacherData) => api.request('/user/teacher', 'PUT', teacherData),
        deleteUser: (userId) => api.request('/user', 'DELETE', { userId })
    },

    course: {
        getMyCourses: () => api.request('/course/my-courses', 'GET'),
        getDetails: (courseId) => api.request(`/course/details?courseId=${courseId}`, 'GET'),
        getAvailableCourses: () => api.request('/course/available', 'GET'),
        getMyCoursesTeacher: () => api.request('/course/list', 'GET'),
        getAllCourses: () => api.request('/course/list', 'GET'),
        enroll: (courseId, code = "") => api.request('/course/enroll', 'POST', { courseId, code }),
        create: (courseData) => api.request('/course', 'POST', courseData),
        update: (courseData) => api.request('/course', 'PUT', courseData),
        delete: (courseId) => api.request('/course', 'DELETE', { courseId })
    },
    
    module: {
        getByCourse: (courseId) => api.request(`/module?courseId=${courseId}`, 'GET'),
        create: (moduleData) => api.request('/module', 'POST', moduleData),
        update: (moduleData) => api.request('/module', 'PUT', moduleData),
        delete: (moduleId) => api.request('/module', 'DELETE', { moduleId })
    },
    
    chapter: {
        getByModule: (moduleId) => api.request(`/chapter?moduleId=${moduleId}`, 'GET'),
        getDetails: (chapterId) => api.request(`/chapter?chapterId=${chapterId}`, 'GET'),
        create: (chapterData) => api.request('/chapter', 'POST', chapterData),
        update: (chapterData) => api.request('/chapter', 'PUT', chapterData),
        delete: (chapterId) => api.request('/chapter', 'DELETE', { chapterId })
    },
    
    material: {
        getByChapter: (chapterId) => api.request(`/material?chapterId=${chapterId}`, 'GET'),
        getDetails: (materialId) => api.request(`/material?materialId=${materialId}`, 'GET'),
        create: (materialData) => api.request('/material', 'POST', materialData),
        update: (materialData) => api.request('/material', 'PUT', materialData),
        delete: (materialId) => api.request('/material', 'DELETE', { materialId })
    },
    
    forum: {
        getByChapter: (chapterId) => api.request(`/forum?chapterId=${chapterId}`, 'GET'),
        create: (forumData) => api.request('/forum', 'POST', forumData),
        update: (forumData) => api.request('/forum', 'PUT', forumData),
        delete: (forumId) => api.request('/forum', 'DELETE', { forumId })
    },
    
    comment: {
        getByForum: (forumId) => api.request(`/comment?forumId=${forumId}`, 'GET'),
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

    quiz: {
        getByChapter: (chapterId) => api.request(`/quiz?chapterId=${chapterId}`, 'GET'),
        getDetails: (quizId) => api.request(`/quiz?quizId=${quizId}`, 'GET'),
        create: (quizData) => api.request('/quiz', 'POST', quizData),
        update: (quizData) => api.request('/quiz', 'PUT', quizData),
        delete: (quizId) => api.request('/quiz', 'DELETE', { quizId }),
        getQuestions: (quizId) => api.request(`/question?quizId=${quizId}`, 'GET')
    },
    
    question: {
        getByQuiz: (quizId) => api.request(`/question?quizId=${quizId}`, 'GET'),
        getDetails: (questionId) => api.request(`/question?questionId=${questionId}`, 'GET'),
        create: (questionData) => api.request('/question', 'POST', questionData),
        update: (questionData) => api.request('/question', 'PUT', questionData),
        delete: (questionId) => api.request('/question', 'DELETE', { questionId })
    },
    
    answer: {
        getByQuestion: (questionId) => api.request(`/answer?questionId=${questionId}`, 'GET'),
        create: (answerData) => api.request('/answer', 'POST', answerData),
        update: (answerData) => api.request('/answer', 'PUT', answerData),
        delete: (answerId) => api.request('/answer', 'DELETE', { answerId })
    },
    
    submission: {
        submitFile: (quizId, filePath) => api.request('/submission', 'POST', {
            quizId,
            type: 'FILE',
            filePath
        }),
        submitMCQ: (quizId, answersMap) => api.request('/submission', 'POST', {
            quizId,
            type: 'MCQ',
            answers: answersMap
        }),
        get: (quizId, studentId) => api.request(`/submission?quizId=${quizId}&studentId=${studentId}`, 'GET'),
        grade: (studentId, quizId, grade, feedback) => api.request('/submission', 'PUT', {
            studentId,
            quizId,
            grade,
            feedback
        })
    }
};

export default api;