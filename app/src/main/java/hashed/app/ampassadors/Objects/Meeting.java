package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

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
  @PropertyName("startTime;")
  private long startTime;
  @PropertyName("createdTime")
  private long createdTime;
  @PropertyName("members")
  private List<String> members;
  @PropertyName("meetingId")
  private String meetingId;
  @PropertyName("hasEnded")
  private boolean hasEnded;
  @PropertyName("hasStarted")
  private boolean hasStarted;
  @PropertyName("imageUrl")
  private String imageUrl;

  public Meeting() {
  }

  public Meeting(String creatorId, String title, String description, long startTime,
                 long createdTime, List<String> members, String meetingId, boolean hasEnded) {
    this.creatorId = creatorId;
    this.title = title;
    this.description = description;
    this.startTime = startTime;
    this.createdTime = createdTime;
    this.members = members;
    this.meetingId = meetingId;
    this.hasEnded = hasEnded;
  }


  public Meeting(String creatorId, String title, long startTime,
                 long createdTime, List<String> members, String meetingId, boolean hasEnded) {
    this.creatorId = creatorId;
    this.title = title;
    this.startTime = startTime;
    this.createdTime = createdTime;
    this.members = members;
    this.meetingId = meetingId;
    this.hasEnded = hasEnded;
  }

  public Meeting(String creatorId, String title, long startTime,
                 long createdTime, List<String> members, String meetingId, boolean hasEnded,
                 String imageUrl) {
    this.creatorId = creatorId;
    this.title = title;
    this.startTime = startTime;
    this.createdTime = createdTime;
    this.members = members;
    this.meetingId = meetingId;
    this.hasEnded = hasEnded;
    this.imageUrl = imageUrl;
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

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

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
}
