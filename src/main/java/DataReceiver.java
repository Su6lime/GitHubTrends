/**
 * Created by Ali on 7/25/17.
 */

import com.satori.rtm.*;
import com.satori.rtm.model.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataReceiver {


    private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<AnyJson>();
    private BlockingQueue<GitHubEvent> eventObjects = new LinkedBlockingQueue<GitHubEvent>();
    private DataAnalyser dataAnalyser;

    public DataReceiver(DataAnalyser dataAnalyser) {
        this.dataAnalyser = dataAnalyser;
    }

    public void start() {
        new Subscriber().start();
        new Parser().start();
        new Analyser().start();
    }

    private class Subscriber extends Thread {

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

    private class Parser extends Thread {

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

    private class Analyser extends Thread {

        public void run(){
            while(isAlive()){
                GitHubEvent event = null;
                try {
                    event = eventObjects.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("after take 2");
                dataAnalyser.addType(event.getType());
                dataAnalyser.addRepoID(event.getRepo().getId());
                dataAnalyser.addActorID(event.getActor().getId());
                Storage.storeInFile(event);
//                System.out.println("ID : " + event.id);
            }
        }
    }

}
