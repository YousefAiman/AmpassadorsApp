package hashed.app.ampassadors.Objects;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.Map;

@IgnoreExtraProperties
public class PostNewsPreview implements Serializable {

  @PropertyName("postId")
  private String postId;
  @PropertyName("type")
  private int type;
  @PropertyName("title")
  private String title;
  @PropertyName("attachmentUrl")
  private String attachmentUrl;
  @PropertyName("description")
  private String description;
  @PropertyName("videoThumbnailUrl")
  private String videoThumbnailUrl;
  @PropertyName("attachmentType")
  private int attachmentType;

  public PostNewsPreview() {
  }

  public PostNewsPreview(String postId, int type, String title, String attachmentUrl, String description, String videoThumbnailUrl, int attachmentType) {
    this.postId = postId;
    this.type = type;
    this.title = title;
    this.attachmentUrl = attachmentUrl;
    this.description = description;
    this.videoThumbnailUrl = videoThumbnailUrl;
    this.attachmentType = attachmentType;
  }

  public PostNewsPreview(Map<String, Object> postMap) {

    this.postId = (String) postMap.get("postId");
    this.title = (String) postMap.get("title");
    this.description = (String) postMap.get("description");
    this.type = (int) postMap.get("type");

    if(postMap.containsKey("attachmentType")){
      this.attachmentType = (int) postMap.get("attachmentType");
      this.attachmentUrl = (String) postMap.get("attachmentUrl");
      if (videoThumbnailUrl != null) {
        this.videoThumbnailUrl = (String) postMap.get("videoThumbnailUrl");
      }
    }

  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public int getAttachmentType() {
    return attachmentType;
  }

  public void setAttachmentType(int attachmentType) {
    this.attachmentType = attachmentType;
  }

  public String getVideoThumbnailUrl() {
    return videoThumbnailUrl;
  }

  public void setVideoThumbnailUrl(String videoThumbnailUrl) {
    this.videoThumbnailUrl = videoThumbnailUrl;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public void setAttachmentUrl(String attachmentUrl) {
    this.attachmentUrl = attachmentUrl;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}