Travel via Via 2nd Semester Project - Group 5
Analysis

Arsenie, Artem, Iulian, Marina, Sofia

VIA University College

Software Engineering 2X-S26

Supervisors: Allan Henriksen, Joseph Chukwudi

Number of Characters:


May – , 2026

Requirements:	3
Functional:	3
Non-Functional:	4
Use Cases:	6
Create Customer Account	6
Search Trips	8
Book Trip	10
Manage Own Bookings	12
Add Flight	15
Manage Flights	17
Manage Bookings	19
Use case to Requirement table:	21


Requirements:

Functional:
As a Customer, I want to book a selected trip for one or more passengers so that seats are reserved for me and/or others.
As a Customer, I want to provide passenger information for each traveler so that every passenger is connected to the booking.
As a Customer, I want to choose baggage options for each passenger so that the booking reflects each passenger’s travel needs.
As a Customer, I want to choose travel class for each passenger and flight so that the reservation matches the selected comfort level.
As a Customer, I want to choose seats for each passenger and flight so that every passenger has an assigned seat.
As a Customer, I want seat selection to be mandatory before confirming a booking so that no passenger in the booking is left without a seat.
As a Customer, I want selected seats to become unavailable to others so that no two passengers reserve the same seat on the same flight.
As a Customer, I want to see a booking summary before confirming so that I can check the selected trip, trip type, passengers, seats, baggage, travel classes, and total price.
As a Customer, I want the total reservation price to be calculated from my booking choices so that I can review the full cost before confirming.  
As a Customer, I want my booking to have a specific identifier so that the reservation can be accessed or identified later if needed. \
As an Anonymous Viewer or Customer, I want to view trip information including route, departure time, arrival time, duration, carrier, aircraft, stops, and price so that I can compare travel options.
As an Anonymous Viewer or Customer, I want to view trip information including route, departure time, arrival time, duration, carrier, aircraft, stops, and price so that I can compare travel options.
As a Customer, I want to see only available trips so that I can choose a suitable option for my trip and booking.
As a Customer, I want to include a return trip when needed so that I can book a round trip.
As a Customer, I want return dates earlier than departure dates to be rejected so that I can book only logical round-trip itineraries.
As a Customer, I want to be restricted from selecting dates prior to the current date, and prior to my departure date for round trips, so that I cannot search or book past trips.
As a Customer, I want unavailable flights or flights without available seats to be hidden or unavailable so that I only choose bookable options.
As a Customer, I want a trip to be composed of one or more flights so that direct, connected, and round-trip travel can be represented.
As an Anonymous Viewer, I want to create a customer account so that I can book and manage trips later.
As a Customer or Admin, I want to log in with role-based authentication so that protected actions are restricted to the correct user type.
As an Admin, I want to be the only one with access to flight data management, while customers can only view trips, so that management duties are restricted according to user roles.
As a Customer, I want bookings created by me to be automatically linked to my account, so that I can easily check information of my owned bookings.

As a Customer, I want to link a booking using booking code and passenger last name so that I can view a booking created by another customer.
As a Customer, I want linked bookings to be view-only unless I created the booking so that bookings cannot be changed by the wrong account.
As a Customer, I want to cancel a booking I created so that the reservation is no longer active.
As a Customer, I want cancelled booking seats to become available again so that other customers can reserve them.
As an Admin, I want to add a flight by providing flight ID, carrier, aircraft, origin, destination, departure time, arrival time, economy price, and business price so that the flight has the required information to become available for customer trips.
As an Admin, I want each flight to be registered as one point-to-point route from one origin to one destination so that flight data stays consistent.
As an Admin, I want to access flight information and seat details so that I can inspect full flight availability.
As an Admin, I want to edit an existing flight so that outdated or incorrect flight data can be corrected.
As an Admin, I want to remove a flight so that cancelled or invalid flights cannot be booked.
As an Admin, I want removal to be blocked when a flight is used in active bookings so that customer reservations are not impacted.
As an Admin, I want to view all bookings and identify them by customer account and booking identifier so that I can monitor reservations.
As an Admin, I want to find bookings by booking code or customer information so that I can find specific reservations efficiently.
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
As an Admin, I want to view the total number of flights, active bookings, and total passengers so that I can quickly understand the current operational state.


Non-Functional:
The program shall be implemented in Java.
The program must support multiple simultaneous clients connected to the server.
The system shall use a database to store and update customers, admins, trips, flights, bookings, passengers, seats, baggage, and prices.
The system must provide a graphical user interface (GUI).
The system shall maintain seat assignment consistency when multiple clients are connected.
The system shall provide clear validation/error messages for user-correctable errors.
The system shall enforce role-based authorization for admin-only actions.
Bookings linked by booking code and passenger last name shall be view-only unless the customer is the original booking owner.
The system shall follow the project scope: Europe-focused flights, reservations only, no real payment processing, and maximum one airport per city.

Use Cases:
Field
Description
Use Case
Create Customer Account
Summary
Anonymous viewer creates a customer account.
Actor
Anonymous Viewer
Precondition
The actor is not authenticated as a customer
Postcondition
A customer account exists and can be used for customer authentication
Base sequence
The Anonymous Viewer requests customer registration.
The System requests the required customer information.
The Anonymous Viewer provides first name, last name, email, and password.
The System validates the provided information. [ALT*1] [ALT*2] [ALT*3] [ALT*4] [ALT*5]
The System creates the customer account
The System informs the Anonymous Viewer that an account has been created successfully.
The account becomes available for customer authentication.
Use case ends.
Alternate sequence
[ALT*0] In all steps before the account is created, the use case can be cancelled. No account is created.
[ALT*1] If required information is missing, the System shows an error message requesting the missing information.
[ALT*2] If the email format is invalid, the System shows an error message requesting a valid email.
[ALT*3] If the email already belongs to an existing account, the System shows an error message indicating that the account already exists.
[ALT*4] If the first and last name contain numbers, the System shows an error message, requesting for the values to be string only.
[ALT*5] If the password is less than 8 characters, the System shows an error message, requesting the password to be more than 8 characters long.
Note
Admin accounts are not created through customer registration.
Requirements covered: FR 19, 20, 35 - NFR 6, 7


FR19
As an Anonymous Viewer, I want to create a customer account so that I can book and manage trips later.
FR20
As a Customer or Admin, I want to log in with role-based authentication so that protected actions are restricted to the correct user type.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR6
The system shall provide clear validation/error messages for user-correctable errors.
NFR7
The system shall enforce role-based authorization for admin-only actions.







Field
Description
Use Case
Search Trips
Summary
Anonymous Viewer or Customer searching for available trips
Actor
Anonymous Viewer, Customer
Precondition
Flights exist in the system
Postcondition
Matching available trips are shown to the actor.
Base sequence
The Anonymous Viewer/Customer provides origin and destination.
The Anonymous Viewer/Customer provides a departure date.
The Anonymous Viewer/Customer provides a trip type.
If the trip type is round trip, the actor provides a return date.
The actor provides number of passengers.
The System validates the search criteria.
The System finds matching trips.
The System excludes unavailable trips and flights without enough available seats.
The System shows matching trips with route, departure time, arrival time, duration, carrier, aircraft, stops, seat availability, and price.
The actor may order or limit the matching trips by preference.
Use case ends.
Alternate sequence
[ALT*0] Actor cancels search. Use case ends.
Note
A flight is one point-to-point route.
Customer can not select a date before current date.
Customers can not select a return date earlier than the current date.
If no flights match the table shows empty.
A flight is one point-to-point route.
A trip contains one or more flights.
A direct trip contains one flight.
A connected trip contains multiple flights.
A round trip contains one outbound trip and one return trip.

Requirements covered: FR 11, 12, 13, 14, 15, 16, 17, 18, 35 -  NFR 6
FR11
As an Anonymous Viewer or Customer, I want to view trip information including route, departure time, arrival time, duration, carrier, aircraft, stops, and price so that I can compare travel options.
FR12
As an Anonymous Viewer or Customer, I want to view trip information including route, departure time, arrival time, duration, carrier, aircraft, stops, and price so that I can compare travel options.
FR13
As a Customer, I want to see only available trips so that I can choose a suitable option for my trip and booking.
FR14
As a Customer, I want to include a return trip when needed so that I can book a round trip.
FR15
As a Customer, I want return dates earlier than departure dates to be rejected so that I can book only logical round-trip itineraries.
FR16
As a Customer, I want to be restricted from selecting dates prior to the current date, and prior to my departure date for round trips, so that I cannot search or book past trips.
FR17
As a Customer, I want unavailable flights or flights without available seats to be hidden or unavailable so that I only choose bookable options.
FR18
As a Customer, I want a trip to be composed of one or more flights so that direct, connected, and round-trip travel can be represented.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR6
The system shall provide clear validation/error messages for user-correctable errors.



Field
Description
Use Case
Book Trip
Summary
Customer reserves a selected trip for one or more passengers.
Actor
Customer
Precondition
The customer is authenticated, flights exist in the system, and at least one trip is available
Postcondition
A booking exists, passengers are connected to the booking, selected seats are reserved, the booking is linked to the Customer account, and the booking has a unique booking identifier.
Base sequence
The Customer searches for trips.
The Customer chooses an outbound trip.
If the booking is for a round trip, the Customer chooses a return trip.
The System shows the selected travel information. [ALT* 1]
The Customer provides passenger information for each traveler.
The Customer provides baggage choices for each passenger.
The Customer provides travel class choices for each passenger and flight.
The Customer provides seat choices for each passenger and flight.
The System calculates the total reservation price.
The System shows reservation details and total price.
The Customer approves the reservation.
The System validates the reservation information. [ALT*2] [ALT*3]
The System creates the booking.
The System creates a unique booking identifier.
The System gives Customers the unique booking identifier.
The System reserves the selected seats.
The System links the booking to the Customer account.
The booking becomes available for later viewing.
Use case ends
Alternate sequence
[ALT*0] In all steps before the booking is created, the use case can be cancelled. No booking is created and no seats are reserved.
[ALT*1] If the Customer is not authenticated, the System requires customer authentication before the booking can continue.
[ALT*2] If passenger details are  missing or invalid, the System shows an error message requesting valid passenger information.
[ALT*3] If seat information is missing for any passenger on any selected flight, the System shows an error message requesting information, not allowing confirmation.
Note
Economy class is preselected.
Customers can not choose unavailable seats.
Seat selection is mandatory.
The customer can not select unavailable flights.
A customer can book for multiple passengers.
Seats are reserved per passenger and per flight.
No real payment is processed.
Customers cannot edit a booking.

Requirements covered: FR1, 2,3, 4, 5, 6, 7, 8, 9, 10, 11, 14, 17, 18, 20, 22, 35 - NFR 3, 5, 6
FR1
As a Customer, I want to book a selected trip for one or more passengers so that seats are reserved for me and/or others.
FR2
As a Customer, I want to provide passenger information for each traveler so that every passenger is connected to the booking.
FR3
As a Customer, I want to choose baggage options for each passenger so that the booking reflects each passenger’s travel needs.
FR4
As a Customer, I want to choose travel class for each passenger and flight so that the reservation matches the selected comfort level.
FR5
As a Customer, I want to choose seats for each passenger and flight so that every passenger has an assigned seat.
FR6
As a Customer, I want seat selection to be mandatory before confirming a booking so that no passenger in the booking is left without a seat.
FR7
As a Customer, I want selected seats to become unavailable to others so that no two passengers reserve the same seat on the same flight.
FR8
As a Customer, I want to see a booking summary before confirming so that I can check the selected trip, trip type, passengers, seats, baggage, travel classes, and total price.
FR9
As a Customer, I want the total reservation price to be calculated from my booking choices so that I can review the full cost before confirming.
FR10
As a Customer, I want my booking to have a specific identifier so that the reservation can be accessed or identified later if needed.
FR14
As a Customer, I want to include a return trip when needed so that I can book a round trip.
FR17
As a Customer, I want unavailable flights or flights without available seats to be hidden or unavailable so that I only choose bookable options.
FR18
As a Customer, I want a trip to be composed of one or more flights so that direct, connected, and round-trip travel can be represented.
FR20
As a Customer or Admin, I want to log in with role-based authentication so that protected actions are restricted to the correct user type.
FR22
As a Customer, I want bookings created by me to be automatically linked to my account, so that I can easily check information of my owned bookings.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR3
The system shall use a database to store and update customers, admins, trips, flights, bookings, passengers, seats, baggage, and prices.
NFR5
The system shall maintain seat assignment consistency when multiple clients are connected.
NFR6
The system shall provide clear validation/error messages for user-correctable errors.





Field
Description
Use Case
Manage Own Bookings
Summary
Customer views linked bookings, links a booking by booking identifier and passenger last name, and cancels owned bookings.
Actor
Customer
Precondition
The Customer is authenticated, bookings exist.
Postcondition
The customer has viewed booking information, linked a view-only booking, or cancelled an owned booking.
Base sequence
The Customer requests their booking collection. [ALT*1]
The System shows bookings already linked to the Customer. [ALT*2]
The Customer may provide booking code and passenger last name to link a booking to the account.
The System validates the booking code and passenger last name. [ALT*3]
If a matching booking exists, the System links the booking to the Customer as view-only.
The Customer chooses a booking to review.
The System shows itinerary, passengers, seats, baggage, travel class, and price.
If the Customer is the booking creator/owner, the Customer may request cancellation.
The System cancels the booking.
The System releases the reserved seats.
The System informs the customer booking has been canceled.

Use case ends.
Alternate sequence
[ALT*0] In all steps, the use case can be cancelled, ending the use case.
[ALT*1] If an Anonymous Viewer requests booking management, the System requires customer authentication.
[ALT*2] If the booking code or passenger last name is missing, the System shows an error message requesting the missing information.
[ALT*3] If no booking matches both the booking code and one of the passenger last name, the System shows a message indicating that no matching booking was found.
Note

If the Customer has no linked bookings the section remains empty.
Bookings created by the Customer are automatically linked to the Customer account.
Bookings linked by booking identifier and passenger last name are view-only.
Customers cannot edit  bookings.
Requirements covered: FR 22, 23, 24, 25, 26, 35 - NFR 6, 8
FR22
As a Customer, I want bookings created by me to be automatically linked to my account, so that I can easily check information of my owned bookings.
FR23
As a Customer, I want to link a booking using booking code and passenger last name so that I can view a booking created by another customer.
FR24
As a Customer, I want linked bookings to be view-only unless I created the booking so that bookings cannot be changed by the wrong account.
FR25
As a Customer, I want to cancel a booking I created so that the reservation is no longer active.
FR26
As a Customer, I want cancelled booking seats to become available again so that other customers can reserve them.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR6
The system shall provide clear validation/error messages for user-correctable errors.
NFR8
Bookings linked by booking code and passenger last name shall be view-only unless the customer is the original booking owner.


Field
Description
Use Case
Add Flight
Summary
Adding a new point-to-point flight.
Actor
Admin
Precondition
The Admin is authenticated
Postcondition
A new flight exists and can be used as part of customer trips
Base sequence
The Admin requests to add a flight.
The System requests flight ID, carrier, aircraft, origin, destination, departure time, arrival time, economy price, and business price.
The Admin provides the required flight information.
The System applies predetermined seat capacity and layout.
The Admin confirms addition.
The System validates the flight information. [ALT*1] [ALT*2] [ALT*3] [ALT*4][ALT*5]
The System adds the flight.
The flight becomes available for trip search and booking.
Use case ends.
Alternate sequence
[ALT*0] In all steps before the flight is added, the use case can be cancelled. No flight is added.
[ALT*1] If required flight information is missing, the System shows an error message requesting the missing information and blocks confirmation.
[ALT*2] If the flight ID already exists, the System shows an error message requesting a unique flight ID.
[ALT*3] If departure or arrival time is invalid, the System shows an error message requesting valid time information.
[ALT*4] If price information is invalid, the System shows an error message requesting valid price information.
[ALT*5] If origin and destination are the same, the System shows an error message requesting different cities.
Note
A flight is one point-to-point route.
Seating capacity is predetermined.
Adding a flight is separate from managing existing flights because it does not require choosing an existing flight first.
Requirements covered: FR20, 21, 27, 28, 35 - NFR 6, 7
FR20
As a Customer or Admin, I want to log in with role-based authentication so that protected actions are restricted to the correct user type.
FR21
As an Admin, I want to be the only one with access to flight data management, while customers can only view trips, so that management duties are restricted according to user roles.
FR27
As an Admin, I want to add a flight by providing flight ID, carrier, aircraft, origin, destination, departure time, arrival time, economy price, and business price so that the flight has the required information to become available for customer trips.
FR28
As an Admin, I want each flight to be registered as one point-to-point route from one origin to one destination so that flight data stays consistent.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR6
The system shall provide clear validation/error messages for user-correctable errors.
NFR7
The system shall enforce role-based authorization for admin-only actions.


Field
Description
Use Case
Manage Flights
Summary
Admin inspects, edits, and removes existing flights.
Actor
Admin
Precondition
The Admin is authenticated and at least one flight exists
Postcondition
Flight information has been inspected, updated, or removed from availability.
Base sequence
The Admin requests flight management.
The System shows existing flights.
The Admin may provide criteria to find specific flights.
The Admin chooses an existing flight.
The System shows flight details and seat availability.
6. The Admin may edit flight information, remove the flight, or inspect seat availability.
   Scenario A: Edit flight (check limitation)
   A.1. The Admin provides updated flight information.
   A.2. The System validates the updated information. [ALT*1]
   A.3. The System updates the flight.
   Scenario B: Remove flight
   B.1. The Admin requests removal of the flight.
   B.2. The System verifies whether removal is allowed. [ALT*3]
   B.3. The System removes the flight from availability.
   Scenario C: Inspect seat availability
   C.1. The Admin requests seat availability for the selected flight.
   C.2. The System shows available and occupied seats for the flight.




Use case end
Alternate sequence
[ALT*0] In all steps before changes are applied, the use case can be cancelled.
[ALT*1] If updated flight information is missing or invalid, the System shows an error message requesting valid information.
[ALT*2] If the flight is used in active bookings, the System shows a message indicating that removal is not allowed.
Note
If no flight matches the provided criteria the table will be empty.
This use case manages existing flights.
Seat availability is inspected, not manually changed.
Flight data must stay consistent with bookings and reservations.
Requirements covered: FR 20, 21, 29, 30, 31, 32, 35 - NFR6, 7
FR20
As a Customer or Admin, I want to log in with role-based authentication so that protected actions are restricted to the correct user type.
FR21
As an Admin, I want to be the only one with access to flight data management, while customers can only view trips, so that management duties are restricted according to user roles.
FR29
As an Admin, I want to access flight information and seat details so that I can inspect full flight availability.
FR30
As an Admin, I want to edit an existing flight so that outdated or incorrect flight data can be corrected.
FR31
As an Admin, I want to remove a flight so that cancelled or invalid flights cannot be booked.
FR32
As an Admin, I want removal to be blocked when a flight is used in active bookings so that customer reservations are not impacted.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR6
The system shall provide clear validation/error messages for user-correctable errors.
NFR7
The system shall enforce role-based authorization for admin-only actions.


Field
Description
Use Case
View Bookings (Admin)
Summary
Admin views and finds customer bookings.
Actor
Admin
Precondition
The Admin is authenticated
Postcondition
Booking information has been shown to the Admin
Base sequence
The Admin requests to view active bookings
The System shows existing bookings.
The Admin may provide booking code or customer email to find specific bookings.
The Admin chooses a booking.
The System shows itinerary, passengers, seats, baggage, travel class, booking identifier and total price.
Use case ends.
Alternate sequence
[ALT*0] In all steps, the use case can be cancelled, ending the use case.
Note
If no booking matches criteria, table will appear empty.
Admin can inspect bookings but does not access customer bookings and does not access customer passwords.
Requirements covered:
FR20, 21, 33, 34, 35,- NFR 6, 7
FR20
As a Customer or Admin, I want to log in with role-based authentication so that protected actions are restricted to the correct user type.
FR21
As an Admin, I want to be the only one with access to flight data management, while customers can only view trips, so that management duties are restricted according to user roles.
FR33
As an Admin, I want to view all bookings and identify them by customer account and booking identifier so that I can monitor reservations.
FR34
As an Admin, I want to find bookings by booking code or customer information so that I can find specific reservations efficiently.
FR35
As a Customer or Admin, I want clear validation, error, and confirmation messages so that I understand whether an action succeeded or what must be corrected .
NFR6
The system shall provide clear validation/error messages for user-correctable errors.
NFR7
The system shall enforce role-based authorization for admin-only actions.


Use case to Requirement table:
Use Case
Requirements covered
Create Customer Account
FR19, FR20, FR35, NFR1, NFR4, NFR6, NFR7
Search Trips
FR11, FR12, FR13, FR14, FR15, FR16, FR17, FR18, FR35, NFR1, NFR2, NFR4, NFR6, NFR9
Book Trip
FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR8, FR9, FR10, FR14, FR17, FR18, FR20, FR22, FR35, NFR1, NFR2, NFR3, NFR4, NFR5, NFR6, NFR9
Manage Own Bookings
FR22, FR23, FR24, FR25, FR26, FR35, NFR1, NFR2, NFR4, NFR6, NFR8
Add Flight
FR20, FR21, FR27, FR28, FR35, NFR1, NFR2, NFR4, NFR6, NFR7, NFR9
Manage Flights
FR20, FR21, FR29, FR30, FR31, FR32, FR35, NFR1, NFR2, NFR4, NFR6, NFR7, NFR9
View Bookings (Admin)
FR20, FR21, FR33, FR34, FR35, NFR1, NFR2, NFR4, NFR6, NFR7



Use case diagram (draft):
Change to view bookings



Activity Diagrams:
Create Customer Account
Search Trips
Book Trip
Manage Own Bookings
Add Flight
Manage Flights
Manage Bookings

