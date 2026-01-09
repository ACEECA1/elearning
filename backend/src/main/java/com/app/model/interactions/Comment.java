package com.app.model.interactions;
import java.util.List;
import com.app.model.users.User;

public class Comment {
    private int id;
    private int userId;
    private int forumId;
    private String content;
    private boolean isReply;
    private int parentCommentId;
    private String createdAt;
    private int likes;
    private int dislikes;
    private boolean isModified;
    private Forum forum;
    private User user;
    private List<Comment> replies;
    public Comment(int id, String content, String createdAt, int likes, int dislikes,
                   boolean isModified, int forumId, int userId) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isModified = isModified;
        this.forumId = forumId;
        this.userId = userId;
        this.isReply = false;
        this.parentCommentId = -1;
    }
    public Comment(String content, String createdAt, int likes, int dislikes,
                   boolean isModified, int forumId, int userId) {
        this.id = 0; 
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isModified = isModified;
        this.forumId = forumId;
        this.userId = userId;
        this.isReply = false;
        this.parentCommentId = -1;
    }
    public Comment(String content, String createdAt, int likes, int dislikes,
                   boolean isModified, int forumId, int userId,
                   boolean isReply, int parentCommentId) {
        this.id = 0; 
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isModified = isModified;
        this.forumId = forumId;
        this.userId = userId;
        this.isReply = isReply;
        this.parentCommentId = parentCommentId;
    }
    public Comment(int id, String content, String createdAt, int likes, int dislikes,
                   boolean isModified, int forumId, int userId,
                   boolean isReply, int parentCommentId) {
        this.id = id; 
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isModified = isModified;
        this.forumId = forumId;
        this.userId = userId;
        this.isReply = isReply;
        this.parentCommentId = parentCommentId;
    }
    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public int getLikes() {
        return likes;
    }
    public void setLikes(int likes) {
        this.likes = likes;
    }
    public int getDislikes() {
        return dislikes;
    }
    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }
    public boolean isModified() {
        return isModified;
    }
    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }
    public int getForumId() {
        return forumId;
    }
    public void setForumId(int forumId) {
        this.forumId = forumId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public Forum getForum() {
        return forum;
    }
    public void setForum(Forum forum) {
        this.forum = forum;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public boolean isReply() {
        return isReply;
    }
    public void setReply(boolean isReply) {
        this.isReply = isReply;
    }
    public int getParentCommentId() {
        return parentCommentId;
    }
    public void setParentCommentId(int parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
    public List<Comment> getReplies() {
        return replies;
    }
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
}
