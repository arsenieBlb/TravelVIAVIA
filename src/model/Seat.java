package model;

import java.util.Objects;

public class Seat
{
  private int seatId;
  private String seatNumber;
  private int rowNumber;
  private SeatClass seatClass;

  public Seat(int seatId, String seatNumber, int rowNumber, SeatClass seatClass)
  {
    setSeatId(seatId);
    setSeatNumber(seatNumber);
    setRowNumber(rowNumber);
    setSeatClass(seatClass);
  }

  public int getSeatId()
  {
    return seatId;
  }

  public void setSeatId(int seatId)
  {
    if (seatId <= 0)
    {
      throw new IllegalArgumentException("Seat id must be positive.");
    }
    this.seatId = seatId;
  }

  public String getSeatNumber()
  {
    return seatNumber;
  }

  public void setSeatNumber(String seatNumber)
  {
    if (seatNumber == null || seatNumber.isBlank())
    {
      throw new IllegalArgumentException("Seat number is required.");
    }
    this.seatNumber = seatNumber;
  }

  public int getRowNumber()
  {
    return rowNumber;
  }

  public void setRowNumber(int rowNumber)
  {
    if (rowNumber <= 0)
    {
      throw new IllegalArgumentException("Row number must be positive.");
    }
    this.rowNumber = rowNumber;
  }

  public SeatClass getSeatClass()
  {
    return seatClass;
  }

  public void setSeatClass(SeatClass seatClass)
  {
    this.seatClass = Objects.requireNonNull(seatClass,
        "Seat class is required.");
  }

  @Override public boolean equals(Object object)
  {
    if (this == object)
    {
      return true;
    }
    if (!(object instanceof Seat seat))
    {
      return false;
    }
    return seatId == seat.seatId;
  }

  @Override public int hashCode()
  {
    return Objects.hash(seatId);
  }

  @Override public String toString()
  {
    return seatNumber + " (" + seatClass + ")";
  }
}
