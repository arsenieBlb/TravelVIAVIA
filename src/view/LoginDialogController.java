package view;

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

  public void init(FlightSceneViewController owner, StackPane wrapper)
  {
    this.owner = owner;
    this.wrapper = wrapper;

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
    wrapper.setVisible(true);
    wrapper.setManaged(true);
    loginEmailField.requestFocus();
  }

  public void hide()
  {
    wrapper.setVisible(false);
    wrapper.setManaged(false);
  }

  private void login()
  {
    if (!owner.loginCustomer(loginEmailField.getText(),
        loginPasswordField.getText()))
    {
      showError("Please use a valid customer account.");
      return;
    }
    hide();
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
