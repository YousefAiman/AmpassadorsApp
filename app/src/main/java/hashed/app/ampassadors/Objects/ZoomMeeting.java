package hashed.app.ampassadors.Objects;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class ZoomMeeting {

  @SerializedName("id")
  @PropertyName("id")
  private String id;

  @SerializedName("host_id")
  @PropertyName("hostId")
  private String hostId;

  @SerializedName("host_email")
  @PropertyName("hostEmail")
  private String hostEmail;

  @SerializedName("topic")
  @PropertyName("topic")
  private String topic;

  @SerializedName("type")
  @PropertyName("type")
  private int type;
//  @PropertyName("duration")
//  private int duration;
  @SerializedName("status")
  @PropertyName("status")
  private String status;

  @SerializedName("start_url")
  @PropertyName("startUrl")
  private String startUrl;

  @SerializedName("join_url")
  @PropertyName("joinUrl")
  private String joinUrl;

  @SerializedName("")
  @PropertyName("startTime")
  private long startTime;

  @PropertyName("estimatedStartTime")
  private long estimatedStartTime;

  @PropertyName("estimatedEndTime")
  private long estimatedEndTime;

  public ZoomMeeting() {
  }

  public ZoomMeeting(String id, String hostId, String hostEmail, String topic, int type,
//                     int duration,
                     long estimatedStartTime,long estimatedEndTime,
                     String status, String startUrl, String joinUrl) {
    this.setId(id);
    this.setHostId(hostId);
    this.setHostEmail(hostEmail);
    this.setTopic(topic);
//    this.setDuration(duration);
    this.setStatus(status);
    this.setStartUrl(startUrl);
    this.setJoinUrl(joinUrl);
    this.setType(type);
    this.setEstimatedStartTime(estimatedStartTime);
    this.setEstimatedEndTime(estimatedEndTime);
  }

  public ZoomMeeting(String id, String hostId, String hostEmail, String topic, int type, long startTime,
//                     int duration,
                     long estimatedStartTime,long estimatedEndTime,
                     String status, String startUrl, String joinUrl) {
    this.setId(id);
    this.setHostId(hostId);
    this.setHostEmail(hostEmail);
    this.setTopic(topic);
//    this.setDuration(duration);
    this.setStatus(status);
    this.setStartUrl(startUrl);
    this.setJoinUrl(joinUrl);
    this.setType(type);
    this.setStartTime(startTime);
    this.setEstimatedStartTime(estimatedStartTime);
    this.setEstimatedEndTime(estimatedEndTime);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHostId() {
    return hostId;
  }

  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  public String getHostEmail() {
    return hostEmail;
  }

  public void setHostEmail(String hostEmail) {
    this.hostEmail = hostEmail;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStartUrl() {
    return startUrl;
  }

  public void setStartUrl(String startUrl) {
    this.startUrl = startUrl;
  }

  public String getJoinUrl() {
    return joinUrl;
  }

  public void setJoinUrl(String joinUrl) {
    this.joinUrl = joinUrl;
  }


  @NonNull
  @Override
  public String toString() {
    return "ZoomMeeting{" +
            "id='" + id + '\'' +
            ", hostId='" + hostId + '\'' +
            ", hostEmail='" + hostEmail + '\'' +
            ", topic='" + topic + '\'' +
            ", status='" + status + '\'' +
            ", startUrl='" + startUrl + '\'' +
            ", joinUrl='" + joinUrl + '\'' +
            '}';
  }

//  public int getDuration() {
//    return duration;
//  }
//
//  public void setDuration(int duration) {
//    this.duration = duration;
//  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEstimatedStartTime() {
    return estimatedStartTime;
  }

  public void setEstimatedStartTime(long estimatedStartTime) {
    this.estimatedStartTime = estimatedStartTime;
  }

  public long getEstimatedEndTime() {
    return estimatedEndTime;
  }

  public void setEstimatedEndTime(long estimatedEndTime) {
    this.estimatedEndTime = estimatedEndTime;
  }
}
