const BASE_API_URL = "http://192.168.100.3:8080/api/";

export default function MaterialItem({ material }) {
    // Determine icon based on type
    const getIcon = (type) => {
        if (type === 'VIDEO') return '🎥';
        if (type === 'PDF') return '📄';
        return 'link';
    };

    return (
        <div className="material-item">
            <span className="material-icon">{getIcon(material.type)}</span>
            <a 
                href={`${BASE_API_URL}${material.path}`} 
                target="_blank" 
                rel="noopener noreferrer"
                className="material-link"
            >
                {material.title}
            </a>
        </div>
    );
}