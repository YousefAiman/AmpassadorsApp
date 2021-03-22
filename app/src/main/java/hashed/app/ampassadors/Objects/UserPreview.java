package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserPreview implements Serializable {

  @PropertyName("userId")
  public String userId;
  @PropertyName("imageUrl")
  public String imageUrl;
  @PropertyName("username")
  public String username;
  @PropertyName("status")
  private boolean status;

  public UserPreview() {
  }


  public UserPreview(String userId, String imageUrl, String username, boolean status) {
    this.userId = userId;
    this.imageUrl = imageUrl;
    this.username = username;
    this.setStatus(status);
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


  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }
}
