import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { courseApi } from '../api';
import MaterialItem from '../Components/MaterialItem';
import ForumSection from '../Components/ForumSection'; // ✅ Import

const BASE_API_URL = import.meta.env.VITE_API_URL;

export default function ChapterDetailsPage() {
    const { courseId, moduleId, chapterId } = useParams();
    const navigate = useNavigate();

    const [chapter, setChapter] = useState(null);
    const [materials, setMaterials] = useState([]);
    const [videoMaterial, setVideoMaterial] = useState(null);

    useEffect(() => {
        // 1. Get Chapter Details
        courseApi.getChapter(chapterId)
            .then(res => setChapter(res.data))
            .catch(err => console.error(err));

        // 2. Get Materials
        courseApi.getMaterials(chapterId)
            .then(res => {
                const allMaterials = res.data;
                const video = allMaterials.find(m => m.type === 'VIDEO' || m.path.endsWith('.mp4'));
                setVideoMaterial(video);
                setMaterials(allMaterials);
            })
            .catch(err => console.error(err));
    }, [chapterId]);

    if (!chapter) return <div className="page-container">Loading...</div>;

    return (
        <div className="page-container">
            <button className="btn-back" onClick={() => navigate(`/course/${courseId}/modules`)}>
                ← Back to Modules
            </button>

            {/* Video Section */}
            <div className="video-section">
                <h1>{chapter.title}</h1>
                <div className="video-player-wrapper">
                    {videoMaterial ? (
                        <video 
                            controls 
                            autoPlay
                            className="main-video"
                            src={`${BASE_API_URL}${videoMaterial.path}`}
                        >
                            Your browser does not support the video tag.
                        </video>
                    ) : (
                        <div className="no-video-placeholder">
                            <p>No video available for this lesson.</p>
                        </div>
                    )}
                </div>
                <div className="chapter-description-box">
                    <h3>About this lesson</h3>
                    <p>{chapter.content || "No description provided."}</p>
                </div>
            </div>

            {/* Materials Section */}
            <div className="materials-section">
                <h3>Course Materials</h3>
                <div className="materials-list">
                    {materials.map(mat => (
                        <MaterialItem key={mat.id} material={mat} />
                    ))}
                    {materials.length === 0 && <p>No materials attached.</p>}
                </div>
            </div>

            <hr className="divider" />

            {/* ✅ NEW: Forum / Comments Section */}
            <ForumSection chapterId={chapterId} />
        </div>
    );
}