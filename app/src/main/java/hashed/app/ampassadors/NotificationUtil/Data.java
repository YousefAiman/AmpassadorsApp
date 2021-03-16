package hashed.app.ampassadors.NotificationUtil;

public class Data {

  private String senderUid;
  private String body;
  private String title;
  private String senderImageUrl;
  private String type;
  private String sourceType;
  private String sourceId;

  public Data(String senderUid, String body, String title, String senderImageUrl,
              String type, String sourceType,String sourceId) {
    this.setSenderUid(senderUid);
    this.setBody(body);
    this.setTitle(title);
    this.setSenderImageUrl(senderImageUrl);
    this.setType(type);
    this.setSourceType(sourceType);
    this.setSourceId(sourceId);
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


  public String getSenderImageUrl() {
    return senderImageUrl;
  }

  public void setSenderImageUrl(String senderImageUrl) {
    this.senderImageUrl = senderImageUrl;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }
}
