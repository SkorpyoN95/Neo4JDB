import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        GraphDatabase db = GraphDatabase.createDatabase();
        //DataPopulator.populateGenreList(db);
        //DataPopulator.populateAnimeList(db);
        DataPopulator.populateUsers(db);
        System.out.println("Done");
        while(true);
        /*Map<String, Object> params = new HashMap<>();
        params.put("nickname", "SomeFancyNick");
        params.put("email", "abc@d.e");
        db.addUser(params);*/
    }
}
