package hashed.app.ampassadors.Objects;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class ChatItem implements Serializable {

  private String messagingDocId;
  private boolean seen;
  private String messagingUid;
  private String imageUrl;
  private String username;
  private PrivateMessagePreview message;
  private long time;
  private long messageKey;

  public ChatItem() {
  }

  public ChatItem(String messagingUid, String imageUrl, String username,
                  PrivateMessagePreview message, long time) {
    this.messagingUid = messagingUid;
    this.imageUrl = imageUrl;
    this.username = username;
    this.message = message;
    this.time = time;
  }


  public PrivateMessagePreview getMessage() {
    return message;
  }

  public void setMessage(PrivateMessagePreview message) {
    this.message = message;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
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

  public String getMessagingUid() {
    return messagingUid;
  }

  public void setMessagingUid(String messagingUid) {
    this.messagingUid = messagingUid;
  }

  public String getMessagingDocId() {
    return messagingDocId;
  }

  public void setMessagingDocId(String messagingDocId) {
    this.messagingDocId = messagingDocId;
  }


  public boolean isSeen() {
    return seen;
  }

  public void setSeen(boolean seen) {
    this.seen = seen;
  }

  public long getMessageKey() {
    return messageKey;
  }

  public void setMessageKey(long messageKey) {
    this.messageKey = messageKey;
  }
}
