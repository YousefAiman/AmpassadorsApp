package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@IgnoreExtraProperties
public class PrivateMessage implements Serializable {

  @Exclude
  private String id;
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
  @PropertyName("attachmentUrl")
  private String attachmentUrl;
  @PropertyName("videoThumbnail")
  private String videoThumbnail;
  @PropertyName("length")
  private long length;
  @PropertyName("fileName")
  private String fileName;
  @Exclude
  private UploadTask uploadTask;
  @Exclude
  private boolean stopPlayingAudio;

  @PropertyName("zoomMeeting")
  private ZoomMeeting zoomMeeting;

  public PrivateMessage() {
  }

  //text message
  public PrivateMessage(String content, long time, String sender, int type) {
    this.content = content;
    this.time = time;
    this.sender = sender;
    this.type = type;
  }

  //image message
  public PrivateMessage(String content, long time, String sender, int type, String attachmentUrl) {
    this.content = content;
    this.time = time;
    this.sender = sender;
    this.type = type;
    this.attachmentUrl = attachmentUrl;
  }

  //video message
  public PrivateMessage(String content, long time, String sender, int type, String attachmentUrl,
                        String videoThumbnail) {
    this.content = content;
    this.time = time;
    this.sender = sender;
    this.type = type;
    this.attachmentUrl = attachmentUrl;
    this.videoThumbnail = videoThumbnail;
  }

  //audio message
  public PrivateMessage(long length, long time, String sender, int type, String attachmentUrl) {
    this.length = length;
    this.time = time;
    this.sender = sender;
    this.type = type;
    this.attachmentUrl = attachmentUrl;
  }

  //document message
  public PrivateMessage(long time, String content, String sender, int type, String attachmentUrl,
                        String fileName) {
    this.time = time;
    this.content = content;
    this.sender = sender;
    this.type = type;
    this.attachmentUrl = attachmentUrl;
    this.fileName = fileName;
  }

  //
  public UploadTask getUploadTask() {
    return uploadTask;
  }

  public void setUploadTask(UploadTask uploadTask) {
    this.uploadTask = uploadTask;
  }

  public ZoomMeeting getZoomMeeting() {
    return zoomMeeting;
  }

  public void setZoomMeeting(ZoomMeeting zoomMeeting) {
    this.zoomMeeting = zoomMeeting;
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public String getVideoThumbnail() {
    return videoThumbnail;
  }

  public void setVideoThumbnail(String videoThumbnail) {
    this.videoThumbnail = videoThumbnail;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isStopPlayingAudio() {
    return stopPlayingAudio;
  }

  public void setStopPlayingAudio(boolean stopPlayingAudio) {
    this.stopPlayingAudio = stopPlayingAudio;
  }

  public static class UploadTask {

    private long downloadId;
    private boolean isDownloading;
    private boolean isCompleted;

    public UploadTask(long downloadId, boolean isDownloading) {
      this.downloadId = downloadId;
      this.isDownloading = isDownloading;
    }

    public long getDownloadId() {
      return downloadId;
    }

    public void setDownloadId(long downloadId) {
      this.downloadId = downloadId;
    }

    public boolean isDownloading() {
      return isDownloading;
    }

    public void setDownloading(boolean downloading) {
      isDownloading = downloading;
    }

    public boolean isCompleted() {
      return isCompleted;
    }

    public void setCompleted(boolean completed) {
      isCompleted = completed;
    }
  }


}
