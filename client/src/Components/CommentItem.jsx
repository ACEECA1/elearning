import { useState } from 'react';

export default function CommentItem({ comment, onReply, onDelete, onEdit,currentUserId }) {
    const [isReplying, setIsReplying] = useState(false);
    const [replyContent, setReplyContent] = useState('');
    // Edit State
    const [isEditing, setIsEditing] = useState(false);
    const [editContent, setEditContent] = useState(comment.content);

    console.log("Rendering CommentItem for comment ID:", comment.id);
    console.log("Current User ID:", currentUserId);
    //Handle Reply Submit
    const handleSubmitReply = (e) => {
        e.preventDefault();
        onReply(comment.id, replyContent);
        setIsReplying(false);
        setReplyContent('');
    };

    //Handle Edit Submit
    const handleSaveEdit = (e) => {
        e.preventDefault();
        onEdit(comment.id, editContent);
        setIsEditing(false);
    };

    const formatDate = (dateString) => {
        if (!dateString) return '';
        return new Date(dateString).toLocaleString();
    };

    return (
        <div className="comment-item">
            <div className="comment-card">
                <div className="comment-header">
                    <span className="comment-author">User #{comment.userId}</span>
                    <span className="comment-date">{formatDate(comment.createdAt)}</span>
                    {/* {comment.isModified ? (<span>Modified</span>) : null} */}
                </div>
                
                {/* LOGIC: Show Form if editing, otherwise show text */}
                {isEditing ? (
                    <form onSubmit={handleSaveEdit} className="edit-form">
                        <textarea 
                            value={editContent} 
                            onChange={(e) => setEditContent(e.target.value)}
                            className="edit-textarea"
                            required
                        />
                        <div className="edit-actions">
                            <button type="submit" className="btn-primary btn-sm">Save</button>
                            <button type="button" className="btn-secondary btn-sm" onClick={() => setIsEditing(false)}>Cancel</button>
                        </div>
                    </form>
                ) : (
                    <p className="comment-body">{comment.content}</p>
                )}

                {/* Action Buttons */}
                <div className="comment-actions">
                    <button className="btn-link" onClick={() => setIsReplying(!isReplying)}>
                        {isReplying ? 'Cancel Reply' : 'Reply'}
                    </button>
                    
                    {/*Check Ownership for Edit/Delete */}
                    {currentUserId === comment.userId && !isEditing && (
                        <div className="owner-actions">
                            <button className="btn-link" onClick={() => setIsEditing(true)}>
                                Edit
                            </button>
                            <button className="btn-link btn-danger" onClick={() => onDelete(comment.id)}>
                                Delete
                            </button>
                        </div>
                    )}
                </div>

                {/* Reply Form */}
                {isReplying && (
                    <form onSubmit={handleSubmitReply} className="reply-form">
                        <textarea
                            value={replyContent}
                            onChange={(e) => setReplyContent(e.target.value)}
                            placeholder="Write a reply..."
                            required
                        />
                        <button type="submit" className="btn-primary btn-sm">Post Reply</button>
                    </form>
                )}
            </div>

            {/* Recursive Replies */}
            {comment.replies && comment.replies.length > 0 && (
                <div className="comment-replies">
                    {comment.replies.map(reply => (
                        <CommentItem 
                            key={reply.id}
                            comment={reply}
                            onReply={onReply}
                            onDelete={onDelete}
                            onEdit={onEdit}
                            currentUserId={currentUserId}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}