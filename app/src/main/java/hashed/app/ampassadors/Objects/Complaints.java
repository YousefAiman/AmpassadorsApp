package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class Complaints {
    @PropertyName("title")
    String title;
    @PropertyName("description")
    String description;
    @PropertyName("defendant")
    String defendant;
    @PropertyName("time")
    long time;
    @PropertyName("userid")
    String userid;
    @PropertyName("complaintsId")
    String complaintsId;
    @PropertyName("reviewed")
    boolean reviewed;
    @Exclude
    String username;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefendant() {
        return defendant;
    }

    public void setDefendant(String defendant) {
        this.defendant = defendant;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getComplaintsId() {
        return complaintsId;
    }

    public void setComplaintsId(String complaintsId) {
        this.complaintsId = complaintsId;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
