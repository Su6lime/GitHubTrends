import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.HashMap;

/**
 * Created by Ali on 7/25/17.
 */
public class Data {

    static private HashMap<String, Integer> repoFrequency = new HashMap();
    static private HashMap<String, Integer> actorFrequency = new HashMap();

    public static void addRepoID(String ID) {
        if(repoFrequency.containsKey(ID))
            repoFrequency.put(ID, 1 + repoFrequency.get(ID));
        else
            repoFrequency.put(ID, 1);
    }

    public static String getMostFrequentRepo() {
        int max = 0;
        String mID = "";
        for (String ID: repoFrequency.keySet())
            if(repoFrequency.get(ID) >= max) {
                max = repoFrequency.get(ID);
                mID = ID;
            }
        return "RepoID : " + mID + " frequency = " + max;
    }

    public static void addActorID(String ID) {
        if(actorFrequency.containsKey(ID))
            actorFrequency.put(ID, 1 + actorFrequency.get(ID));
        else
            actorFrequency.put(ID, 1);
    }

    public static String getMostFrequentActor() {
        int max = 0;
        String mID = "";
        for (String ID: actorFrequency.keySet())
            if(actorFrequency.get(ID) >= max) {
                max = actorFrequency.get(ID);
                mID = ID;
            }
        return "ActorID : " + mID + " frequency = " + max;
    }

}
