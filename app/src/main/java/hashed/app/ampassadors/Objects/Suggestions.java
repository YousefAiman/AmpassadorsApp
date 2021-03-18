package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class Suggestions implements Serializable {


  @PropertyName("title")
  String title;
  @PropertyName("description")
  String description;
  @PropertyName("time")
  long time;
  @PropertyName("userid")
  String userid;
  @Exclude
  String username;
  @PropertyName("SuggestionId")
  String SuggestionId;
  @PropertyName("reviewed")
  boolean reviewed;

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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getSuggestionId() {
    return SuggestionId;
  }

  public void setSuggestionId(String suggestionId) {
    SuggestionId = suggestionId;
  }

  public boolean isReviewed() {
    return reviewed;
  }

  public void setReviewed(boolean reviewed) {
    this.reviewed = reviewed;
  }
}
