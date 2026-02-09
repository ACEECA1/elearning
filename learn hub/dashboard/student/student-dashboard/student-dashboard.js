document.addEventListener('DOMContentLoaded', () => {
    //redirect to courses/courses.html
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = '../../../auth/login.html';
        return;
    }
    const user = JSON.parse(userJson);
    if (user.role !== 'STUDENT') {
        alert("Access Denied: Students only.");
        window.location.href = '../../../index.html';
        return;
    }
    window.location.href = '../courses/courses.html';
});