import api from '../api.js';

document.addEventListener("DOMContentLoaded", () => {
    const f = document.getElementById("loginForm");
    
    if (f) {
        f.addEventListener("submit", async (e) => {
            e.preventDefault();
            
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value;
            const submitBtn = f.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.textContent;

            submitBtn.disabled = true;
            submitBtn.textContent = "Signing in...";

            try {
                const response = await api.auth.login(email, password);
                
                if (response.status === 'success') {
                    const user = response.user;
                    
                    localStorage.setItem('user', JSON.stringify(user));

                    const role = user.role ? user.role.toUpperCase() : '';

                    if (role === 'ADMIN') {
                        location.href = "../dashboard/admin/admin-dashboard/admin-dashboard.html";
                    } else if (role === 'TEACHER') {
                        location.href = "../dashboard/teacher/teacher-dashboard/teacher-dashboard.html";
                    } else if (role === 'STUDENT') {
                        location.href = "../dashboard/student/student-dashboard/student-dashboard.html";
                    } else {
                        alert("Login successful, but user role is unknown.");
                    }
                }
            } catch (error) {
                alert(error.message || "Invalid credentials");
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = originalBtnText;
            }
        });
    }
});