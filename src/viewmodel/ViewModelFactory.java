package viewmodel;

import model.Model;

public class ViewModelFactory
{

    private Model model;
    private FlightSceneViewModel flightSceneViewModel;
    private SeatMapViewModel seatMapViewModel;
    private PassengerDetailsViewModel passengerDetailsViewModel;
    private MyBookingsViewModel myBookingsViewModel;

    public ViewModelFactory(Model model)
    {
        this.model = model;
    }

    public Model getModel()
    {
        return model;
    }

    public FlightSceneViewModel getFlightSceneViewModel()
    {
        if (flightSceneViewModel == null)
        {
            flightSceneViewModel = new FlightSceneViewModel(model);
        }
        return flightSceneViewModel;
    }

    public SeatMapViewModel getSeatMapViewModel()
    {
        if (seatMapViewModel == null)
        {
            seatMapViewModel = new SeatMapViewModel(getPassengerDetailsViewModel());
        }
        return seatMapViewModel;
    }

    public PassengerDetailsViewModel getPassengerDetailsViewModel()
    {
        if (passengerDetailsViewModel == null)
        {
            passengerDetailsViewModel = new PassengerDetailsViewModel(model,
                    getFlightSceneViewModel());
        }
        return passengerDetailsViewModel;
    }

    public MyBookingsViewModel getMyBookingsViewModel()
    {
        if (myBookingsViewModel == null)
        {
            myBookingsViewModel = new MyBookingsViewModel(model);
        }
        return myBookingsViewModel;
    }
}
