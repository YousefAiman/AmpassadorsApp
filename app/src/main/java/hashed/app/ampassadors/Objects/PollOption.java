package hashed.app.ampassadors.Objects;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class PollOption implements Serializable {

  @PropertyName("option")
  private String option;
  @PropertyName("votes")
  private long votes;
  @Exclude
  private boolean isChosen;

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

}
