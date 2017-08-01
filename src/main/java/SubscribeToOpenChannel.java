/**
 * Created by Ali on 7/25/17.
 */

import com.satori.rtm.*;
import com.satori.rtm.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SubscribeToOpenChannel {


    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<AnyJson>();
    static private BlockingQueue<GitHubEvent> eventObjects = new LinkedBlockingQueue<GitHubEvent>();
    static private DataAnalyser dataAnalyser = new DataAnalyser();

    public static void main(String[] args) throws InterruptedException {

        new Subscriber().start();
        new Parser().start();
        new Analyser().start();
        new UI().start();

    }

    static private class UI extends Thread {

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

        private static void runQuery(String query) throws ParseException, NumberFormatException {
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
//                        new AnalyseThread(System.currentTimeMillis()-100000, System.currentTimeMillis()-10000, data[0]).start();
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

    static private class Subscriber extends Thread {

        static final String endpoint = "wss://open-data.api.satori.com";
        static final String appkey = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
        static final String channel = "github-events";

        public void run() {
            final RtmClient client = new RtmClientBuilder(endpoint, appkey)
                    .setListener(new RtmClientAdapter() {
                        @Override
                        public void onEnterConnected(RtmClient client) {
                            System.out.println("Connected to Satori RTM!");
                        }
                    })
                    .build();

            SubscriptionAdapter listener = new SubscriptionAdapter() {
                int i = 0;
                int j = 0;

                @Override
                public void onSubscriptionData(SubscriptionData data) {

//                    System.out.println("num of data packets = " + ++i);
                    for (AnyJson json : data.getMessages()) {
//                        System.out.println("num of messages = " + ++j);
                        try {
                            jsonMessages.put(json);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            client.createSubscription(channel, SubscriptionMode.SIMPLE, listener);
            client.start();
        }
    }

    static private class Parser extends Thread {

        public void run(){
            while(isAlive()){
                AnyJson json = null;
                try {
                    json = jsonMessages.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("after take 1");
                GitHubEvent event = json.convertToType(GitHubEvent.class);
                try {
                    eventObjects.put(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static private class Analyser extends Thread {

        public void run(){
            while(isAlive()){
                GitHubEvent event = null;
                try {
                    event = eventObjects.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("after take 2");
                dataAnalyser.addType(event.type);
                dataAnalyser.addRepoID(event.repo.id);
                dataAnalyser.addActorID(event.actor.id);
                Storage.storeInFile(event);
//                System.out.println("ID : " + event.id);
            }
        }
    }

}
