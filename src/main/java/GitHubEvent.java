/**
 * Created by Ali on 7/25/17.
 */
public class GitHubEvent {

    private String id;
    private String type;

//    TODO get public attribute
//    @JsonProperty("public")
//    String isPublic;

    //TODO get CreatedAt and translate it to miliseconds


    private Actor actor;
    private Repo repo;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Actor getActor() {
        return actor;
    }

    public Repo getRepo() {
        return repo;
    }

    public class Actor {
        private String id;

        public String getId() {
            return id;
        }
    }
    public class Repo {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
