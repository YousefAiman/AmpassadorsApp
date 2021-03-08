package hashed.app.ampassadors.NotificationUtil;

public class NotificationData {
  
  private String senderUid;
  private String senderUsername;
  private String body;
  private String title;
  private String senderImageUrl;
  private String type;

  public NotificationData(String senderUid, String body, String title, String senderImageUrl,
                          String senderUsername, String type) {
    this.senderUid = senderUid;
    this.body = body;
    this.title = title;
    this.senderImageUrl = senderImageUrl;
    this.senderUsername = senderUsername;
    this.type = type;
  }


  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSenderUid() {
    return senderUid;
  }

  public void setSenderUid(String senderUid) {
    this.senderUid = senderUid;
  }

  public String getSenderUsername() {
    return senderUsername;
  }

  public void setSenderUsername(String senderUsername) {
    this.senderUsername = senderUsername;
  }

  public String getSenderImageUrl() {
    return senderImageUrl;
  }

  public void setSenderImageUrl(String senderImageUrl) {
    this.senderImageUrl = senderImageUrl;
  }
}
