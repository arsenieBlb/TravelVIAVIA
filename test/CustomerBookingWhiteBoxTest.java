import client.model.Booking;
import client.model.BusinessClass;
import client.model.ConnectingFlight;
import client.model.EconomyClass;
import client.model.Flight;
import client.model.Passenger;
import client.model.Seat;
import client.model.SeatAssignment;
import client.view.PassengerDetailsViewController;
import client.viewmodel.FlightSceneViewModel;
import client.viewmodel.PassengerDetailsViewModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerBookingWhiteBoxTest extends BookingTestBase
{
  @Test
  void bookingWithoutEverySeatShouldBeBlocked()
  {
    FlightSceneViewModel flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    PassengerDetailsViewModel passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    IllegalStateException error = assertThrows(IllegalStateException.class,
        passengerDetails::confirmBooking);

    assertEquals("Please choose a seat for every passenger and flight segment.",
        error.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  void firstPassengerAutofillShouldRefreshFromLoggedInCustomer()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    var firstPassenger = passengerDetails.getPassengerForms().get(0);
    firstPassenger.setFirstName("Old");
    firstPassenger.setLastName("Memory");

    passengerDetails.prepare();

    assertEquals("Artem", firstPassenger.getFirstName());
    assertEquals("Twelve", firstPassenger.getLastName());
  }

  @Test
  void bookingConfirmationMessageShouldUseReferenceCode()
  {
    Booking booking = new Booking(1234, customer, outbound,
        List.of(new Passenger(2000, "Artem", "Twelve")));

    String message =
        PassengerDetailsViewController.formatBookingSuccessMessage(booking);

    assertEquals("Booking reference code: #1234 has been saved.", message);
    assertTrue(message.contains("reference code"));
  }

  @Test
  void cancellingLoadedAssignmentShouldFreeOriginalSeat()
  {
    Passenger originalPassenger = new Passenger(2001, "Artem", "Twelve");
    Booking originalBooking = new Booking(customer, outbound,
        List.of(originalPassenger));
    Seat seat = firstSeat(outbound, EconomyClass.class);
    new SeatAssignment(99, originalPassenger, seat, outbound);

    assertFalse(outbound.getAvailableSeats().contains(seat));

    Passenger loadedPassenger = new Passenger(originalPassenger.getPassengerId(),
        "Artem", "Twelve");
    new Booking(originalBooking.getBookingId(), customer, outbound,
        List.of(loadedPassenger));
    SeatAssignment loadedAssignment = new SeatAssignment(outbound, seat,
        loadedPassenger);
    loadedPassenger.addSeatAssignment(loadedAssignment);
    outbound.markSeatOccupied(seat);

    loadedAssignment.release();

    assertTrue(outbound.getAvailableSeats().contains(seat));
  }

  @Test
  void connectedRoundTripBookingShouldMatchSummaryTotal()
  {
    Flight returnFirst = createFlight(7, "VIA701", paris, rome, viaAir, 60,
        returnFlight.getDepartureTime().plusHours(1));
    Flight returnSecond = createFlight(8, "VIA702", rome, berlin, viaAir, 70,
        returnFlight.getDepartureTime().plusHours(4));
    ConnectingFlight returnConnection =
        new ConnectingFlight(returnFirst, returnSecond);

    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(connectingFlight);
    flightSceneViewModel.setSelectedReturnFlight(returnConnection);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    var form = passengerDetails.getPassengerForms().get(0);
    form.seatClassProperty(1).set(new BusinessClass());

    chooseSeat(passengerDetails, 1, 0,
        firstSeat(connectingFlight.getFirstSegment(), EconomyClass.class));
    chooseSeat(passengerDetails, 1, 1,
        firstSeat(connectingFlight.getSecondSegment(), BusinessClass.class));
    chooseSeat(passengerDetails, 1, 2,
        firstSeat(returnFirst, EconomyClass.class));
    chooseSeat(passengerDetails, 1, 3,
        firstSeat(returnSecond, EconomyClass.class));

    double expectedTotal = 75 + (95 * 2.5) + 60 + 70
        + PassengerDetailsViewModel.CARRY_ON_UNIT_PRICE;
    assertEquals(expectedTotal, passengerDetails.totalFareProperty().get(),
        0.001);

    Booking booking = passengerDetails.confirmBooking();

    assertEquals(expectedTotal, booking.getTotalPrice(), 0.001);
    assertTrue(booking.getBookingSummary().contains(
        "Total price: EUR " + String.format("%.2f", expectedTotal)));
  }
}
