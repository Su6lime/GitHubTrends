import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by Ali on 8/2/17.
 */
public class UI extends Thread{

    public static void main(String[] args) throws InterruptedException {
        DataAnalyser dataAnalyser = new DataAnalyser();
        UI ui = new UI(dataAnalyser);
        new DataReceiver(dataAnalyser).start();
        ui.start();
    }

    private DataAnalyser dataAnalyser;

    public UI(DataAnalyser dataAnalyser) {
        this.dataAnalyser = dataAnalyser;
    }
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            try {
                runQuery(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("wrong format!\n" + "type HELP for more information");
//                    e.printStackTrace();
            }
        }
    }

    private void runQuery(String query) throws ParseException, NumberFormatException {
        String[] data = query.split(" ");
        Long startTime;
        Long endTime;
        int numOfResult;

        if (data.length == 1) {
            switch (data[0]) {
                case "Type":
                    System.out.println(dataAnalyser.getMostFrequentType(5));
                    break;
                case "Hot":
                    System.out.println(dataAnalyser.getMostFrequentRepo(5));
                    break;
                case "Dev":
                    System.out.println(dataAnalyser.getMostFrequentActor(5));
                    break;
                case "HELP":
                    System.out.println("Under maintenance");
                    break;
                default:
                    throw new ParseException("", 1);
            }
        } else if (data.length == 4) {
            numOfResult = Integer.parseInt(data[1]);
            startTime = Long.parseLong(data[2]);
            endTime = Long.parseLong(data[3]);
            new AnalyseThread(startTime, endTime, data[0], numOfResult).start();
        } else {
            numOfResult = Integer.parseInt(data[1]);
            endTime = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss zzz");
            String time1 = query.substring(query.indexOf(" ", 1 + query.indexOf(" "))+ 1);
            if (time1.contains(",")) {
                endTime = sdf.parse(time1.substring(1 + time1.indexOf(','))).getTime();
                time1 = time1.substring(0, time1.indexOf(','));
            }
            startTime = sdf.parse(time1).getTime();
            new AnalyseThread(startTime, endTime, data[0], numOfResult).start();
        }
    }
}