package hashed.app.ampassadors.NotificationUtil;

class Sender {
  private final NotificationData notificationData;
  private final String to;

  Sender(NotificationData notificationData, String to) {
    this.notificationData = notificationData;
    this.to = to;
  }
}
