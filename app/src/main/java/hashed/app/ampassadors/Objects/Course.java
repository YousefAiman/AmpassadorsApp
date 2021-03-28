package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Course implements Serializable {

  @PropertyName("courseId")
  private String courseId;
  @PropertyName("creatorId")
  private String creatorId;
  @PropertyName("title")
  private String title;
  @PropertyName("tutorName")
  private String tutorName;
  @PropertyName("tutorId")
  private String tutorId;
  @PropertyName("startTime")
  private long startTime;
  @PropertyName("createdTime")
  private long createdTime;
  @PropertyName("duration")
  private int duration;
  @PropertyName("hasEnded")
  private boolean hasEnded;

  public Course() {
  }

  public Course(Map<String, Object> postMap) {

    this.courseId = (String) postMap.get("courseId");
    this.creatorId = (String) postMap.get("creatorId");
    this.title = (String) postMap.get("title");
    this.tutorName = (String) postMap.get("tutorName");
    this.startTime = (long) postMap.get("startTime");
    this.createdTime = (long) postMap.get("createdTime");
    this.duration = (int) postMap.get("duration");
    this.hasEnded = (boolean) postMap.get("hasEnded");

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

  public boolean isHasEnded() {
    return hasEnded;
  }

  public void setHasEnded(boolean hasEnded) {
    this.hasEnded = hasEnded;
  }

  public String getTutorName() {
    return tutorName;
  }

  public void setTutorName(String tutorName) {
    this.tutorName = tutorName;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public String getTutorId() {
    return tutorId;
  }

  public void setTutorId(String tutorId) {
    this.tutorId = tutorId;
  }
}
