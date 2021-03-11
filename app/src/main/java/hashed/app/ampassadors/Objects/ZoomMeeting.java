package hashed.app.ampassadors.Objects;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class ZoomMeeting {

    @PropertyName("id")
    private String id;
    @PropertyName("hostId")
    private String hostId;
    @PropertyName("hostEmail")
    private String hostEmail;
    @PropertyName("topic")
    private String topic;
    @PropertyName("type")
    private int type;
    @PropertyName("duration")
    private int duration;
    @PropertyName("status")
    private String status;
    @PropertyName("startUrl")
    private String startUrl;
    @PropertyName("joinUrl")
    private String joinUrl;
    @PropertyName("startTime")
    private long startTime;

  public ZoomMeeting(){
  }

  public ZoomMeeting(String id, String hostId, String hostEmail, String topic,int type, int duration,
                     String status, String startUrl, String joinUrl) {
    this.setId(id);
    this.setHostId(hostId);
    this.setHostEmail(hostEmail);
    this.setTopic(topic);
    this.setDuration(duration);
    this.setStatus(status);
    this.setStartUrl(startUrl);
    this.setJoinUrl(joinUrl);
    this.setType(type);
  }

  public ZoomMeeting(String id, String hostId, String hostEmail, String topic,int type, long startTime,
                     int duration, String status, String startUrl, String joinUrl) {
    this.setId(id);
    this.setHostId(hostId);
    this.setHostEmail(hostEmail);
    this.setTopic(topic);
    this.setDuration(duration);
    this.setStatus(status);
    this.setStartUrl(startUrl);
    this.setJoinUrl(joinUrl);
    this.setType(type);
    this.setStartTime(startTime);
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

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

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
}
