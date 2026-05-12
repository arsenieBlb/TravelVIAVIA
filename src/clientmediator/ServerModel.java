package clientmediator;

import model.Flight;
import model.Model;

import java.beans.PropertyChangeListener;
import java.util.List;

public interface ServerModel extends Model
{
  default List<Flight> getFlights()
  {
    return getAllFlights();
  }

  default void addListener(PropertyChangeListener listener)
  {
    addPropertyChangeListener(listener);
  }

  default void removeListener(PropertyChangeListener listener)
  {
    removePropertyChangeListener(listener);
  }
}
