package client.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import client.viewmodel.DashboardViewModel;

public class DashboardViewController {
    @FXML private Label totalFlightsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label totalPassengersLabel;

    private Region root;
    private DashboardViewModel viewModel;
    private ViewHandler viewHandler;

    public void init(DashboardViewModel viewModel, Region root, ViewHandler viewHandler) {
        this.viewModel = viewModel;
        this.root = root;
        this.viewHandler = viewHandler;

        totalFlightsLabel.textProperty().bind(viewModel.totalFlightsProperty());
        activeBookingsLabel.textProperty().bind(viewModel.activeBookingsProperty());
        totalPassengersLabel.textProperty().bind(viewModel.totalPassengersProperty());
    }

    public Region getRoot() {
        return root;
    }
}

