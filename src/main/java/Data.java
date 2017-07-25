import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.HashMap;

/**
 * Created by Ali on 7/25/17.
 */
public class Data {

    static private HashMap<String, Integer> repoFrequency = new HashMap();

    public static void addRepoID(String ID) {
        if(repoFrequency.containsKey(ID))
            repoFrequency.put(ID, 1 + repoFrequency.get(ID));
        else
            repoFrequency.put(ID, 1);
    }

    public static String getMost() {
        int max = 1;
        String mID = "";
        for (String ID: repoFrequency.keySet())
            if(repoFrequency.get(ID) > max) {
                max = repoFrequency.get(ID);
                mID = ID;
            }
        return "RepoID : " + mID + " frequency = " + max;
    }

}
