package de.schuette.procman;

public interface TextView extends Appendable {

  public boolean isAutoScrollToBottom();

  public void setAutoScrollToBottom(boolean autoScrollToBottom);

  /**
   * This method scrolls the {@link TextView} to the bottom.
   */
  public void scrollToBottom();
}
