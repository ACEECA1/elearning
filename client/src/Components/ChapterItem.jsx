import { useNavigate } from 'react-router-dom';

export default function ChapterItem({ chapter, courseId, moduleId }) {
    const navigate = useNavigate();
    
    const handleClick = () => {
        navigate(`/course/${courseId}/module/${moduleId}/chapter/${chapter.id}`);
    };

    return (
        <div className="chapter-item" onClick={handleClick}>
            <div className="chapter-header">
                <span className="play-icon">▶</span>
                <h4>{chapter.title}</h4>
            </div>
            {/* Optional: Show small subtitle */}
            <p className="chapter-preview">Click to watch lesson</p>
        </div>
    );
}