package model;

public class LuggageType
{
  private int luggageTypeId;
  private String name;
  private String description;
  private double extraPrice;

  public LuggageType(int luggageTypeId, String name, String description, double extraPrice)
  {
    setLuggageTypeId(luggageTypeId);
    setName(name);
    setDescription(description);
    setExtraPrice(extraPrice);
  }

  public int getLuggageTypeId()
  {
    return luggageTypeId;
  }

  public void setLuggageTypeId(int luggageTypeId)
  {
    if (luggageTypeId <= 0)
    {
      throw new IllegalArgumentException("Luggage type id must be positive.");
    }
    this.luggageTypeId = luggageTypeId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    if (name == null || name.isBlank())
    {
      throw new IllegalArgumentException("Luggage type name is required.");
    }
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    if (description == null || description.isBlank())
    {
      throw new IllegalArgumentException(
          "Luggage type description is required.");
    }
    this.description = description;
  }


  public double getExtraPrice()
  {
    return extraPrice;
  }

  public void setExtraPrice(double extraPrice)
  {
    if (extraPrice < 0)
    {
      throw new IllegalArgumentException("Extra price cannot be negative.");
    }
    this.extraPrice = extraPrice;
  }

  @Override public String toString()
  {
    return name + extraPrice;
  }
}
