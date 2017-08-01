import java.util.*;

/**
 * Created by Ali on 7/25/17.
 */
public class DataAnalyser {


    private HashMap<String, Integer> repoFrequency = new HashMap();
    private HashMap<String, Integer> actorFrequency = new HashMap();

    synchronized public void addRepoID(String ID) {
        Integer oldValue = repoFrequency.get(ID);
        if(oldValue != null)
            repoFrequency.put(ID, 1 + oldValue);
        else
            repoFrequency.put(ID, 1);
    }

    synchronized protected String getMostFrequentRepo() {

        int max = 0;
        String mID = "";
        for (String ID: repoFrequency.keySet())
            if(repoFrequency.get(ID) >= max) {
                max = repoFrequency.get(ID);
                mID = ID;
            }
        return "RepoID : " + mID + " frequency = " + max+ " num of Repositories = " + repoFrequency.size();
    }

    synchronized public void addActorID(String ID) {
        Integer oldValue = actorFrequency.get(ID);
        if(oldValue != null)
            actorFrequency.put(ID, 1 + oldValue);
        else
            actorFrequency.put(ID, 1);
    }

    synchronized public String getMostFrequentActor() {
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
