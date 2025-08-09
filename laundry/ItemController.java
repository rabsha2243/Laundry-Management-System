package laundry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;

import java.sql.*;

public class ItemController {

    @FXML private TextField itemNameField;
    @FXML private TextField priceField;
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Integer> idCol;
    @FXML private TableColumn<Item, String> nameCol;
    @FXML private TableColumn<Item, Double> priceCol;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadItems();

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadItemDetails(newSel);
            }
        });
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    private void loadItems() {
        itemList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM items");
            while (rs.next()) {
                itemList.add(new Item(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getDouble("price")
                ));
            }
            itemTable.setItems(itemList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadItemDetails(Item item) {
        itemNameField.setText(item.getItemName());
        priceField.setText(String.valueOf(item.getPrice()));
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        String name = itemNameField.getText();
        String priceStr = priceField.getText();

        if (name.isEmpty() || priceStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO items (item_name, price) VALUES (?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Item added successfully");
                clearFields();
                loadItems();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid price value");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error adding item");
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select an item to update");
            return;
        }

        String name = itemNameField.getText();
        String priceStr = priceField.getText();

        if (name.isEmpty() || priceStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all fields");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE items SET item_name=?, price=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setInt(3, selected.getId());
                pst.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Item updated successfully");
                clearFields();
                loadItems();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid price value");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error updating item");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select an item to delete");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM items WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selected.getId());
            pst.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Item deleted successfully");
            clearFields();
            loadItems();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error deleting item");
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        itemNameField.clear();
        priceField.clear();
        itemTable.getSelectionModel().clearSelection();
    }

    
    
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Item Management");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
