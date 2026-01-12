import { useEffect, useState } from 'react';
import { courseApi } from '../api';
import ChapterItem from './ChapterItem';

const BASE_API_URL = import.meta.env.VITE_API_URL;

export default function ModuleItem({ module, courseId }) { // Receive courseId prop
    const [chapters, setChapters] = useState([]);
    const [isOpen, setIsOpen] = useState(false);

    useEffect(() => {
        if (isOpen) {
            courseApi.getChapters(module.id)
                .then(res => setChapters(res.data))
                .catch(err => console.error(err));
        }
    }, [isOpen, module.id]);

    return (
        <div className="module-item">
            <div className="module-header" onClick={() => setIsOpen(!isOpen)}>
                <div className="module-info">
                    <span className="module-number">#{module.orderIndex}</span>
                    <h3>{module.title}</h3>
                </div>
                {module.imagePath && (
                    <img 
                        src={`${BASE_API_URL}${module.imagePath}`} 
                        alt="module thumb" 
                        className="module-thumb"
                    />
                )}
            </div>

            {isOpen && (
                <div className="module-body">
                    <p>{module.description}</p>
                    <div className="chapter-list">
                        {chapters.map(chap => (
                            <ChapterItem 
                                key={chap.id} 
                                chapter={chap} 
                                courseId={courseId} // ✅ Pass props
                                moduleId={module.id} // ✅ Pass props
                            />
                        ))}
                        {chapters.length === 0 && <p className="empty-msg">No chapters yet.</p>}
                    </div>
                </div>
            )}
        </div>
    );
}