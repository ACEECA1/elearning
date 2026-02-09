import api from './api.js';
import config from './config.js';

const BASE_URL = config.API_BASE_URL;

// Mobile Menu Toggle
const mobileMenuToggle = document.getElementById('mobileMenuToggle');
const navActions = document.querySelector('.nav-actions');

if (mobileMenuToggle) {
    mobileMenuToggle.addEventListener('click', () => {
        navActions.classList.toggle('active');
    });
}

// Fetch Stats from Backend
async function loadStats() {
    const coursesCard = document.getElementById('coursesCard');
    const studentsCard = document.getElementById('studentsCard');
    const teachersCard = document.getElementById('teachersCard');

    // Add loading state
    [coursesCard, studentsCard, teachersCard].forEach(card => {
        card.classList.add('loading');
    });

    try {
        const courseStats = await api.course.count();
        const teacherStats = await api.user.getTeachersCount();
        const studentStats = await api.user.getStudentsCount();
        // Remove loading state
        [coursesCard, studentsCard, teachersCard].forEach(card => {
            card.classList.remove('loading');
        });

        // Update data attributes
        coursesCard.querySelector('.stat-number').dataset.target = courseStats.courseCount || 0;
        studentsCard.querySelector('.stat-number').dataset.target = studentStats.studentCount || 0;
        teachersCard.querySelector('.stat-number').dataset.target = teacherStats.teacherCount || 0;

        // Trigger animation observer
        [coursesCard, studentsCard, teachersCard].forEach(card => {
            statsObserver.observe(card);
        });

    } catch (error) {
        console.error('Error loading stats:', error);
        
        // Remove loading state and show error
        [coursesCard, studentsCard, teachersCard].forEach(card => {
            card.classList.remove('loading');
            card.querySelector('.stat-number').textContent = '--';
        });
    }
}

// Animated Counter
function animateCounter(element, target) {
    let current = 0;
    const increment = target / 100;
    const duration = 2000;
    const stepTime = duration / 100;

    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        
        // Format number with commas if > 1000
        const formatted = Math.floor(current).toLocaleString();
        element.textContent = formatted;
    }, stepTime);
}

// Intersection Observer for Stats Animation
const statsObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const statCard = entry.target;
            const numberElement = statCard.querySelector('.stat-number');
            const targetNumber = parseInt(numberElement.dataset.target);
            
            if (!isNaN(targetNumber)) {
                animateCounter(numberElement, targetNumber);
            }
            
            statsObserver.unobserve(statCard);
        }
    });
}, { threshold: 0.5 });

// Load stats on page load
document.addEventListener('DOMContentLoaded', () => {
    loadStats();
});

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});
