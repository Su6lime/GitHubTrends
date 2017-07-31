import com.sun.xml.internal.bind.v2.TODO;

/**
 * Created by Ali on 7/25/17.
 */
public class GitHubEvent {

    String id;
    String type;

//    TODO get public attribute
//    @JsonProperty("public")
//    String isPublic;

    //TODO get CreatedAt and translate it to miliseconds


    Actor actor;
    Repo repo;

    public class Actor {
        String id;

    }
    public class Repo {
        String id;
        String name;

    }
}
