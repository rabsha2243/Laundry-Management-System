package laundry;

public class Item {
    private int id;
    private String itemName;
    private double price;

    public Item(int id, String itemName, double price) {
        this.id = id;
        this.itemName = itemName;
        this.price = price;
    }

    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }

    public void setId(int id) { this.id = id; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setPrice(double price) { this.price = price; }
}
