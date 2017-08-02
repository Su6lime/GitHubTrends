import java.io.*;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Created by Ali on 7/31/17.
 */
public class AnalyseThread extends Thread {

    private Long startTime, endTime;
    private String operation;
    private DataAnalyser dataAnalyser;
    private int numOfResult;
    private Date startDate;
    private Date endDate;

    public AnalyseThread(long startTime, long endTime, String operation, int numOfResults) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.operation = operation;
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
            System.out.println("fail to reach Data");
            return;
        }

        Scanner scanner = new Scanner(fileReader);
        int badInput = 0;
        while (scanner.hasNext()) {
            String[] data = scanner.nextLine().split(" ");
            if(data.length != 6) {
                badInput ++;
                continue;
            }
            Long timeStamp;
            try {
                timeStamp = Long.parseLong(data[0]);
            } catch (NumberFormatException e) {
                badInput ++;
                continue;
            }
            if(timeStamp >= startTime && timeStamp < endTime) {
                dataAnalyser.addType(data[2]);
                dataAnalyser.addRepoID(data[3]);
                dataAnalyser.addActorID(data[5]);
            }
        }
        String result = "from " + startDate + " to " + endDate + "\tBad Inputs = " + badInput + "\n\n";
        switch (operation) {
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
        FileWriter fw = null;
        try {
            File dir = new File("Results");
            dir.mkdir();

            fw = new FileWriter("Results/Query result for " + operation
                    + " from " + startDate + " to " + endDate + ".txt");
            fw.write(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("fail to write in file");
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
