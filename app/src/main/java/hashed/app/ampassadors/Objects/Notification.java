package hashed.app.ampassadors.Objects;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Notification implements Serializable {

  @PropertyName("senderId")
  public String senderId;
  @PropertyName("receiverId")
  public String receiverId;
  @PropertyName("type")
  public String type;
  @PropertyName("timeCreated")
  public long timeCreated;
  @PropertyName("content")
  public String content;

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getTimeCreated() {
    return timeCreated;
  }

  public void setTimeCreated(long timeCreated) {
    this.timeCreated = timeCreated;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
