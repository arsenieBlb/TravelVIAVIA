package viewmodel;

import model.Model;

public class ViewModelFactory
{

    private Model model;
    private FlightSceneViewModel flightSceneViewModel;
    private SeatMapViewModel seatMapViewModel;
    private BookFlightViewModel bookFlightViewModel;
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

    public BookFlightViewModel getBookFlightViewModel()
    {
        if (bookFlightViewModel == null)
        {
            bookFlightViewModel = new BookFlightViewModel(model);
        }
        return bookFlightViewModel;
    }

    public PassengerDetailsViewModel getPassengerDetailsViewModel()
    {
        if (passengerDetailsViewModel == null)
        {
            passengerDetailsViewModel = new PassengerDetailsViewModel(model,
                    getBookFlightViewModel());
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
