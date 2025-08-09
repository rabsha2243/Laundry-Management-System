package laundry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;

import java.sql.*;

public class CustomerController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idCol;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> phoneCol;
    @FXML private TableColumn<Customer, String> addressCol;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCustomers();

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadCustomerDetails(newSel);
            }
        });
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    private void loadCustomers() {
        customerList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM customers");
            while (rs.next()) {
                customerList.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
            customerTable.setItems(customerList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerDetails(Customer c) {
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        addressField.setText(c.getAddress());
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Fill all fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO customers (name, phone, address) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, phone);
            pst.setString(3, address);
            pst.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Customer added successfully");
            clearFields();
            loadCustomers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error adding customer");
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select a customer to update");
            return;
        }

        String name = nameField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Fill all fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE customers SET name=?, phone=?, address=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, phone);
            pst.setString(3, address);
            pst.setInt(4, selected.getId());
            pst.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Customer updated successfully");
            clearFields();
            loadCustomers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error updating customer");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select a customer to delete");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM customers WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selected.getId());
            pst.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Customer deleted successfully");
            clearFields();
            loadCustomers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error deleting customer");
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        nameField.clear();
        phoneField.clear();
        addressField.clear();
        customerTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Customer Management");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
