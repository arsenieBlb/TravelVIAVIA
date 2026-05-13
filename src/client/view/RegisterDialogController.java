package client.view;

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
    String firstName = registerNameField.getText().trim();
    String lastName = registerLastNameField.getText().trim();
    String email = registerEmailField.getText().trim();
    String password = registerPasswordField.getText();

    // name cannot contain numbers
    if (firstName.isEmpty() || containsDigit(firstName)) {
      showError("First name is required and cannot contain numbers.");
      return;
    }
    if (lastName.isEmpty() || containsDigit(lastName)) {
      showError("Last name is required and cannot contain numbers.");
      return;
    }

    // email must end with @gmail.com
    if (!email.contains("@gmail.com")) {
      showError("Email must be a valid @gmail.com address.");
      return;
    }

    // password at least 8 characters
    if (password.length() < 8) {
      showError("Password must be at least 8 characters long.");
      return;
    }

    if (!owner.registerCustomer(firstName, lastName, email, password))
    {
      showError("Could not create the account. Check the fields and try again.");
      return;
    }

    registerPasswordField.clear();
    hide();
    owner.showLoginDialog();
  }

  private boolean containsDigit(String text) {
    for (char c : text.toCharArray()) {
      if (Character.isDigit(c)) return true;
    }
    return false;
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


