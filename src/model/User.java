package model;

import java.util.Objects;

public abstract class User
{
  private int userId;
  private String email;
  private String password;
  private boolean loggedIn;

  protected User(int userId, String email, String password)
  {
    setUserId(userId);
    setEmail(email);
    setPassword(password);
  }

  public boolean login(String email, String password)
  {
    loggedIn = Objects.equals(this.email, email)
        && Objects.equals(this.password, password);
    return loggedIn;
  }

  public void logout()
  {
    loggedIn = false;
  }

  public int getUserId()
  {
    return userId;
  }

  public void setUserId(int userId)
  {
    if (userId <= 0)
    {
      throw new IllegalArgumentException("User id must be positive.");
    }
    this.userId = userId;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    if (email == null || email.isBlank())
    {
      throw new IllegalArgumentException("Email is required.");
    }
    this.email = email;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    if (password == null || password.isBlank())
    {
      throw new IllegalArgumentException("Password is required.");
    }
    this.password = password;
  }

  public boolean isLoggedIn()
  {
    return loggedIn;
  }
}
