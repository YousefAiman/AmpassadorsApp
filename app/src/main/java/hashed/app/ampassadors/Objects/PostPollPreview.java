package hashed.app.ampassadors.Objects;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.R;

@IgnoreExtraProperties
public class PostPollPreview implements Serializable {

  @PropertyName("postId")
  private String postId;
  @PropertyName("title")
  private String title;
  @PropertyName("publisherId")
  private String publisherId;
  @PropertyName("type")
  private int type;
  @PropertyName("publishTime")
  private long publishTime;
  @PropertyName("pollDuration")
  private long pollDuration;
  @PropertyName("totalVotes")
  private long totalVotes;
  @PropertyName("pollEnded")
  private boolean pollEnded;
  @Exclude
  private int chosenPollOption;
  @Exclude
  private ArrayList<PollOption> pollOptions;

  public PostPollPreview() {
  }

  public PostPollPreview(Map<String, Object> postMap) {

    this.postId = (String) postMap.get("postId");
    this.title = (String) postMap.get("title");
    this.publisherId = (String) postMap.get("publisherId");
    this.type = (int) postMap.get("type");

  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getPublisherId() {
    return publisherId;
  }

  public void setPublisherId(String publisherId) {
    this.publisherId = publisherId;
  }

   public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public int getChosenPollOption() {
    return chosenPollOption;
  }

  public void setChosenPollOption(int chosenPollOption) {
    this.chosenPollOption = chosenPollOption;
  }

  public long getTotalVotes() {
    return totalVotes;
  }

  public void setTotalVotes(long totalVotes) {
    this.totalVotes = totalVotes;
  }

  public ArrayList<PollOption> getPollOptions() {
    return pollOptions;
  }

  public void setPollOptions(ArrayList<PollOption> pollOptions) {
    this.pollOptions = pollOptions;
  }

  public boolean isPollEnded() {
    return pollEnded;
  }

  public void setPollEnded(boolean pollEnded) {
    this.pollEnded = pollEnded;
  }

  public long getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(long publishTime) {
    this.publishTime = publishTime;
  }

  public long getPollDuration() {
    return pollDuration;
  }

  public void setPollDuration(long pollDuration) {
    this.pollDuration = pollDuration;
  }
}