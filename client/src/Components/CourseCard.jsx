import { useNavigate } from 'react-router-dom';

const BASE_API_URL = import.meta.env.VITE_API_URL;

export default function CourseCard({ course }) {
    const navigate = useNavigate();

    return (
        <div className="card course-card">
            <div className="card-img-wrapper">
                <img 
                    src={course.thumbnailPath ? `${BASE_API_URL}${course.thumbnailPath}` : "https://placehold.co/600x400?text=No+Image"} 
                    alt={course.title}
                    onError={(e) => { e.target.src = "https://placehold.co/600x400?text=Error"; }}
                />
            </div>
            <div className="card-body">
                <h3>{course.title}</h3>
                <p className="course-desc">{course.description}</p>
                <span className="badge">{course.targetAudience}</span>
            </div>
            <div className="card-footer">
                <button 
                    className="btn-primary"
                    onClick={() => navigate(`/course/${course.id}/modules`)}
                >
                    View Course
                </button>
            </div>
        </div>
    );
}