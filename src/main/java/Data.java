import com.sun.org.apache.bcel.internal.generic.NEW;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Ali on 7/25/17.
 */
public class Data {

    static private HashMap<String, Integer> repoFrequency = new HashMap();
    static private HashMap<String, Integer> actorFrequency = new HashMap();

    synchronized public static void addRepoID(String ID) {
        Integer oldValue = repoFrequency.get(ID);
        if(oldValue != null)
            repoFrequency.put(ID, 1 + oldValue);
        else
            repoFrequency.put(ID, 1);
    }

    synchronized protected static String getMostFrequentRepo() {

        int max = 0;
        String mID = "";
        for (String ID: repoFrequency.keySet())
            if(repoFrequency.get(ID) >= max) {
                max = repoFrequency.get(ID);
                mID = ID;
            }
        return "RepoID : " + mID + " frequency = " + max+ " num of Repositories = " + repoFrequency.size();
    }

    synchronized public static void addActorID(String ID) {
        Integer oldValue = actorFrequency.get(ID);
        if(oldValue != null)
            actorFrequency.put(ID, 1 + oldValue);
        else
            actorFrequency.put(ID, 1);
    }

    synchronized public static String getMostFrequentActor() {
        int max = 0;
        String mID = "";
        for (String ID: actorFrequency.keySet())
            if(actorFrequency.get(ID) >= max) {
                max = actorFrequency.get(ID);
                mID = ID;
            }
        return "ActorID : " + mID + " frequency = " + max + " num of Actors = " + actorFrequency.size();
    }

}
