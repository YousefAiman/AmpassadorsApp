package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserPreview implements Serializable {

  @PropertyName("userId")
  private String userId;
  @PropertyName("imageUrl")
  private String imageUrl;
  @PropertyName("username")
  private String username;
  @PropertyName("online")
  private boolean online;

  public UserPreview() {
  }


  public UserPreview(String userId, String imageUrl, String username, boolean online) {
    this.userId = userId;
    this.imageUrl = imageUrl;
    this.username = username;
    this.online = online;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }
}
