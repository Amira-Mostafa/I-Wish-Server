package tete.Database.POJOs;

public class UserContribution {
    private int contributionId;
    private int userId;
    private int wishId;
    private double amount;

    public UserContribution() {}

    public UserContribution(int contributionId, int userId, int wishId, double amount) {
        this.contributionId = contributionId;
        this.userId = userId;
        this.wishId = wishId;
        this.amount = amount;
    }

    public int getContributionId() { return contributionId; }
    public void setContributionId(int contributionId) { this.contributionId = contributionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getWishId() { return wishId; }
    public void setWishId(int wishId) { this.wishId = wishId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
