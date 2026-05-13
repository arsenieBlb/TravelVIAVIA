package client.model;

public class EconomyClass implements SeatClass
{
    @Override
    public String getClassName()
    {
        return "Economy";
    }

    @Override
    public double getPriceMultiplier()
    {
        return 1.0;
    }

    @Override
    public String toString()
    {
        return getClassName();
    }
}


