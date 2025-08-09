package laundry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;

import java.sql.*;
import java.time.LocalDate;

public class OrderController {

    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> itemComboBox;
    @FXML private TextField quantityField;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<OrderView> orderTable;
    @FXML private TableColumn<OrderView, Integer> idCol;
    @FXML private TableColumn<OrderView, String> customerCol;
    @FXML private TableColumn<OrderView, String> itemCol;
    @FXML private TableColumn<OrderView, Integer> quantityCol;
    @FXML private TableColumn<OrderView, String> deliveryDateCol;
    @FXML private TableColumn<OrderView, String> statusCol;

    private ObservableList<OrderView> orderList = FXCollections.observableArrayList();
    private ObservableList<String> customers = FXCollections.observableArrayList();
    private ObservableList<String> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("Pending", "In Progress", "Completed");
        loadCustomers();
        loadItems();

        setupTableColumns();
        loadOrders();

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadOrderDetails(newSel);
            }
        });
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        deliveryDateCol.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadCustomers() {
        customers.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM customers");
            while (rs.next()) {
                customers.add(rs.getString("name"));
            }
            customerComboBox.setItems(customers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadItems() {
        items.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT item_name FROM items");
            while (rs.next()) {
                items.add(rs.getString("item_name"));
            }
            itemComboBox.setItems(items);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOrders() {
        orderList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT o.id, c.name AS customer_name, i.item_name, o.quantity, o.delivery_date, o.status " +
                    "FROM orders o " +
                    "JOIN customers c ON o.customer_id = c.id " +
                    "JOIN items i ON o.item_id = i.id";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                orderList.add(new OrderView(
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDate("delivery_date").toString(),
                        rs.getString("status")
                ));
            }
            orderTable.setItems(orderList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOrderDetails(OrderView order) {
        customerComboBox.setValue(order.getCustomerName());
        itemComboBox.setValue(order.getItemName());
        quantityField.setText(String.valueOf(order.getQuantity()));
        deliveryDatePicker.setValue(LocalDate.parse(order.getDeliveryDate()));
        statusComboBox.setValue(order.getStatus());
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try (Connection conn = DBConnection.getConnection()) {
            String customerName = customerComboBox.getValue();
            String itemName = itemComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            LocalDate deliveryDate = deliveryDatePicker.getValue();
            String status = statusComboBox.getValue();

            if (customerName == null || itemName == null || status == null || deliveryDate == null) {
                showAlert(Alert.AlertType.ERROR, "Fill all fields");
                return;
            }

            int customerId = getCustomerIdByName(customerName);
            int itemId = getItemIdByName(itemName);

            String sql = "INSERT INTO orders (customer_id, item_id, quantity, delivery_date, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            pst.setInt(2, itemId);
            pst.setInt(3, quantity);
            pst.setDate(4, Date.valueOf(deliveryDate));
            pst.setString(5, status);

            pst.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Order added successfully");
            clearFields();
            loadOrders();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error adding order");
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        OrderView selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select an order to update");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String customerName = customerComboBox.getValue();
            String itemName = itemComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText());
            LocalDate deliveryDate = deliveryDatePicker.getValue();
            String status = statusComboBox.getValue();

            int customerId = getCustomerIdByName(customerName);
            int itemId = getItemIdByName(itemName);

            String sql = "UPDATE orders SET customer_id=?, item_id=?, quantity=?, delivery_date=?, status=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, customerId);
            pst.setInt(2, itemId);
            pst.setInt(3, quantity);
            pst.setDate(4, Date.valueOf(deliveryDate));
            pst.setString(5, status);
            pst.setInt(6, selected.getId());

            pst.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Order updated successfully");
            clearFields();
            loadOrders();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error updating order");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        OrderView selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select an order to delete");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM orders WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selected.getId());

            pst.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Order deleted successfully");
            clearFields();
            loadOrders();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error deleting order");
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        customerComboBox.setValue(null);
        itemComboBox.setValue(null);
        quantityField.clear();
        deliveryDatePicker.setValue(null);
        statusComboBox.setValue(null);
        orderTable.getSelectionModel().clearSelection();
    }

    private int getCustomerIdByName(String name) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id FROM customers WHERE name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private int getItemIdByName(String name) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id FROM items WHERE item_name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Order Management");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Inner class to show order data with names instead of IDs
    public static class OrderView {
        private int id;
        private String customerName;
        private String itemName;
        private int quantity;
        private String deliveryDate;
        private String status;

        public OrderView(int id, String customerName, String itemName, int quantity, String deliveryDate, String status) {
            this.id = id;
            this.customerName = customerName;
            this.itemName = itemName;
            this.quantity = quantity;
            this.deliveryDate = deliveryDate;
            this.status = status;
        }

        public int getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public String getDeliveryDate() { return deliveryDate; }
        public String getStatus() { return status; }
    }
}
