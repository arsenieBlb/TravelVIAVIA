package client.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import client.viewmodel.MyBookingsViewModel;

public class AddBookingDialogController
{
  @FXML private StackPane addBookingModal;
  @FXML private Button closeAddBookingButton;
  @FXML private TextField addBookingCodeField;
  @FXML private TextField addBookingLastNameField;
  @FXML private Label addBookingErrorLabel;
  @FXML private Button cancelAddBookingButton;
  @FXML private Button saveAddBookingButton;

  private StackPane wrapper;
  private ViewHandler viewHandler;
  private MyBookingsViewModel viewModel;

  public void init(MyBookingsViewModel viewModel, ViewHandler viewHandler,
      StackPane wrapper)
  {
    this.viewModel = viewModel;
    this.viewHandler = viewHandler;
    this.wrapper = wrapper;

    closeAddBookingButton.setOnAction(event -> hide());
    cancelAddBookingButton.setOnAction(event -> hide());
    saveAddBookingButton.setOnAction(event -> saveBooking());
    addBookingLastNameField.setEditable(false);
    addBookingLastNameField.setFocusTraversable(false);
    hideError();
  }

  public void show()
  {
    addBookingCodeField.clear();
    addBookingLastNameField.setText(viewModel.getCustomerLastName());
    hideError();
    wrapper.setVisible(true);
    wrapper.setManaged(true);
    addBookingCodeField.requestFocus();
  }

  private void hide()
  {
    wrapper.setVisible(false);
    wrapper.setManaged(false);
  }

    private void saveBooking()
    {
        saveAddBookingButton.setDisable(true);
        try
        {
            viewModel.addBookingById(addBookingCodeField.getText());
            viewModel.refresh();

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Added");
            alert.setHeaderText(null);
            alert.setContentText("Booking has been added to your account.");
            alert.showAndWait();

            hide();
        }
        catch (RuntimeException e)
        {
            showError(e.getMessage());
        }
        finally
        {
            saveAddBookingButton.setDisable(false);
        }
    }

  private void showError(String message)
  {
    addBookingErrorLabel.setText(message == null ? "Could not add booking."
        : message);
    addBookingErrorLabel.setVisible(true);
    addBookingErrorLabel.setManaged(true);
  }

  private void hideError()
  {
    addBookingErrorLabel.setVisible(false);
    addBookingErrorLabel.setManaged(false);
  }
}


