import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataPopulator {
    static private Map<String, String[]> animeList = new HashMap<>();
    static{
        animeList.put("Death Note", new String[] {"Mystery", "Police", "Psychological", "Supernatural", "Thriller"});
        animeList.put("Shaman King", new String[] {"Action", "Adventure", "Comedy", "Supernatural", "Drama", "Shounen"});
        animeList.put("Dragon Ball Z", new String[] {"Action", "Adventure", "Comedy", "Fantasy", "Martial Arts", "Shounen", "Super Power"});
        animeList.put("Shingeki no Kiyojin", new String[] {"Action", "Military", "Mystery", "Super Power", "Drama", "Fantasy", "Shounen"});
        animeList.put("Sword Art Online", new String[] {"Action", "Adventure", "Fantasy", "Game", "Romance"});
    }
    static private String[] genreList = {"Action", "Adventure", "Comedy", "Drama", "Mystery", "Police", "Psychological",
            "Supernatural", "Thriller", "Shounen", "Fantasy", "Martial Arts", "Romance", "Game", "Military"};

    static private String[] users = {"Jumbo", "Gibby", "Cobra", "Skin", "Pipi", "Ace", "Loco", "Hooks", "Growl", "Petit",
            "Fish", "Genie", "Bunny", "Viper", "Slayer", "Jewel", "Slick", "Butch", "Frosty", "Stout"};

    static public void populateGenreList(GraphDatabase db){
        for(String genre : genreList){
            Map<String, Object> params = new HashMap<>();
            params.put("name", genre);
            db.addGenre(params);
        }
    }

    static public void populateAnimeList(GraphDatabase db){
        for(String anime : animeList.keySet()){
            Map<String, Object> params = new HashMap<>();
            params.put("title", anime);
            db.addAnime(params, animeList.get(anime));
        }
    }

    static public void populateUsers(GraphDatabase db){
        for(String user : users){
            Map<String, Object> params = new HashMap<>();
            params.put("nickname", user);
            db.addUser(params);
        }

        Random generator = new Random();
        List<String> keys = new ArrayList(animeList.keySet());
        for(int i = 0; i < 20; ++i){
            List<Integer> range = IntStream.range(0, 20).boxed().collect(Collectors.toList());
            range.remove(i);
            for(int j = 0; j < 3; ++j){
                db.addKnowsRelationship(users[i], users[range.remove(generator.nextInt(range.size()))]);
            }
            db.addWatchedRelationship(users[i], keys.get(generator.nextInt(5)));
        }
    }
}
