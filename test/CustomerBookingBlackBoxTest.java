import client.model.Booking;
import client.model.BusinessClass;
import client.model.EconomyClass;
import client.model.Flight;
import client.model.Seat;
import client.view.PassengerDetailsViewController;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerBookingBlackBoxTest extends BookingTestBase
{
  @Test
  void validLoginSucceedsBeforeBookingFlow()
  {
    var flightSceneViewModel = createFlightSceneViewModel();

    boolean loggedIn = flightSceneViewModel.login("artem@gmail.com",
        "ArtemGetsA12");

    assertTrue(loggedIn);
    assertTrue(customer.isLoggedIn());
    assertSame(customer, flightSceneViewModel.getLoggedInUser());
  }

  @Test
  void flightSearchReturnsAvailableMatchingFlights()
  {
    var flightSceneViewModel = createFlightSceneViewModel();

    flightSceneViewModel.departureCityProperty().set(berlin);
    flightSceneViewModel.arrivalCityProperty().set(paris);
    flightSceneViewModel.travelDateProperty().set(
        outbound.getDepartureTime().toLocalDate());
    flightSceneViewModel.searchFlights();

    List<Flight> results = new ArrayList<>(
        flightSceneViewModel.getFilteredFlights());
    assertTrue(results.contains(outbound));
    assertTrue(results.contains(cheapFlight));
    assertFalse(results.contains(expensiveFlight));
    assertTrue(results.stream().allMatch(flight ->
        flight.getDepartureCity().equals(berlin)
            && flight.getArrivalCity().equals(paris)));
  }

  @Test
  void selectingFlightStoresSelectedOutboundFlight()
  {
    var flightSceneViewModel = createFlightSceneViewModel();

    flightSceneViewModel.setSelectedFlight(outbound);

    assertSame(outbound, flightSceneViewModel.getSelectedFlight());
  }

  @Test
  void oneWayBookingCreatesBookingWithoutReturnFlight()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    Seat selectedSeat = firstSeat(outbound, EconomyClass.class);

    chooseSeat(passengerDetails, 1, 0, selectedSeat);
    Booking booking = passengerDetails.confirmBooking();

    assertSame(model.createdBooking, booking);
    CapturedBooking captured = verifyCreatedBooking(outbound, null);
    assertEquals(1, captured.passengers().size());
    assertEquals("Artem", captured.passengers().get(0).getFirstName());
    assertEquals("Twelve", captured.passengers().get(0).getLastName());
    assertEquals(List.of(selectedSeat), captured.outboundSeats());
    assertNull(captured.returnSeats());
  }

  @Test
  void roundTripBookingIncludesReturnFlightAndReturnSeat()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.roundTripProperty().set(true);
    flightSceneViewModel.setSelectedFlight(outbound);
    flightSceneViewModel.setSelectedReturnFlight(returnFlight);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    Seat outboundSeat = firstSeat(outbound, EconomyClass.class);
    Seat returnSeat = firstSeat(returnFlight, EconomyClass.class);

    chooseSeat(passengerDetails, 1, 0, outboundSeat);
    chooseSeat(passengerDetails, 1, 1, returnSeat);
    passengerDetails.confirmBooking();

    CapturedBooking captured = verifyCreatedBooking(outbound, returnFlight);
    assertEquals(List.of(outboundSeat), captured.outboundSeats());
    assertEquals(List.of(returnSeat), captured.returnSeats());
  }

  @Test
  void passengerCountCreatesCorrectNumberOfPassengerForms()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();

    passengerDetails.prepare();

    assertEquals(3, passengerDetails.getPassengerForms().size());
  }

  @Test
  void bookingIsBlockedWhenNoFlightIsSelected()
  {
    var passengerDetails =
        createFlightSceneViewModel().getPassengerDetailsViewModel();

    IllegalStateException prepareError = assertThrows(
        IllegalStateException.class, passengerDetails::prepare);
    IllegalStateException bookingError = assertThrows(
        IllegalStateException.class, passengerDetails::confirmBooking);

    assertEquals("Please select a flight first.", prepareError.getMessage());
    assertEquals("Please select a flight first.", bookingError.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  void bookingIsBlockedWhenPassengerNameIsMissing()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    chooseSeat(passengerDetails, 1, 0, firstSeat(outbound, EconomyClass.class));
    passengerDetails.getPassengerForms().get(0).setFirstName("");

    IllegalStateException error = assertThrows(IllegalStateException.class,
        passengerDetails::confirmBooking);

    assertEquals("First name is required.", error.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  void bookingIsBlockedWhenEveryRequiredSeatIsNotSelected()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    IllegalStateException error = assertThrows(IllegalStateException.class,
        passengerDetails::confirmBooking);

    assertEquals("Please choose a seat for every passenger and flight segment.",
        error.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  void baggageChangesUpdateCarryOnBaggageAndTotalFare()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    var form = passengerDetails.getPassengerForms().get(0);

    form.carryOnQuantityProperty().set(2);
    form.baggageQuantityProperty().set(1);

    assertEquals(100, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(30, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(65, passengerDetails.baggageFareProperty().get(), 0.001);
    assertEquals(195, passengerDetails.totalFareProperty().get(), 0.001);
  }

  @Test
  void economyAndBusinessSeatClassesUpdateFare()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    var form = passengerDetails.getPassengerForms().get(0);

    form.seatClassProperty(0).set(new BusinessClass());

    assertEquals(250, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(265, passengerDetails.totalFareProperty().get(), 0.001);

    form.seatClassProperty(0).set(new EconomyClass());

    assertEquals(100, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(115, passengerDetails.totalFareProperty().get(), 0.001);
  }

  @Test
  void wrongClassTakenAndDuplicateSeatSelectionsThrowErrors()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    Seat businessSeat = firstSeat(outbound, BusinessClass.class);
    IllegalArgumentException wrongClassError = assertThrows(
        IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(1, 0, businessSeat));
    assertEquals("Seat does not match the selected class.",
        wrongClassError.getMessage());

    Seat takenSeat = economySeats(outbound).get(0);
    outbound.markSeatOccupied(takenSeat);
    IllegalArgumentException takenSeatError = assertThrows(
        IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(1, 0, takenSeat));
    assertEquals("Seat is already taken.", takenSeatError.getMessage());

    Seat sharedSeat = economySeats(outbound).get(1);
    passengerDetails.selectSeatForPassenger(1, 0, sharedSeat);
    IllegalArgumentException duplicateSeatError = assertThrows(
        IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(2, 0, sharedSeat));
    assertEquals("Another passenger already selected this seat.",
        duplicateSeatError.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  void successfulConfirmationReturnsBookingReferenceMessage()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    chooseSeat(passengerDetails, 1, 0, firstSeat(outbound, EconomyClass.class));

    Booking booking = passengerDetails.confirmBooking();
    String message =
        PassengerDetailsViewController.formatBookingSuccessMessage(booking);

    assertEquals("Booking reference code: #" + booking.getBookingId()
        + " has been saved.", message);
    assertTrue(message.contains("reference code"));
  }
}
