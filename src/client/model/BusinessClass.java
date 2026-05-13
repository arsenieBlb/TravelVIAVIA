package client.model;

public class BusinessClass implements SeatClass
{
    @Override
    public String getClassName()
    {
        return "Business";
    }

    @Override
    public double getPriceMultiplier()
    {
        return 2.5;
    }

    @Override
    public String toString()
    {
        return getClassName();
    }
}


