package view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class AdminLoginDialogController
{
  @FXML private Button closeAdminButton;
  @FXML private TextField adminEmailField;
  @FXML private PasswordField adminPasswordField;
  @FXML private Button adminSubmitButton;
  @FXML private Button backToLoginFromAdminButton;
  @FXML private Label adminErrorLabel;

  private FlightSceneViewController owner;
  private StackPane wrapper;

  public void init(FlightSceneViewController owner, StackPane wrapper)
  {
    this.owner = owner;
    this.wrapper = wrapper;

    closeAdminButton.setOnAction(event -> hide());
    adminSubmitButton.setOnAction(event -> login());
    backToLoginFromAdminButton.setOnAction(event -> owner.showLoginDialog());
    hideError();
  }

  public void show()
  {
    adminPasswordField.clear();
    hideError();
    wrapper.setVisible(true);
    wrapper.setManaged(true);
    adminEmailField.requestFocus();
  }

  public void hide()
  {
    wrapper.setVisible(false);
    wrapper.setManaged(false);
  }

  private void login()
  {
    if (!owner.loginAdmin(adminEmailField.getText(),
        adminPasswordField.getText()))
    {
      showError("Please use a valid admin account.");
      return;
    }
    hide();
  }

  private void showError(String message)
  {
    adminErrorLabel.setText(message);
    adminErrorLabel.setVisible(true);
    adminErrorLabel.setManaged(true);
  }

  private void hideError()
  {
    adminErrorLabel.setVisible(false);
    adminErrorLabel.setManaged(false);
  }
}
