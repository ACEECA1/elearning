import { useEffect, useState } from 'react';
import { forumApi, commentApi } from '../api';
import CommentItem from './CommentItem';
import { useAuth } from '../context/AuthContext';

export default function ForumSection({ chapterId }) {
    const [forums, setForums] = useState([]);
    const [activeForum, setActiveForum] = useState(null);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [loading, setLoading] = useState(true);
    
    const { user } = useAuth();
    const currentUserId = user? user.id : 0;


    // 1. Fetch Forums when Chapter changes
    useEffect(() => {
        setLoading(true);
        forumApi.getByChapter(chapterId)
            .then(res => {
                setForums(res.data);
                // Automatically select the first forum if available
                if (res.data.length > 0) {
                    setActiveForum(res.data[0]);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Error loading forums", err);
                setLoading(false);
            });
    }, [chapterId]);

    // 2. Fetch Comments when Active Forum changes
    const fetchComments = () => {
        if (!activeForum) return;
        commentApi.getByForum(activeForum.id)
            .then(res => setComments(res.data))
            .catch(err => console.error("Error loading comments", err));
    };

    useEffect(() => {
        fetchComments();
    }, [activeForum]);

    // 3. Handle Add Comment (Root)
    const handlePostComment = async (e) => {
        e.preventDefault();
        if (!activeForum) return;

        try {
            await commentApi.add({
                forumId: activeForum.id,
                content: newComment,
                isReply: false
            });
            setNewComment('');
            fetchComments(); // Refresh list
        } catch (err) {
            alert("Failed to post comment.");
        }
    };

    // 4. Handle Reply
    const handleReply = async (parentCommentId, content) => {
        if (!activeForum) return;
        try {
            await commentApi.add({
                forumId: activeForum.id,
                content: content,
                isReply: true,
                parentCommentId: parentCommentId
            });
            fetchComments();
        } catch (err) {
            alert("Failed to post reply.");
        }
    };

    // 5. Handle Delete
    const handleDelete = async (commentId) => {
        if (!window.confirm("Delete this comment?")) return;
        try {
            await commentApi.delete(commentId);
            fetchComments();
        } catch (err) {
            alert("Failed to delete comment.");
        }
    };

    const handleEdit = async (commentId, newContent) => {
        try {
            await commentApi.update(commentId, newContent);
            fetchComments(); // Refresh list to show changes
        } catch (err) {
            alert("Failed to update comment.");
            console.error(err);
        }
    };
    if (loading) return <p>Loading discussion...</p>;
    if (forums.length === 0) return <div className="forum-empty"><p>No discussion topics for this chapter.</p></div>;

    return (
        <div className="forum-section">
            <div className="forum-header-row">
                <h3>Discussion</h3>
                {forums.length > 1 && (
                    <select 
                        className="forum-selector"
                        onChange={(e) => setActiveForum(forums.find(f => f.id === parseInt(e.target.value)))}
                        value={activeForum?.id}
                    >
                        {forums.map(f => <option key={f.id} value={f.id}>{f.title}</option>)}
                    </select>
                )}
            </div>

            {/* Post Main Comment */}
            <form onSubmit={handlePostComment} className="main-comment-form">
                <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="Ask a question or share your thoughts..."
                    required
                />
                <button type="submit" className="btn-primary">Post Comment</button>
            </form>

            {/* Comment List */}
            <div className="comments-list">
                {comments.map(comment => (
                    <CommentItem 
                        key={comment.id} 
                        comment={comment} 
                        onReply={handleReply} 
                        onDelete={handleDelete}
                        onEdit={handleEdit}
                        currentUserId={currentUserId}
                    />
                ))}
                {comments.length === 0 && <p className="no-comments">Be the first to comment!</p>}
            </div>
        </div>
    );
}