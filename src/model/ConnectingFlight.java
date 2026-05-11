package model;

import java.time.Duration;

public class ConnectingFlight extends Flight {

    private Flight firstSegment;
    private Flight secondSegment;

    // constructor for connecting flights that uses the two separate flights
    public ConnectingFlight(Flight firstSegment, Flight secondSegment) {
        super(
            // we combine the ids so it has a unique id
            firstSegment.getFlightId() * 1000 + secondSegment.getFlightId(),
            // we combine the flight numbers
            firstSegment.getFlightNumber() + "+" + secondSegment.getFlightNumber(),
            firstSegment.getDepartureTime(),
            secondSegment.getArrivalTime(),
            // we add both prices to get the total price
            firstSegment.getBasePrice() + secondSegment.getBasePrice(),
            firstSegment.getCarrier(), 
            firstSegment.getPlane(), 
            firstSegment.getDepartureCity(),
            secondSegment.getArrivalCity()
        );
        this.firstSegment = firstSegment;
        this.secondSegment = secondSegment;
    }

    public Flight getFirstSegment() {
        return firstSegment;
    }

    public Flight getSecondSegment() {
        return secondSegment;
    }
}
