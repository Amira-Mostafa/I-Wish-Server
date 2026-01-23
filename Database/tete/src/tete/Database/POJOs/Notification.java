
package tete.Database.POJOs;

public class Notification {
    private int notificationId;
    private int receiverId;
    private Integer wishId; // can be null
    private String type; // WISH_COMPLETED, WISH_BOUGHT
    private String message;
    private char isRead; // 'Y' or 'N'

    public Notification() {}

    public Notification(int notificationId, int receiverId, Integer wishId, String type, String message, char isRead) {
        this.notificationId = notificationId;
        this.receiverId = receiverId;
        this.wishId = wishId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public Integer getWishId() { return wishId; }
    public void setWishId(Integer wishId) { this.wishId = wishId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public char getIsRead() { return isRead; }
    public void setIsRead(char isRead) { this.isRead = isRead; }
}
