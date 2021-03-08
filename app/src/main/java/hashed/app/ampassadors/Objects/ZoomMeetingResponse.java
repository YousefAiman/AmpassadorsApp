package hashed.app.ampassadors.Objects;

import com.google.gson.annotations.SerializedName;

public class ZoomMeetingResponse {

    @SerializedName("id")
    private
    String id;
    @SerializedName("host_id")
    private
    String hostId;
    @SerializedName("host_email")
    private
    String hostEmail;
    @SerializedName("topic")
    private
    String topic;
    @SerializedName("status")
    private
    String status;
    @SerializedName("start_url")
    private
    String startUrl;
    @SerializedName("join_url")
    private
    String joinUrl;

  public ZoomMeetingResponse(String id, String hostId, String hostEmail, String topic, String status, String startUrl, String joinUrl) {
    this.setId(id);
    this.setHostId(hostId);
    this.setHostEmail(hostEmail);
    this.setTopic(topic);
    this.setStatus(status);
    this.setStartUrl(startUrl);
    this.setJoinUrl(joinUrl);
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
}
