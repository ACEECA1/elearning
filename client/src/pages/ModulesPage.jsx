import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api, { courseApi } from '../api';
import ModuleItem from '../Components/ModuleItem';

export default function ModulesPage() {
    const { courseId } = useParams();
    const navigate = useNavigate();
    
    const [modules, setModules] = useState([]);
    const [error, setError] = useState('');
    const [isNotEnrolled, setIsNotEnrolled] = useState(false);
    
    // Enrollment State
    const [enrollmentCode, setEnrollmentCode] = useState('');
    const [enrollMessage, setEnrollMessage] = useState('');

    const fetchModules = () => {
        setError('');
        setIsNotEnrolled(false);
        
        courseApi.getModules(courseId)
            .then(res => setModules(res.data))
            .catch(err => {
                if (err.response && err.response.status === 403) {
                    setIsNotEnrolled(true);
                } else if (err.response && err.response.status === 401) {
                    navigate('/login');
                } else {
                    setError("Failed to load modules.");
                }
            });
    };

    useEffect(() => {
        fetchModules();
    }, [courseId]);

    const handleEnroll = async (e) => {
        e.preventDefault();
        setEnrollMessage('');
        try {
            const response = await api.post('/course/enroll', {
                courseId: parseInt(courseId),
                code: enrollmentCode
            });
            if (response.data.status === 'success') {
                setEnrollMessage('Success! Loading...');
                setTimeout(fetchModules, 1000);
            }
        } catch (err) {
            setEnrollMessage(err.response?.data?.message || 'Enrollment failed.');
        }
    };

    if (isNotEnrolled) {
        return (
            <div className="page-container">
                <button className="btn-back" onClick={() => navigate('/courses')}>← Back</button>
                <div className="card enroll-card">
                    <h2>Enrollment Required</h2>
                    <form onSubmit={handleEnroll}>
                        <input 
                            type="text" 
                            placeholder="Enrollment Code"
                            value={enrollmentCode}
                            onChange={(e) => setEnrollmentCode(e.target.value)}
                        />
                        <button type="submit" className="btn-primary">Enroll</button>
                    </form>
                    {enrollMessage && <p className="msg">{enrollMessage}</p>}
                </div>
            </div>
        );
    }

    return (
        <div className="page-container">
            <button className="btn-back" onClick={() => navigate('/courses')}>← Back to Courses</button>
            <h2>Course Modules</h2>
            {error && <div className="error-message">{error}</div>}
            
            <div className="module-list">
                {modules.map(mod => (
                    <ModuleItem key={mod.id} module={mod} courseId={courseId} />
                ))}
                {modules.length === 0 && <p>No modules found.</p>}
            </div>
        </div>
    );
}