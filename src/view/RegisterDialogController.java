package view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class RegisterDialogController
{
  @FXML private Button closeRegisterButton;
  @FXML private TextField registerNameField;
  @FXML private TextField registerLastNameField;
  @FXML private TextField registerEmailField;
  @FXML private PasswordField registerPasswordField;
  @FXML private Button registerSubmitButton;
  @FXML private Button backToLoginFromRegisterButton;
  @FXML private Label registerErrorLabel;

  private FlightSceneViewController owner;
  private StackPane wrapper;

  public void init(FlightSceneViewController owner, StackPane wrapper)
  {
    this.owner = owner;
    this.wrapper = wrapper;

    closeRegisterButton.setOnAction(event -> hide());
    registerSubmitButton.setOnAction(event -> register());
    backToLoginFromRegisterButton.setOnAction(event -> owner.showLoginDialog());
    hideError();
  }

  public void show()
  {
    hideError();
    wrapper.setVisible(true);
    wrapper.setManaged(true);
    registerNameField.requestFocus();
  }

  public void hide()
  {
    wrapper.setVisible(false);
    wrapper.setManaged(false);
  }

  private void register()
  {
    if (!owner.registerCustomer(registerNameField.getText(),
        registerLastNameField.getText(), registerEmailField.getText(),
        registerPasswordField.getText()))
    {
      showError("Could not create the account. Check the fields and try again.");
      return;
    }

    registerPasswordField.clear();
    hide();
    owner.showLoginDialog();
  }

  private void showError(String message)
  {
    registerErrorLabel.setText(message);
    registerErrorLabel.setVisible(true);
    registerErrorLabel.setManaged(true);
  }

  private void hideError()
  {
    registerErrorLabel.setVisible(false);
    registerErrorLabel.setManaged(false);
  }
}
