package laundry;

public class Order {
    private int id;
    private int customerId;
    private int itemId;
    private int quantity;
    private String deliveryDate;
    private String status;

    public Order(int id, int customerId, int itemId, int quantity, String deliveryDate, String status) {
        this.id = id;
        this.customerId = customerId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.deliveryDate = deliveryDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public int getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
    public void setStatus(String status) { this.status = status; }
}
