package viewmodel;

import model.Model;
import view.NavigationAdminViewController;

public class ViewModelFactory
{

    private Model model;
    private FlightSceneViewModel flightSceneViewModel;
    private SeatMapViewModel seatMapViewModel;
    private PassengerDetailsViewModel passengerDetailsViewModel;
    private MyBookingsViewModel myBookingsViewModel;
    private NavigationAdminViewModel navigationAdminViewModel;
    private FlightsTabViewModel flightsTabViewModel;
    private AddFlightTabViewModel addFlightTabViewModel;

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

    public NavigationAdminViewModel getNavigationAdminViewModel()
    {
        if (navigationAdminViewModel == null)
        {
            navigationAdminViewModel = new NavigationAdminViewModel(model);
        }
        return navigationAdminViewModel;
    }

    public FlightsTabViewModel getFlightsTabViewModel()
    {
        if (flightsTabViewModel == null)
        {
            flightsTabViewModel = new FlightsTabViewModel(model);
        }
        return flightsTabViewModel;
    }

    public AddFlightTabViewModel getAddFlightTabViewModel()
    {
        if (addFlightTabViewModel == null)
        {
            addFlightTabViewModel = new AddFlightTabViewModel(model);
        }
        return addFlightTabViewModel;
    }
}
