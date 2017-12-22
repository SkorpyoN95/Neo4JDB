import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.neo4j.udc.UsageDataKeys.Features.bolt;

public final class GraphDatabase {

    private final GraphDatabaseService graphDatabaseService;
    private GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );
    private static final String GRAPH_DIR_LOC = "./neo4j";
    public static enum RelTypes implements RelationshipType
    {
        KNOWS, WATCHED, IS_KIND_OF
    }
    public static enum NodeTypes implements Label
    {
        USER, ANIME, GENRE
    }


    public static GraphDatabase createDatabase() {
        return new GraphDatabase();
    }

    private GraphDatabase() {
        graphDatabaseService = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File(GRAPH_DIR_LOC))
                .setConfig( bolt.type, "BOLT" )
                .setConfig( bolt.enabled, "true" )
                .setConfig( bolt.address, "localhost:7687" )
                .newGraphDatabase();
        registerShutdownHook();
    }

    private void registerShutdownHook() {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(graphDatabaseService::shutdown));
    }

    public void config(){
        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            graphDatabaseService.schema()
                    .constraintFor( NodeTypes.USER)
                    .assertPropertyIsUnique( "nickname" )
                    .create();
            graphDatabaseService.schema()
                    .constraintFor( NodeTypes.ANIME)
                    .assertPropertyIsUnique( "title" )
                    .create();
            graphDatabaseService.schema()
                    .constraintFor( NodeTypes.GENRE)
                    .assertPropertyIsUnique( "name" )
                    .create();
            tx.success();
        }
    }

    public String runCypher(final String cypher) {
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            final Result result = graphDatabaseService.execute(cypher);
            transaction.success();
            return result.resultAsString();
        }
    }

    public void addUser(Map<String, Object> props){
        Map<String, Object> params = new HashMap<>();
        params.put("props", props);
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = "create (u:USER) set u = $props";
            graphDatabaseService.execute(query, params);
            transaction.success();
        }
    }

    public void addAnime(Map<String, Object> props){
        Map<String, Object> params = new HashMap<>();
        params.put("props", props);
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = "create (a:ANIME) set a = $props";
            graphDatabaseService.execute(query, params);
            transaction.success();
        }
    }

    public void addAnime(Map<String, Object> props, String[] genres){
        Map<String, Object> params = new HashMap<>();
        params.put("props", props);
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = "create (a:ANIME) set a = $props";
            graphDatabaseService.execute(query, params);
            String title = props.get("title").toString();
            for(String genre : genres){
                addIsKindOfRelationship(title, genre);
            }
            transaction.success();
        }
    }

    public void addGenre(Map<String, Object> props){
        Map<String, Object> params = new HashMap<>();
        params.put("props", props);
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = "create (g:GENRE) set g = $props";
            graphDatabaseService.execute(query, params);
            transaction.success();
        }
    }

    public void addKnowsRelationship(String user1, String user2){
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = String.format("match (u1:USER),(u2:USER) " +
                    "where u1.nickname='%s' and u2.nickname='%s' " +
                    "create (u1)-[r1:KNOWS]->(u2)", user1, user2);
            graphDatabaseService.execute(query);
            transaction.success();
        }
    }

    public void addWatchedRelationship(String user, String title){
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = String.format("match (u:USER),(a:ANIME) " +
                    "where u.nickname='%s' and a.title='%s' " +
                    "create (u)-[r:WATCHED]->(a)", user, title);
            graphDatabaseService.execute(query);
            transaction.success();
        }
    }

    public void addIsKindOfRelationship(String title, String name){
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            String query = String.format("match (a:ANIME),(g:GENRE) " +
                    "where a.title='%s' and g.name='%s' " +
                    "create (a)-[r:IS_KIND_OF]->(g)", title, name);
            graphDatabaseService.execute(query);
            transaction.success();
        }
    }

    public String allRelations(String key, NodeTypes type){
        String value;
        switch (type) {
            case USER: value = String.format("{nickname: '%s'}", key); break;
            case ANIME: value = String.format("{title: '%s'}", key); break;
            case GENRE: value = String.format("{name: '%s'}", key); break;
            default: value = ""; break;
        }
        return runCypher(String.format("match (n:%s %s)-[r]-(m) return n,r,m", type.toString(), value));
    }

    public String shortestPath(String user1, String user2){
        return runCypher(String.format("match p = shortestPath((u1:USER {nickname: '%s'})-[*]-(u2:USER {nickname: '%s'})) return p",
                user1, user2));
    }

    public void shutDown(){
        graphDatabaseService.shutdown();
    }
}