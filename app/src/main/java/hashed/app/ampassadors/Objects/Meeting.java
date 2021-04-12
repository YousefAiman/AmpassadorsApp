package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;

@IgnoreExtraProperties
public class Meeting implements Serializable {

  @PropertyName("creatorId")
  private String creatorId;
  @PropertyName("title")
  private String title;
  @PropertyName("description")
  private String description;
  @PropertyName("startTime")
  private long startTime;
  @PropertyName("createdTime")
  private long createdTime;
//  @Exclude
//  private List<String> members;
  @PropertyName("meetingId")
  private String meetingId;
  @PropertyName("hasEnded")
  private boolean hasEnded;
  @PropertyName("hasStarted")
  private boolean hasStarted;
  @PropertyName("imageUrl")
  private String imageUrl;
  @PropertyName("important")
  boolean important ;
  public Meeting() {
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

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

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

//  public List<String> getMembers() {
//    return members;
//  }
//
//  public void setMembers(List<String> members) {
//    this.members = members;
//  }

  public String getMeetingId() {
    return meetingId;
  }

  public void setMeetingId(String meetingId) {
    this.meetingId = meetingId;
  }

  public boolean isHasEnded() {
    return hasEnded;
  }

  public void setHasEnded(boolean hasEnded) {
    this.hasEnded = hasEnded;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }
}
