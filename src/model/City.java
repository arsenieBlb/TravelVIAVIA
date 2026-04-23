package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class City
{
  private int cityId;
  private String cityName;
  private String country;

  public City(int cityId, String cityName, String country)
  {
    setCityId(cityId);
    setCityName(cityName);
    setCountry(country);
  }

  public LocalDateTime getLocalTime()
  {
    return LocalDateTime.now();
  }

  public int getCityId()
  {
    return cityId;
  }

  public void setCityId(int cityId)
  {
    if (cityId <= 0)
    {
      throw new IllegalArgumentException("City id must be positive.");
    }
    this.cityId = cityId;
  }

  public String getCityName()
  {
    return cityName;
  }

  public void setCityName(String cityName)
  {
    if (cityName == null || cityName.isBlank())
    {
      throw new IllegalArgumentException("City name is required.");
    }
    this.cityName = cityName;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    if (country == null || country.isBlank())
    {
      throw new IllegalArgumentException("Country is required.");
    }
    this.country = country;
  }

  @Override public boolean equals(Object object)
  {
    if (this == object)
    {
      return true;
    }
    if (!(object instanceof City city))
    {
      return false;
    }
    return cityId == city.cityId;
  }

  @Override public int hashCode()
  {
    return Objects.hash(cityId);
  }

  @Override public String toString()
  {
    return cityName + ", " + country;
  }
}
