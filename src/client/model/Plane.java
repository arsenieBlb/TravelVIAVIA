package client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Plane
{
    private int planeId;
    private String registrationNumber;
    private PlaneState status;
    private String state;
    private PlaneType planeType;
    private Carrier carrier;
    private final List<Seat> seats;

    public Plane(int planeId, String registrationNumber, PlaneState status,
                 PlaneType planeType, Carrier carrier)
    {
        this.seats = new ArrayList<>();
        setPlaneId(planeId);
        setRegistrationNumber(registrationNumber);
        this.status = Objects.requireNonNull(status, "Status is required.");
        setPlaneType(planeType);
        setCarrier(carrier);
    }

    public void activate()
    {
        status.activate(this);
    }

    public void sendToMaintenance()
    {
        status.sendToMaintenance(this);
    }

    public void decommission()
    {
        status.decommission(this);
    }

    protected void setInternalStatus(PlaneState status)
    {
        this.status = status;
    }

    public String getStatusName()
    {
        return status.getStatus();
    }

    public int getTotalSeats()
    {
        return seats.size();
    }

    public List<Seat> getSeatsByClass(SeatClass seatClass)
    {
        Objects.requireNonNull(seatClass, "Seat class is required.");
        List<Seat> matchingSeats = new ArrayList<>();
        for (Seat seat : seats)
        {
            if (seat.getSeatClass().getClass().equals(seatClass.getClass())) {
                matchingSeats.add(seat);
            }
        }
        return matchingSeats;
    }

    public void addSeat(Seat seat)
    {
        Objects.requireNonNull(seat, "Seat is required.");
        if (seats.contains(seat))
        {
            throw new IllegalArgumentException("Seat already exists on this plane.");
        }
        for (Seat existingSeat : seats)
        {
            if (existingSeat.getSeatNumber().equalsIgnoreCase(seat.getSeatNumber()))
            {
                throw new IllegalArgumentException(
                        "Seat number already exists on this plane.");
            }
        }
        validateSeatCapacity(seat.getSeatClass());
        seats.add(seat);
    }

    private void validateSeatCapacity(SeatClass seatClass)
    {
        int currentSeats = getSeatsByClass(seatClass).size();

        if (seatClass instanceof BusinessClass)
        {
            if (currentSeats >= planeType.getNumberOfBusinessSeats())
            {
                throw new IllegalArgumentException("Business seat capacity reached.");
            }
        }
        else if (seatClass instanceof EconomyClass)
        {
            if (currentSeats >= planeType.getNumberOfEconomySeats())
            {
                throw new IllegalArgumentException("Economy seat capacity reached.");
            }
        }
    }

    public int getPlaneId()
    {
        return planeId;
    }

    public void setPlaneId(int planeId)
    {
        if (planeId <= 0)
        {
            throw new IllegalArgumentException("Plane id must be positive.");
        }
        this.planeId = planeId;
    }

    public String getRegistrationNumber()
    {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber)
    {
        if (registrationNumber == null || registrationNumber.isBlank())
        {
            throw new IllegalArgumentException("Registration number is required.");
        }
        this.registrationNumber = registrationNumber;
    }

    public PlaneState getStatus()
    {
        return status;
    }

    public void setStatus(PlaneState status)
    {
        this.status = Objects.requireNonNull(status, "Plane status is required.");
    }

    public PlaneType getPlaneType()
    {
        return planeType;
    }

    public void setPlaneType(PlaneType planeType)
    {
        this.planeType = Objects.requireNonNull(planeType,
                "Plane type is required.");
    }

    public Carrier getCarrier()
    {
        return carrier;
    }

    public void setCarrier(Carrier carrier)
    {
        if (this.carrier == carrier)
        {
            return;
        }
        if (this.carrier != null)
        {
            this.carrier.removePlane(this);
        }
        this.carrier = Objects.requireNonNull(carrier, "Carrier is required.");
        this.carrier.addPlane(this);
    }

    public List<Seat> getSeats()
    {
        return Collections.unmodifiableList(seats);
    }

    @Override
    public String toString()
    {
        return registrationNumber + " [" + getStatusName() + "] (" + planeType.getTypeName() + ")";
    }
}

