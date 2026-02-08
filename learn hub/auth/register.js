import api from '../api.js';

const emailInput = document.getElementById('email');
const verifyEmailBtn = document.getElementById('verifyEmailBtn');
const verificationCodeGroup = document.getElementById('verificationCodeGroup');
const verificationCodeInput = document.getElementById('verificationCode');
const emailHint = document.getElementById('emailHint');
const codeHint = document.getElementById('codeHint');

// New: Username Input
const usernameInput = document.getElementById('username');

let codeSent = false;

if (verifyEmailBtn) {
    verifyEmailBtn.addEventListener('click', async () => {
        const email = emailInput.value.trim();
        
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            emailHint.textContent = 'Please enter a valid email address';
            emailHint.className = 'field-hint error';
            return;
        }
        
        verifyEmailBtn.disabled = true;
        verifyEmailBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
        
        try {
            await api.auth.sendVerificationCode(email);
            
            codeSent = true;
            emailHint.textContent = `Verification code sent to ${email}`;
            emailHint.className = 'field-hint success';
            
            verificationCodeGroup.style.display = 'block';
            verificationCodeInput.required = true;
            
            verifyEmailBtn.innerHTML = '<i class="fas fa-check-circle"></i> Code Sent';
            verificationCodeInput.focus();
            emailInput.readOnly = true;

        } catch (error) {
            console.error(error);
            emailHint.textContent = error.message || 'Failed to send code.';
            emailHint.className = 'field-hint error';
            verifyEmailBtn.disabled = false;
            verifyEmailBtn.innerHTML = 'Verify Email';
        }
    });
}

if (verificationCodeInput) {
    verificationCodeInput.addEventListener('input', (e) => {
        const code = e.target.value.trim();
        if (code.length === 6) {
            verificationCodeInput.style.borderColor = 'var(--success-green)';
            codeHint.textContent = 'Code entered.';
            codeHint.className = 'field-hint success';
        } else {
            verificationCodeInput.style.borderColor = '';
            codeHint.textContent = 'Code must be 6 characters';
            codeHint.className = 'field-hint';
        }
    });
}

const roleSelect = document.getElementById('role');
const studentFields = document.getElementById('studentFields');
const teacherFields = document.getElementById('teacherFields');

const studentCardInput = document.getElementById('studentCardNumber');
const academicYearInput = document.getElementById('academicYear');
const domainInput = document.getElementById('domain');
const gradeInput = document.getElementById('grade');

if (roleSelect) {
    roleSelect.addEventListener('change', (e) => {
        const selectedRole = e.target.value;
        
        studentFields.style.display = 'none';
        teacherFields.style.display = 'none';
        
        if(studentCardInput) studentCardInput.required = false;
        if(academicYearInput) academicYearInput.required = false;
        if(domainInput) domainInput.required = false;
        if(gradeInput) gradeInput.required = false;
        
        if (selectedRole === 'student') {
            studentFields.style.display = 'block';
            if(studentCardInput) studentCardInput.required = true;
            if(academicYearInput) academicYearInput.required = true;
        } else if (selectedRole === 'teacher') {
            teacherFields.style.display = 'block';
            if(domainInput) domainInput.required = true;
            if(gradeInput) gradeInput.required = true;
        }
    });
}

const signupForm = document.getElementById('signupForm');
const firstName = document.getElementById('firstName');
const lastName = document.getElementById('lastName');
const password = document.getElementById('password');
const confirmPassword = document.getElementById('confirmPassword');
const agreeTerms = document.getElementById('agreeTerms');

if (signupForm) {
    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if (!codeSent) {
            alert('Please verify your email address first.');
            verifyEmailBtn.focus();
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        // Collect form data including Username
        const baseData = {
            firstName: firstName.value.trim(),
            lastName: lastName.value.trim(),
            username: usernameInput.value.trim(), // <--- NEW FIELD
            email: emailInput.value.trim(),
            password: password.value,
            verificationCode: verificationCodeInput.value.trim()
        };
        
        const role = roleSelect.value;
        let apiPromise;

        if (role === 'student') {
            const studentData = {
                ...baseData,
                studentCardNumber: studentCardInput.value.trim(),
                academicYear: academicYearInput.value
            };
            apiPromise = api.auth.registerStudent(studentData);
        } else if (role === 'teacher') {
            const teacherData = {
                ...baseData,
                domain: domainInput.value.trim(),
                grade: gradeInput.value.trim()
            };
            apiPromise = api.auth.registerTeacher(teacherData);
        }

        const submitBtn = signupForm.querySelector('.btn-submit');
        const originalText = submitBtn.textContent;
        submitBtn.textContent = 'Creating Account...';
        submitBtn.disabled = true;
        
        try {
            const response = await apiPromise;
            
            if (response.status === 'success') {
                submitBtn.textContent = '✓ Account Created!';
                submitBtn.style.background = '#10b981';
                
                setTimeout(() => {
                    alert(`Welcome, ${baseData.username}! Your account has been created.`);
                    window.location.href = '../auth/login.html';
                }, 1000);
            }
        } catch (error) {
            console.error("Registration error:", error);
            alert(`Registration Failed: ${error.message}`);
            
            submitBtn.textContent = originalText;
            submitBtn.style.background = '';
            submitBtn.disabled = false;
        }
    });
}

function validateForm() {
    if (firstName.value.trim().length < 2) {
        showError(firstName, 'First name must be at least 2 characters');
        return false;
    }
    if (lastName.value.trim().length < 2) {
        showError(lastName, 'Last name must be at least 2 characters');
        return false;
    }
    
    // Username Validation
    if (usernameInput.value.trim().length < 3) {
        showError(usernameInput, 'Username must be at least 3 characters');
        return false;
    }

    if (password.value.length < 8) {
        showError(password, 'Password must be at least 8 characters');
        return false;
    }
    if (password.value !== confirmPassword.value) {
        showError(confirmPassword, 'Passwords do not match');
        return false;
    }
    if (!roleSelect.value) {
        showError(roleSelect, 'Please select a role');
        return false;
    }
    
    if (roleSelect.value === 'student') {
        if (!studentCardInput.value.trim()) {
            showError(studentCardInput, 'Student card number is required');
            return false;
        }
        if (!academicYearInput.value) {
            showError(academicYearInput, 'Academic year is required');
            return false;
        }
    } else if (roleSelect.value === 'teacher') {
        if (!domainInput.value) {
            showError(domainInput, 'Domain/Department is required');
            return false;
        }
        if (!gradeInput.value) {
            showError(gradeInput, 'Grade/Rank is required');
            return false;
        }
    }
    
    if (!agreeTerms.checked) {
        alert('Please agree to the Terms of Service and Privacy Policy');
        return false;
    }
    
    return true;
}

function showError(input, message) {
    alert(message);
    input.focus();
}

if (confirmPassword) {
    confirmPassword.addEventListener('input', () => {
        if (confirmPassword.value && password.value !== confirmPassword.value) {
            confirmPassword.style.borderColor = '#ef4444';
        } else {
            confirmPassword.style.borderColor = '';
        }
    });
}

window.addEventListener('load', () => {
    const signupCard = document.querySelector('.signup-card');
    if (signupCard) {
        signupCard.style.animation = 'fadeInUp 0.6s ease';
    }
});

const animStyle = document.createElement('style');
animStyle.textContent = `
    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
`;
document.head.appendChild(animStyle);