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
    private int numOfResult;
    private Date startDate;
    private Date endDate;

    public AnalyseThread(long startTime, long endTime, String operation, int numOfResults) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.opearation = operation;
        this.numOfResult = numOfResults;
        startDate = new Date(startTime);
        endDate = new Date(endTime);
        dataAnalyser = new DataAnalyser();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
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
                dataAnalyser.addType(data[2]);
                dataAnalyser.addRepoID(data[3]);
                dataAnalyser.addActorID(data[5]);
            }
        }
        String result = "from " + startDate + " to " + endDate + "\n";
        switch (opearation) {
            case "Type":
                result += dataAnalyser.getMostFrequentType(numOfResult);
                break;
            case "Hot":
                result += dataAnalyser.getMostFrequentRepo(numOfResult);
                break;
            case "Dev":
                result += dataAnalyser.getMostFrequentActor(numOfResult);
                break;
        }
        storeResults(result);
        System.out.println("Done!");
    }

    private void storeResults(String result) {
        try {

            FileWriter fw = new FileWriter("Results/Query result for " + opearation + " from " + startDate + " to " + endDate + ".txt");
            fw.write(result);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
