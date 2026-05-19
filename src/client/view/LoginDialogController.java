package client.view;

import client.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class LoginDialogController
{
  @FXML private Button closeLoginButton;
  @FXML private TextField loginEmailField;
  @FXML private PasswordField loginPasswordField;
  @FXML private Button loginSubmitButton;
  @FXML private Button switchToRegisterButton;
  @FXML private Button switchToAdminButton;
  @FXML private Label loginErrorLabel;

  private FlightSceneViewController owner;
  private StackPane wrapper;

  public void init(FlightSceneViewController owner)
  {
    this.owner = owner;

    closeLoginButton.setOnAction(event -> hide());
    loginSubmitButton.setOnAction(event -> login());
    switchToRegisterButton.setOnAction(event -> owner.showRegisterDialog());
    switchToAdminButton.setOnAction(event -> owner.showAdminLoginDialog());
    hideError();
  }

  public void show()
  {
    loginPasswordField.clear();
    hideError();
    loginEmailField.requestFocus();
  }

  public void hide()
  {
      owner.hideAuthDialogs();
  }

  private void login()
  {
    try
    {
      if (!owner.loginCustomer(loginEmailField.getText(),
          loginPasswordField.getText()))
      {
        showError("Please use a valid customer account.");
        return;
      }
      Customer customer = owner.getLoggedInCustomer();
      String firstName = customer == null ? "" : ", " + customer.getFirstName();
      showInfo("Welcome" + firstName);
      hide();
    }
    catch (RuntimeException e)
    {
      showError("Login failed. Please try again.");
    }
  }

  private void showInfo(String message)
  {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.INFORMATION);
    alert.setTitle("Login");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String message)
  {
    loginErrorLabel.setText(message);
    loginErrorLabel.setVisible(true);
    loginErrorLabel.setManaged(true);
  }

  private void hideError()
  {
    loginErrorLabel.setVisible(false);
    loginErrorLabel.setManaged(false);
  }
}


