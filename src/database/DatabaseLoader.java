package database;

import model.Carrier;
import model.City;
import model.Customer;
import model.Flight;
import model.FlightSearchService;
import model.LuggageType;
import model.Plane;
import model.PlaneType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseLoader
{
  private CityDAO cityDAO;
  private CarrierDAO carrierDAO;
  private PlaneDAO planeDAO;
  private FlightDAO flightDAO;
  private LuggageTypeDAO luggageTypeDAO;

  // stores everything loaded from the database
  private List<City> cities;
  private List<Carrier> carriers;
  private List<PlaneType> planeTypes;
  private List<Plane> planes;
  private List<LuggageType> luggageTypes;
  private List<Flight> flights;

  public DatabaseLoader()
  {
    this.cityDAO = new CityDAO();
    this.carrierDAO = new CarrierDAO();
    this.planeDAO = new PlaneDAO();
    this.flightDAO = new FlightDAO();
    this.luggageTypeDAO = new LuggageTypeDAO();
  }

  // loads all data from the database in the right order
  public FlightSearchService loadAll() throws SQLException
  {
    System.out.println("Loading data from database...");

    // loads cities first since flights need them
    cities = cityDAO.getAllCities();
    System.out.println("Loaded " + cities.size() + " cities.");

    // loads carriers since planes and flights need them
    carriers = carrierDAO.getAllCarriers();
    System.out.println("Loaded " + carriers.size() + " carriers.");

    // loads plane types since planes need them
    planeTypes = planeDAO.getAllPlaneTypes();
    System.out.println("Loaded " + planeTypes.size() + " plane types.");

    // loads planes with their seats
    planes = planeDAO.getAllPlanes(planeTypes, carriers);
    System.out.println("Loaded " + planes.size() + " planes.");

    // loads luggage types for bookings
    luggageTypes = luggageTypeDAO.getAllLuggageTypes();
    System.out.println("Loaded " + luggageTypes.size() + " luggage types.");

    // loads all available flights and connects everything together
    flights = flightDAO.getAllFlights(carriers, planes, cities);
    System.out.println("Loaded " + flights.size() + " flights.");

    // puts all the loaded flights into the FlightSearchService
    FlightSearchService service = new FlightSearchService(flights);

    System.out.println("Database loading complete.");
    return service;
  }

  public List<City> getCities()
  {
    return cities;
  }

  public List<Carrier> getCarriers()
  {
    return carriers;
  }

  public List<LuggageType> getLuggageTypes()
  {
    return luggageTypes;
  }

  public List<Flight> getFlights()
  {
    return flights;
  }

  public List<Plane> getPlanes()
  {
    return planes;
  }
}
