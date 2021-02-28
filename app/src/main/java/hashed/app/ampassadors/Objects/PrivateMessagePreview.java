package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class PrivateMessagePreview implements Serializable {

  @PropertyName("content")
  private String content;
  @PropertyName("deleted")
  private boolean deleted;
  @PropertyName("sender")
  private String sender;
  @PropertyName("time")
  private long time;
  @PropertyName("type")
  private int type;


  public PrivateMessagePreview() {
  }


  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }


}
