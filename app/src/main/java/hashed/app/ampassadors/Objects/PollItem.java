package hashed.app.ampassadors.Objects;

import java.io.Serializable;

public class PollItem implements Serializable {

  private String choice;
  private boolean selected;

  public PollItem(String choice, boolean selected) {
    this.choice = choice;
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public String getChoice() {
    return choice;
  }

  public void setChoice(String choice) {
    this.choice = choice;
  }
}
