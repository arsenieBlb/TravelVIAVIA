package viewmodel;

import model.Model;

public class ViewModelFactory
{

    private Model model;
    private FlightSceneViewModel flightSceneViewModel;

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
}
