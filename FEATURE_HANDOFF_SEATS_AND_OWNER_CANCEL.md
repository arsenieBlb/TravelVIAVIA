# TravelVIAVIA Feature Handoff

## Requested Features

1. Booking must require seat selection.
2. Booking cancellation must only be possible for the customer who owns the booking.

This file is the takeover artifact. No feature code has been implemented yet.

## Project Understanding

The project is a Java/JavaFX client-server desktop app.

- `src/Main.java` launches the JavaFX client through `ModelConsoleSimulation`.
- The customer UI lives mainly in `src/client/view` and `src/client/viewmodel`.
- The client talks to the server through `src/client/mediator/Client.java`.
- The server receives JSON socket requests in `src/server/mediator/ClientHandler.java`.
- Each connected client gets its own `ServerSessionModel`, so each socket has its own logged-in user.
- The shared application logic is in `src/server/model/ModelManager.java`.
- Database work is handled by DAOs in `src/server/database`.
- DTOs in `src/server/network/dto` are used so the socket layer does not send model objects directly.

Current booking path:

1. Customer selects flights in `FlightSceneViewController` / `FlightSceneViewModel`.
2. Passenger and seat data are collected in `PassengerDetailsViewController` / `PassengerDetailsViewModel`.
3. `PassengerDetailsViewModel.confirmBooking()` creates passengers and seat lists.
4. `Client.createBooking(...)` sends a `BookingRequest`.
5. `ClientHandler.createBooking(...)` turns the request back into model objects.
6. `ServerSessionModel.createBooking(...)` passes the current socket user to `ModelManager.createBookingInternal(...)`.
7. `ModelManager` creates a booking, assigns seats, saves it through `BookingDAO.saveBooking(...)`, then confirms it.

Current cancellation path:

1. Customer opens a booking in `BookingDetailsDialogController`.
2. The dialog calls `MyBookingsViewModel.cancelBooking(...)`.
3. The client sends `RequestType.CANCEL_BOOKING` with the booking id.
4. `ClientHandler.cancelBooking(...)` looks for that id in `model.getUserBookings()`.
5. `ModelManager.cancelBooking(currentUser, booking)` removes the booking from the database.

Important database tables:

- `flights.booking.created_by_customer_id` stores the booking owner.
- `flights.booking_customer` links bookings to customers, including bookings added by booking code.
- `flights.flight_seat` stores occupied seats per flight and passenger.

Important current issue:

- A linked booking can appear in a customer's "My Bookings" because of `booking_customer`.
- Cancelling currently deletes the whole booking after it is found in `getUserBookings()`.
- Therefore ownership must be checked against `booking.created_by_customer_id`, not only against `booking_customer`.

Known existing workspace change:

- `src/server/database/DatabaseConnection.java` is already modified in git status. Do not touch or revert it.

## Feature 1: Obligatory Seat Selection

### Current behavior

`PassengerDetailsViewModel.confirmBooking()` collects seats like this:

- outbound seats go into `selectedSeats`
- return seats go into `returnSeats`
- missing seats are currently allowed as `null`

`ModelManager.createBookingInternal(...)` also skips missing seats because it checks `if (seatIndex < selectedSeats.size())` and then only assigns non-null seats.

So a customer can confirm a booking without selecting every required seat.

### Planned behavior

A booking will require one selected seat for every passenger and every flight segment.

Examples:

- 1 passenger, direct one-way flight: 1 seat required.
- 2 passengers, direct one-way flight: 2 seats required.
- 1 passenger, connecting one-way flight with 2 segments: 2 seats required.
- 1 passenger, direct round trip: 2 seats required, one outbound and one return.
- 2 passengers, connecting round trip with 2 outbound and 2 return segments: 8 seats required.

### Files to change

#### `src/client/viewmodel/PassengerDetailsViewModel.java`

Add a simple validation before passengers are created in `confirmBooking()`.

Planned helper methods:

- `private void requireAllSeatsSelected()`
- `private String segmentName(int segmentIndex)` or a similarly small helper if a useful error message is wanted

Simple approach:

- Loop through every `PassengerForm`.
- Loop through every segment index from `0` to `getAllSegments().size() - 1`.
- If `form.getSelectedSeat(segmentIndex) == null`, throw `IllegalStateException`.
- Message should be normal user-facing text, for example: `Please choose a seat for every passenger and flight segment.`

This keeps the rule close to the UI form data and stops the booking before passengers are built.

#### `src/server/model/ModelManager.java`

Add the same rule on the server side so the client cannot bypass it.

Planned helper methods:

- `private void requireSelectedSeats(List<Passenger> passengers, List<Flight> segments, List<Seat> seats)`
- `private List<Flight> getSegments(Flight flight)`

Use it before assigning outbound seats, and again before assigning return seats if `returnFlight != null`.

Simple rule:

- Expected seat count is `passengers.size() * segments.size()`.
- If the list is null, too short, or contains null before the expected count, throw `IllegalArgumentException`.
- Error text can match the client: `Please choose a seat for every passenger and flight segment.`

Do not remove the existing seat assignment loops. Reuse them after validation.

#### `src/server/network/dto/BookingRequest.java`

The current request only has:

- `flight`
- `passengers`
- `selectedSeats`

But the viewmodel already passes `returnFlight` and `returnSeats` to the model. `Client.createBooking(...)` currently drops those values when it creates the DTO.

To keep mandatory seats correct for round trips, extend the DTO with:

- `public FlightDto returnFlight;`
- `public List<SeatDto> returnSeats = new ArrayList<>();`

Add a constructor overload or extend the existing constructor so current calls still stay simple.

#### `src/client/mediator/Client.java`

Update the 5-argument `createBooking(...)` method so it sends:

- outbound flight
- passengers
- outbound seats
- return flight
- return seats

Do this by using the extended `BookingRequest`.

#### `src/server/mediator/ClientHandler.java`

Update `createBooking(...)` so it reads:

- `bookingRequest.returnFlight`
- `bookingRequest.returnSeats`

Resolve the return flight using the same `resolveFlightFromModel(...)` helper if the return flight is present.

Call:

`model.createBooking(flight, passengers, selectedSeats, returnFlight, returnSeats)`

instead of the current 3-argument version.

Also update the logging payload to include `returnSeats` count if useful, but keep it simple.

## Feature 2: Cancel Only If Owner

### Current behavior

`ClientHandler.cancelBooking(...)` checks the current user's visible bookings with `getUserBookings()`.

This is not enough because `getUserBookings()` uses `booking_customer`, and `booking_customer` can include bookings that were added by booking code.

### Planned behavior

Only the customer stored in `flights.booking.created_by_customer_id` can cancel the booking.

### Files to change

#### `src/server/database/BookingDAO.java`

Add:

`public boolean isBookingOwner(int bookingId, int customerId) throws SQLException`

SQL:

`SELECT 1 FROM flights.booking WHERE booking_id = ? AND created_by_customer_id = ?`

Return true if a row exists.

This keeps the owner rule in the database layer where the real owner information lives.

#### `src/server/model/ModelManager.java`

In `cancelBooking(User user, Booking booking)`:

- Keep the existing customer account check.
- Before releasing seats or deleting rows, call `bookingDAO.isBookingOwner(...)`.
- If it returns false, throw `IllegalArgumentException("Only the booking owner can cancel this booking.")`.
- If it returns true, continue with the current cancellation logic.

This is the main security rule.

#### `src/client/view/BookingDetailsDialogController.java`

Wrap `viewModel.cancelBooking(currentBooking)` in a `try/catch`.

If cancellation fails, show an error alert with the message from the exception and do not hide the dialog.

This prevents a server-side owner rejection from turning into an unhandled UI exception.

No complicated client-side owner guessing is planned, because the server is the reliable source and the current client booking object may not safely distinguish owner vs linked customer.

## Tests To Add Or Update

### `test/CustomerBookingZombeTest.java`

Add a test for mandatory seats in the existing customer booking flow.

Suggested test:

- Select a flight.
- Prepare passenger details.
- Do not choose a seat.
- Call `confirmBooking()`.
- Assert `IllegalStateException`.
- Assert `model.createBookingCalls == 0`.

Existing tests already cover:

- successful one-seat booking
- multiple passengers and seats
- round-trip outbound/return seats
- wrong class, taken seat, and duplicate seat rejection

Those should still pass.

### Manual/server verification

Because owner cancellation depends on the database, verify manually or with a small integration check:

1. Customer A creates a booking.
2. Customer B adds that booking by booking id and passenger last name.
3. Customer B opens the booking and tries to cancel.
4. Expected: error message `Only the booking owner can cancel this booking.`
5. Customer A cancels the same booking.
6. Expected: booking is cancelled and removed.

## Implementation Style Rules

Follow the user's rules:

- Keep code simple and readable.
- Do not remove unrelated group-project code.
- Add small helper methods only where they make the flow easier to read.
- Put validation in the classes that already own the booking flow.
- Do not add comments unless they explain an existing block in normal human wording.
- Do not touch generated `out` files.
- Do not touch `src/server/database/DatabaseConnection.java`.

## Suggested Implementation Order

1. Add client-side mandatory seat validation in `PassengerDetailsViewModel`.
2. Extend `BookingRequest` for return flight seats.
3. Update `Client.createBooking(...)` and `ClientHandler.createBooking(...)` to carry return flight seat data.
4. Add server-side mandatory seat validation in `ModelManager`.
5. Add `BookingDAO.isBookingOwner(...)`.
6. Add owner check in `ModelManager.cancelBooking(...)`.
7. Catch cancellation errors in `BookingDetailsDialogController`.
8. Add/update tests.
9. Run available tests or at least compile the changed source.

## Main Risks

- Round-trip booking currently looks partly implemented but the DTO drops return flight fields. This should be fixed with the mandatory seat work so return seats are not lost.
- `BookingDAO.getBookingsForCustomer(...)` uses the current customer when creating loaded `Booking` objects, even though the database also stores the creator. For this feature, server-side `isBookingOwner(...)` avoids relying on that object identity.
- The project has no Gradle/Maven build file, so verification may need IntelliJ or manual `javac`/JUnit classpath setup.
