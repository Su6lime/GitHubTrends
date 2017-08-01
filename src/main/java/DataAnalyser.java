import java.util.*;

/**
 * Created by Ali on 7/25/17.
 */
public class DataAnalyser {

    private static HashMap<String, Integer> repoFrequency = new HashMap();
    private HashMap<String, Integer> actorFrequency = new HashMap();

    synchronized public void addRepoID(String ID) {
        Integer oldValue = repoFrequency.get(ID);
        if (oldValue != null)
            repoFrequency.put(ID, 1 + oldValue);
        else
            repoFrequency.put(ID, 1);
    }

    synchronized public StringBuilder getMostFrequentRepo(int num) {

        StringBuilder result = new StringBuilder();
        String keys[] = repoFrequency.keySet().toArray(new String[0]);
        int values[] = new int[keys.length];

        copyValuesFromHashMapToArray(keys, values , "Repo");
        quickSort(keys, values, 0, values.length - 1);

        int counter = 1;
        result.append("Num of Actors : " + repoFrequency.size() + "\n");
        while (num > 0) {
            result.append(counter);
            result.append("     ActorId : " + keys[keys.length - counter]);
            result.append("     Frequency : " + values[values.length - counter]);
            result.append("\n");
            counter++;
            num--;
        }
        return result;
    }

    synchronized public void addActorID(String ID) {
        Integer oldValue = actorFrequency.get(ID);
        if (oldValue != null)
            actorFrequency.put(ID, 1 + oldValue);
        else
            actorFrequency.put(ID, 1);
    }

    synchronized public StringBuilder getMostFrequentActor(int num) {
        StringBuilder result = new StringBuilder();
        String keys[] = actorFrequency.keySet().toArray(new String[0]);
        int values[] = new int[keys.length];

        copyValuesFromHashMapToArray(keys, values , "Actor");
        quickSort(keys, values, 0, values.length - 1);

        int counter = 1;
        result.append("Num of Repositories : " + actorFrequency.size() + "\n");
        while (num > 0) {
            result.append(counter);
            result.append("     RepoId : " + keys[keys.length - counter]);
            result.append("     Frequency : " + values[values.length - counter]);
            result.append("\n");
            counter++;
            num--;
        }
        return result;
    }

    private void copyValuesFromHashMapToArray(String[] keys, int[] values, String msg) {
        int counter = 0;
        if (msg.equals("Actor"))
            for (String repoid : keys) {
                values[counter] = actorFrequency.get(repoid);
                counter++;
            }
        else
            for (String repoid : keys) {
                values[counter] = repoFrequency.get(repoid);
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
}
