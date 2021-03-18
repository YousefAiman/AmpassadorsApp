package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.Map;

@IgnoreExtraProperties
public class Comment implements Serializable {

  @PropertyName("comment")
  private String comment;
  @PropertyName("userId")
  private String userId;
  @PropertyName("time")
  private long time;
  @PropertyName("replies")
  private int replies;
  @PropertyName("commentId")
  private String commentId;
  @PropertyName("likes")
  private int likes;
  @Exclude
  private String userImage;
  @Exclude
  private String userName;
  @Exclude
  private boolean isLikedByUser;
  @Exclude
  private boolean hasBeenCheckedForUserLike;

  public Comment() {
  }

  public Comment(Map<String, Object> map) {
    this.comment = (String) map.get("comment");
    this.userId = (String) map.get("userId");
    this.time = (long) map.get("time");
    this.replies = (int) map.get("replies");
    this.commentId = (String) map.get("commentId");
    this.likes = (int) map.get("likes");
  }

  public Comment(String comment, String userId, long time, int replies,
                 String commentId, int likes) {
    this.comment = comment;
    this.userId = userId;
    this.time = time;
    this.replies = replies;
    this.commentId = commentId;
    this.likes = likes;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public int getReplies() {
    return replies;
  }

  public void setReplies(int replies) {
    this.replies = replies;
  }

  public String getCommentId() {
    return commentId;
  }

  public void setCommentId(String commentId) {
    this.commentId = commentId;
  }

  public String getUserImage() {
    return userImage;
  }

  public void setUserImage(String userImage) {
    this.userImage = userImage;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getLikes() {
    return likes;
  }

  public void setLikes(int likes) {
    this.likes = likes;
  }


  public boolean isLikedByUser() {
    return isLikedByUser;
  }

  public void setLikedByUser(boolean likedByUser) {
    isLikedByUser = likedByUser;
  }

  public boolean isHasBeenCheckedForUserLike() {
    return hasBeenCheckedForUserLike;
  }

  public void setHasBeenCheckedForUserLike(boolean hasBeenCheckedForUserLike) {
    this.hasBeenCheckedForUserLike = hasBeenCheckedForUserLike;
  }
}