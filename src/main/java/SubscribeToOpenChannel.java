/**
 * Created by Ali on 7/25/17.
 */

import com.satori.rtm.*;
import com.satori.rtm.model.*;
import sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SubscribeToOpenChannel {


    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<AnyJson>();
    static private BlockingQueue<GitHubEvent> eventObjects = new LinkedBlockingQueue<GitHubEvent>();
    static private DataAnalyser dataAnalyser = new DataAnalyser();
//    static private Subscriber subscriber = new Subscriber();
//    static private Analyser analyser = new Analyser();
//    static private Parser parser = new Parser();

    public static void main(String[] args) throws InterruptedException {

        new Subscriber().start();
        new Parser().start();
        new Analyser().start();

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String[] data = scanner.nextLine().split(" ");

            if(data.length == 3) {
                Long startTime = Long.parseLong(data[1]);
                Long endTime = Long.parseLong(data[2]);
                new AnalyseThread(startTime, endTime, data[0]).start();
            } else {
                switch (data[0]) {
                    case "Hot":
                        System.out.println(dataAnalyser.getMostFrequentRepo());
                        new AnalyseThread(System.currentTimeMillis()-100000, System.currentTimeMillis()-10000, data[0]).start();
                        break;
                    case "Dev":
                        System.out.println(dataAnalyser.getMostFrequentActor());
                        break;
                }
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
                dataAnalyser.addRepoID(event.repo.id);
                dataAnalyser.addActorID(event.actor.id);
                Storage.storeInFile(event);
//                System.out.println("ID : " + event.id);
            }
        }
    }

}
