import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Timer;

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
        System.out.println("from " + startTime + " to " + endTime);
        switch (opearation) {
            case "Hot":
                System.out.println(dataAnalyser.getMostFrequentRepo());
                break;
            case "Dev":
                System.out.println(dataAnalyser.getMostFrequentActor());
                break;
        }
    }
}
