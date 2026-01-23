package tete.Database.POJOs;

public class Wish {
    private int wishId;
    private int userId;
    private String name;
    private String description;
    private double price;
    private String imagePath;
    private char isCompleted; // 'Y' or 'N'

    public Wish() {}

    public Wish(int wishId, int userId, String name, String description, double price, String imagePath, char isCompleted) {
        this.wishId = wishId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imagePath = imagePath;
        this.isCompleted = isCompleted;
    }

    public int getWishId() { return wishId; }
    public void setWishId(int wishId) { this.wishId = wishId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public char getIsCompleted() { return isCompleted; }
    public void setIsCompleted(char isCompleted) { this.isCompleted = isCompleted; }
}
