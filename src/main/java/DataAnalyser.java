import java.util.*;

/**
 * Created by Ali on 7/25/17.
 */
public class DataAnalyser {

    private HashMap<String, Integer> repoFrequency = new HashMap();
    private HashMap<String, Integer> actorFrequency = new HashMap();
    private HashMap<String, Integer> typeFrequency = new HashMap();

    synchronized public void addRepoID(String ID) {
        Integer oldValue = repoFrequency.get(ID);
        if (oldValue != null)
            repoFrequency.put(ID, 1 + oldValue);
        else
            repoFrequency.put(ID, 1);
    }

    synchronized public void addActorID(String ID) {
        Integer oldValue = actorFrequency.get(ID);
        if (oldValue != null)
            actorFrequency.put(ID, 1 + oldValue);
        else
            actorFrequency.put(ID, 1);
    }

    synchronized public void addType(String type) {
        Integer oldValue = typeFrequency.get(type);
        if (oldValue != null)
            typeFrequency.put(type, 1 + oldValue);
        else
            typeFrequency.put(type, 1);
    }

    synchronized public String getMostFrequentRepo(int num) {

        String keys[] = repoFrequency.keySet().toArray(new String[0]);
        int values[] = new int[keys.length];

        copyValuesFromHashMapToArray(keys, values , repoFrequency);
        quickSort(keys, values, 0, values.length - 1);


        return "Num of Repositories : " + repoFrequency.size() + "\n" + printResult(keys, values, num, "RepoID");
    }

    synchronized public String getMostFrequentActor(int num) {
        String keys[] = actorFrequency.keySet().toArray(new String[0]);
        int values[] = new int[keys.length];

        copyValuesFromHashMapToArray(keys, values , actorFrequency);
        quickSort(keys, values, 0, values.length - 1);

        return "Num of Actors : " + actorFrequency.size() + "\n" + printResult(keys, values, num, "ActorID");
    }

    synchronized public String getMostFrequentType(int num) {
        String keys[] = typeFrequency.keySet().toArray(new String[0]);
        int values[] = new int[keys.length];

        copyValuesFromHashMapToArray(keys, values , typeFrequency);
        quickSort(keys, values, 0, values.length - 1);

        return "Num of Actors : " + typeFrequency.size() + "\n" + printResult(keys, values, num, "Type");
    }

    private void copyValuesFromHashMapToArray(String[] keys, int[] values, HashMap<String, Integer> map) {
        int counter = 0;
        for (String repoid : keys) {
            values[counter] = map.get(repoid);
            counter++;
        }
    }

    private int partition(String[] keys, int[] values, int left, int right) {
        int i = left, j = right;
        int tmp;
        String repoId;

        int pivot = values[(left + right) / 2];

        while (i <= j) {
            while (values[i] < pivot)
                i++;

            while (values[j] > pivot)
                j--;

            if (i <= j) {
                tmp = values[i];
                values[i] = values[j];
                values[j] = tmp;

                repoId = keys[i];
                keys[i] = keys[j];
                keys[j] = repoId;

                i++;
                j--;
            }
        }
        return i;
    }

    private void quickSort(String keys[], int values[], int left, int right) {

        int index = partition(keys, values, left, right);

        if (left < index - 1)

            quickSort(keys, values, left, index - 1);

        if (index < right)

            quickSort(keys, values, index, right);
    }

    private String printResult(String[] keys, int[] values, int num, String oparation){
        StringBuilder result = new StringBuilder();

        if(keys.length < num)
            num = keys.length;

        for(int i=1; i <= num; i++) {
            result
                    .append(i)
                    .append("     " + oparation +" : " + keys[keys.length - i])
                    .append("     Frequency : " + values[values.length - i])
                    .append("\n");
        }

        return result.toString();
    }
}
