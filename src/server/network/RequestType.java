package server.network;

public final class RequestType
{
  public static final String LOGIN = "LOGIN";
  public static final String REGISTER = "REGISTER";
  public static final String LOGOUT = "LOGOUT";
  public static final String GET_LOGGED_IN_USER = "GET_LOGGED_IN_USER";
  public static final String IS_LOGGED_IN = "IS_LOGGED_IN";
  public static final String SEARCH_FLIGHTS = "SEARCH_FLIGHTS";
  public static final String GET_FLIGHT_DETAILS = "GET_FLIGHT_DETAILS";
  public static final String CREATE_BOOKING = "CREATE_BOOKING";
  public static final String CANCEL_BOOKING = "CANCEL_BOOKING";
  public static final String GET_ALL_BOOKINGS = "GET_ALL_BOOKINGS";
  public static final String GET_USER_BOOKINGS = "GET_USER_BOOKINGS";
  public static final String ADD_BOOKING_TO_CURRENT_USER_BY_ID = "ADD_BOOKING_TO_CURRENT_USER_BY_ID";
  public static final String REMOVE_BOOKING_FROM_CURRENT_USER = "REMOVE_BOOKING_FROM_CURRENT_USER";
  public static final String ADD_FLIGHT = "ADD_FLIGHT";
  public static final String REMOVE_FLIGHT = "REMOVE_FLIGHT";
  public static final String EDIT_FLIGHT = "EDIT_FLIGHT";
  public static final String GET_LUGGAGE_TYPES = "GET_LUGGAGE_TYPES";
  public static final String GET_ALL_CITIES = "GET_ALL_CITIES";
  public static final String GET_CITIES = "GET_CITIES";
  public static final String GET_PLANES = "GET_PLANES";
  public static final String GET_CARRIERS = "GET_CARRIERS";
  public static final String GET_ALL_FLIGHTS = "GET_ALL_FLIGHTS";
  public static final String BROADCAST_PROPERTY_CHANGE = "BROADCAST_PROPERTY_CHANGE";

  private RequestType()
  {
  }
}


