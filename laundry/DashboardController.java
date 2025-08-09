package laundry;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class DashboardController {

    @FXML
    private AnchorPane mainPane;  // ড্যাশবোর্ডের প্রধান পেন যেখানে অন্য পেইজগুলো লোড হবে

    @FXML
    private Button btnOrders, btnItems, btnCustomers, btnLogout;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        try {
            if (event.getSource() == btnLogout) {
                // লগআউট হলে পুরো উইন্ডো পরিবর্তন করুন
                Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Laundry Shop Admin Portal - Login");
                stage.show();
            } else {
                String fxmlFile = "";
                if (event.getSource() == btnOrders) {
                    fxmlFile = "Order.fxml";
                } else if (event.getSource() == btnItems) {
                    fxmlFile = "Item.fxml";
                } else if (event.getSource() == btnCustomers) {
                    fxmlFile = "Customer.fxml";
                }

                if (!fxmlFile.isEmpty()) {
                    // mainPane এর children হিসেবে লোড করুন
                    Parent pane = FXMLLoader.load(getClass().getResource(fxmlFile));
                    mainPane.getChildren().clear();
                    mainPane.getChildren().add(pane);

                    // নতুন পেইজকে mainPane এর সাইজে ফিট করুন
                    AnchorPane.setTopAnchor(pane, 0.0);
                    AnchorPane.setBottomAnchor(pane, 0.0);
                    AnchorPane.setLeftAnchor(pane, 0.0);
                    AnchorPane.setRightAnchor(pane, 0.0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
