import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { courseApi } from '../api';
import CourseCard from '../Components/CourseCard';

export default function CoursesPage() {
    const [courses, setCourses] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        courseApi.getAvailable()
            .then(res => setCourses(res.data))
            .catch(err => console.error(err));
    }, []);

    return (
        <div className="page-container">
            <header className="page-header">
                <h1>Available Courses</h1>
                <button className="btn-secondary" onClick={() => navigate('/login')}>Logout</button>
            </header>

            <div className="course-grid">
                {courses.map(course => (
                    <CourseCard key={course.id} course={course} />
                ))}
            </div>
            
            {courses.length === 0 && <p>No courses available right now.</p>}
        </div>
    );
}