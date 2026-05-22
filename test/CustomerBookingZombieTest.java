import client.model.Booking;
import client.model.BusinessClass;
import client.model.ConnectingFlight;
import client.model.EconomyClass;
import client.model.Seat;
import client.viewmodel.PassengerDetailsViewModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerBookingZombieTest extends BookingTestBase
{
  @Test
  void zeroNoSelectedFlightShouldBlockPassengerDetailsAndBooking()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();

    IllegalStateException prepareError = assertThrows(
        IllegalStateException.class, passengerDetails::prepare);
    IllegalStateException bookingError = assertThrows(
        IllegalStateException.class, passengerDetails::confirmBooking);

    assertEquals("Please select a flight first.", prepareError.getMessage());
    assertEquals("Please select a flight first.", bookingError.getMessage());
    assertEquals(0, model.createBookingCalls);
  }

  @Test
  void onePassengerShouldBookOneWayEconomyFlight()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    Seat selectedSeat = firstSeat(outbound, EconomyClass.class);
    chooseSeat(passengerDetails, 1, 0, selectedSeat);

    Booking booking = passengerDetails.confirmBooking();

    assertSame(model.createdBooking, booking);
    assertEquals(100, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(15, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(0, passengerDetails.baggageFareProperty().get(), 0.001);
    assertEquals(115, passengerDetails.totalFareProperty().get(), 0.001);

    CapturedBooking captured = verifyCreatedBooking(outbound, null);
    assertEquals(1, captured.passengers().size());
    assertEquals("Artem", captured.passengers().get(0).getFirstName());
    assertEquals("Twelve", captured.passengers().get(0).getLastName());
    assertEquals(1,
        captured.passengers().get(0).getPassengerLuggage().size());
    assertEquals("Carry-on", captured.passengers().get(0)
        .getPassengerLuggage().get(0).getLuggageType().getName());
    assertEquals(List.of(selectedSeat), captured.outboundSeats());
  }

  @Test
  void manyPassengersShouldKeepSeatsLuggageAndFareTotals()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    var first = passengerDetails.getPassengerForms().get(0);
    var second = passengerDetails.getPassengerForms().get(1);
    first.baggageQuantityProperty().set(2);
    second.setFirstName("Alex");
    second.setLastName("Smith");
    second.carryOnQuantityProperty().set(2);
    second.baggageQuantityProperty().set(1);
    second.seatClassProperty(0).set(new BusinessClass());

    Seat firstSeat = firstSeat(outbound, EconomyClass.class);
    Seat secondSeat = firstSeat(outbound, BusinessClass.class);
    chooseSeat(passengerDetails, 1, 0, firstSeat);
    chooseSeat(passengerDetails, 2, 0, secondSeat);

    assertEquals(350, passengerDetails.baseFareProperty().get(), 0.001);
    assertEquals(45, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(195, passengerDetails.baggageFareProperty().get(), 0.001);
    assertEquals(590, passengerDetails.totalFareProperty().get(), 0.001);

    passengerDetails.confirmBooking();

    CapturedBooking captured = verifyCreatedBooking(outbound, null);
    assertEquals(2, captured.passengers().size());
    assertEquals(List.of(firstSeat, secondSeat), captured.outboundSeats());
    assertEquals(2, captured.passengers().get(0).getPassengerLuggage().size());
    assertEquals(2, captured.passengers().get(1).getPassengerLuggage().size());
  }

  @Test
  void boundaryPassengerAndLuggageControlsShouldUseExpectedLimits()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    for (int i = 0; i < 20; i++)
    {
      flightSceneViewModel.incrementPassengers();
    }
    assertEquals(9, flightSceneViewModel.passengerCountProperty().get());

    for (int i = 0; i < 20; i++)
    {
      flightSceneViewModel.decrementPassengers();
    }
    assertEquals(1, flightSceneViewModel.passengerCountProperty().get());

    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();
    var form = passengerDetails.getPassengerForms().get(0);

    form.carryOnQuantityProperty().set(0);
    form.baggageQuantityProperty().set(0);
    assertEquals(0, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(0, passengerDetails.baggageFareProperty().get(), 0.001);

    form.carryOnQuantityProperty().set(
        PassengerDetailsViewModel.MAX_CARRY_ON_BAGS);
    form.baggageQuantityProperty().set(5);
    assertEquals(30, passengerDetails.carryOnFareProperty().get(), 0.001);
    assertEquals(325, passengerDetails.baggageFareProperty().get(), 0.001);
  }

  @Test
  void interfaceSearchSortRoundTripAndReserveShouldCallModelCorrectly()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.sortFlights("Price (Low to High)");
    assertEquals(cheapFlight, flightSceneViewModel.getFilteredFlights().get(0));

    flightSceneViewModel.filterByCarrier("VIA Air");
    assertTrue(flightSceneViewModel.getFilteredFlights().stream()
        .allMatch(flight -> flight.getCarrier().equals(viaAir)));

    flightSceneViewModel.directOnlyProperty().set(true);
    flightSceneViewModel.searchFlights();
    assertFalse(flightSceneViewModel.getFilteredFlights().stream()
        .anyMatch(ConnectingFlight.class::isInstance));

    flightSceneViewModel.roundTripProperty().set(true);
    flightSceneViewModel.departureCityProperty().set(berlin);
    flightSceneViewModel.arrivalCityProperty().set(paris);
    flightSceneViewModel.travelDateProperty().set(
        outbound.getDepartureTime().toLocalDate());
    flightSceneViewModel.returnDateProperty().set(
        returnFlight.getDepartureTime().toLocalDate());
    flightSceneViewModel.searchFlights();
    flightSceneViewModel.setSelectedFlight(outbound);
    flightSceneViewModel.setSelectedReturnFlight(returnFlight);

    assertTrue(flightSceneViewModel.getReturnFlights().contains(returnFlight));

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
  void exceptionInvalidPassengerAndSeatActionsShouldThrow()
  {
    var flightSceneViewModel = createFlightSceneViewModel();
    flightSceneViewModel.incrementPassengers();
    flightSceneViewModel.setSelectedFlight(outbound);
    var passengerDetails = flightSceneViewModel.getPassengerDetailsViewModel();
    passengerDetails.prepare();

    var first = passengerDetails.getPassengerForms().get(0);
    var second = passengerDetails.getPassengerForms().get(1);
    passengerDetails.selectSeatForPassenger(1, 0, economySeats(outbound).get(0));
    passengerDetails.selectSeatForPassenger(2, 0, economySeats(outbound).get(1));
    first.setFirstName("");
    assertThrows(IllegalStateException.class, passengerDetails::confirmBooking);

    first.setFirstName("Artem");
    second.setFirstName("Alex");
    second.setLastName("Smith");

    Seat businessSeat = firstSeat(outbound, BusinessClass.class);
    assertThrows(IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(1, 0, businessSeat));

    Seat takenSeat = economySeats(outbound).get(1);
    outbound.markSeatOccupied(takenSeat);
    assertThrows(IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(1, 0, takenSeat));

    Seat sharedSeat = economySeats(outbound).get(2);
    passengerDetails.selectSeatForPassenger(1, 0, sharedSeat);
    assertThrows(IllegalArgumentException.class,
        () -> passengerDetails.selectSeatForPassenger(2, 0, sharedSeat));
    assertEquals(0, model.createBookingCalls);
  }
}
