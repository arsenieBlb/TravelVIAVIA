import server.database.DatabaseLoader;
public class TestDB {
    public static void main(String[] args) {
        try {
            DatabaseLoader loader = new DatabaseLoader();
            loader.loadAll();
            System.out.println("Success loading database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


