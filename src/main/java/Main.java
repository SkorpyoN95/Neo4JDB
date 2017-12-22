import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        GraphDatabase db = GraphDatabase.createDatabase();
        //DataPopulator.populateGenreList(db);
        //DataPopulator.populateAnimeList(db);
        //DataPopulator.populateUsers(db);
        System.out.println(db.allRelations("Petit", GraphDatabase.NodeTypes.USER));
        System.out.println(db.shortestPath("Petit", "Fish"));
    }
}
