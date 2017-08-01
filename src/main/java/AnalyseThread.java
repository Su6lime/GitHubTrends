import java.io.*;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Created by Ali on 7/31/17.
 */
public class AnalyseThread extends Thread {

    private Long startTime, endTime;
    private String opearation;
    private DataAnalyser dataAnalyser;

    public AnalyseThread(long startTime, long endTime, String operation) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.opearation = operation;
        dataAnalyser = new DataAnalyser();
    }

    @Override
    public void run() {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("Data.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(fileReader);
        while (scanner.hasNext()) {
            String[] data = scanner.nextLine().split(" ");
            Long timeStamp = Long.parseLong(data[0]);
            if(timeStamp >= startTime && timeStamp < endTime) {
                dataAnalyser.addRepoID(data[2]);
                dataAnalyser.addActorID(data[4]);
            }
        }
        String result = "from " + startTime + " to " + endTime + "\n";
        switch (opearation) {
            case "Hot":
                result += dataAnalyser.getMostFrequentRepo();
                break;
            case "Dev":
                result += dataAnalyser.getMostFrequentActor();
                break;
        }
        storeResults(result);
    }

    private void storeResults(String result) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);
        try {
            FileWriter fw = new FileWriter("Query result for " + opearation + " from " + startDate + " to " + endDate + ".txt");
            fw.write(result);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
