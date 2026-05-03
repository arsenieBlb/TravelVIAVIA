package model;

public class PlaneType
{
  private int planeTypeId;
  private String typeName;
  private String model;
  private int numberOfColumns;
  private int numberOfEconomySeats;
  private int numberOfBusinessSeats;

  public PlaneType(int planeTypeId, String typeName, String model,
      int numberOfColumns, int numberOfEconomySeats, int numberOfBusinessSeats)
  {
    setPlaneTypeId(planeTypeId);
    setTypeName(typeName);
    setModel(model);
    setNumberOfColumns(numberOfColumns);
    setNumberOfEconomySeats(numberOfEconomySeats);
    setNumberOfBusinessSeats(numberOfBusinessSeats);
    if (getTotalCapacity() <= 0)
    {
      throw new IllegalArgumentException("Plane capacity must be positive.");
    }
  }

  public int getTotalCapacity()
  {
    return numberOfEconomySeats + numberOfBusinessSeats;
  }

  public int getPlaneTypeId()
  {
    return planeTypeId;
  }

  public void setPlaneTypeId(int planeTypeId)
  {
    if (planeTypeId <= 0)
    {
      throw new IllegalArgumentException("Plane type id must be positive.");
    }
    this.planeTypeId = planeTypeId;
  }

  public String getTypeName()
  {
    return typeName;
  }

  public void setTypeName(String typeName)
  {
    if (typeName == null || typeName.isBlank())
    {
      throw new IllegalArgumentException("Type name is required.");
    }
    this.typeName = typeName;
  }

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    if (model == null || model.isBlank())
    {
      throw new IllegalArgumentException("Model is required.");
    }
    this.model = model;
  }

  public int getNumberOfColumns()
  {
    return numberOfColumns;
  }

  public void setNumberOfColumns(int numberOfColumns)
  {
    if (numberOfColumns <= 0)
    {
      throw new IllegalArgumentException("Number of columns must be positive.");
    }
    this.numberOfColumns = numberOfColumns;
  }

  public int getNumberOfEconomySeats()
  {
    return numberOfEconomySeats;
  }

  public void setNumberOfEconomySeats(int numberOfEconomySeats)
  {
    if (numberOfEconomySeats < 0)
    {
      throw new IllegalArgumentException(
          "Number of economy seats cannot be negative.");
    }
    this.numberOfEconomySeats = numberOfEconomySeats;
  }

  public int getNumberOfBusinessSeats()
  {
    return numberOfBusinessSeats;
  }

  public void setNumberOfBusinessSeats(int numberOfBusinessSeats)
  {
    if (numberOfBusinessSeats < 0)
    {
      throw new IllegalArgumentException(
          "Number of business seats cannot be negative.");
    }
    this.numberOfBusinessSeats = numberOfBusinessSeats;
  }

  @Override public String toString()
  {
    return typeName + " " + model + " (" + getTotalCapacity() + " seats)";
  }
}
