package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class MeetingPreview implements Serializable {

  @PropertyName("title")
  private String title;
  @PropertyName("startTime;")
  private long startTime;
  @PropertyName("meetingId")
  private String meetingId;
  @PropertyName("hasEnded")
  private boolean hasEnded;

  public MeetingPreview() {
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
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

}
