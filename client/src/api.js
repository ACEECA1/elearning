import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL, 
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true 
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export const authApi = {
    login: (email, password) => api.post('/auth/login', { email, password }),
};

export const courseApi = {
    getAvailable: () => api.get('/course/available'),
    enroll: (data) => api.post('/course/enroll', data),
    getModules: (courseId) => api.get(`/module?courseId=${courseId}`),
    getChapter: (chapterId) => api.get(`/chapter?chapterId=${chapterId}`),
    getChapters: (moduleId) => api.get(`/chapter?moduleId=${moduleId}`),
    getMaterials: (chapterId) => api.get(`/material?chapterId=${chapterId}`),
};

// ✅ NEW: Forum & Comment APIs
export const forumApi = {
    getByChapter: (chapterId) => api.get(`/forum?chapterId=${chapterId}`),
};

export const commentApi = {
    getByForum: (forumId) => api.get(`/comment?forumId=${forumId}`),
    add: (data) => api.post('/comment', data),
    delete: (commentId) => api.delete('/comment', { data: { commentId } }),
    update: (commentId, content) => api.put('/comment', { commentId, content }),
};

export default api;