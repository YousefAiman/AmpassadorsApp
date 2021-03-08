package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

@IgnoreExtraProperties
public class CommentReply implements Serializable {

    @PropertyName("reply")
    private String reply;
    @PropertyName("userId")
    private String userId;
    @PropertyName("time")
    private long time;
    @PropertyName("replyId")
    private String replyId;
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

    public CommentReply(){
    }

    public CommentReply(Map<String,Object> map) {
        this.reply = (String) map.get("reply");
        this.userId = (String) map.get("userId");
        this.time = (long) map.get("time");
        this.replyId = (String) map.get("replyId");
        this.likes = (int) map.get("likes");
    }

    public CommentReply(String reply, String userId, long time, String replyId,
                        String userImage, String userName,int likes) {
        this.reply = reply;
        this.userId = userId;
        this.time = time;
        this.replyId = replyId;
        this.userImage = userImage;
        this.userName = userName;
        this.likes = likes;
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

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
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