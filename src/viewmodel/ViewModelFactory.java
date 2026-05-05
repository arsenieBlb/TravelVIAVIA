package viewmodel;

import model.Model;

public class ViewModelFactory
{

    private Model model;
    private FlightSceneViewModel flightSceneViewModel;
    private SeatMapViewModel seatMapViewModel;
    private BookFlightViewModel bookFlightViewModel;

    public ViewModelFactory(Model model)
    {
        this.model = model;
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
            seatMapViewModel = new SeatMapViewModel(getFlightSceneViewModel());
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
}
