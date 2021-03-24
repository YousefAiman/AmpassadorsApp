package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class PollOption implements Serializable {

  @PropertyName("id")
  private int id;
  @PropertyName("option")
  public String option;
  @PropertyName("votes")
  public long votes;
  @Exclude
  public boolean isChosen;

  public PollOption() {
  }

  public String getOption() {
    return option;
  }

  public void setOption(String option) {
    this.option = option;
  }

  public long getVotes() {
    return votes;
  }

  public void setVotes(long votes) {
    this.votes = votes;
  }

  public boolean isChosen() {
    return isChosen;
  }

  public void setChosen(boolean chosen) {
    isChosen = chosen;
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
