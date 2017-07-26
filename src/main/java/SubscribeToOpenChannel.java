/**
 * Created by Ali on 7/25/17.
 */

import com.satori.rtm.*;
import com.satori.rtm.model.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SubscribeToOpenChannel {

    static final String endpoint = "wss://open-data.api.satori.com";
    static final String appkey = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
    static final String channel = "github-events";

    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<AnyJson>();
    static private BlockingQueue<GitHubEvent> eventObjects = new LinkedBlockingQueue<GitHubEvent>();
    static private Analyser analyser = new Analyser();
    static private Parser parser = new Parser();
    static private boolean flag1 = false;
    static private boolean flag2 = false;

    public static void main(String[] args) throws InterruptedException {
        final RtmClient client = new RtmClientBuilder(endpoint, appkey)
                .setListener(new RtmClientAdapter() {
                    @Override
                    public void onEnterConnected(RtmClient client) {
                        System.out.println("Connected to Satori RTM!");
                    }
                })
                .build();

        final long startTime = System.currentTimeMillis();
        SubscriptionAdapter listener = new SubscriptionAdapter() {
            int i = 0;
            int j = 0;
            @Override
            public void onSubscriptionData(SubscriptionData data) {

                System.out.println("num of data packets = "+ ++i);
                for (AnyJson json : data.getMessages()) {
                    System.out.println("num of messages = "+ ++j);
                    try {
                        jsonMessages.put(json);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                long currentTime = System.currentTimeMillis();
                if(currentTime - startTime > 5*60*1000) {
                    flag1 = true;
                    client.shutdown();
                    System.out.println("stops");
                    parser.interrupt();
                }
            }
        };

        client.createSubscription(channel, SubscriptionMode.SIMPLE, listener);

        analyser.start();
        parser.start();
        client.start();
    }

    static private class Parser extends Thread {

        public void run(){
            while(!(flag1 && jsonMessages.isEmpty())){
                AnyJson json;
                try {
                    json = jsonMessages.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if(jsonMessages.isEmpty() && flag1)
                        break;
                    else
                        continue;
                }
                System.out.println("after take 1");
                GitHubEvent event = json.convertToType(GitHubEvent.class);
                try {
                    eventObjects.put(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            flag2 = true;
            analyser.interrupt();
        }
    }

    static private class Analyser extends Thread {

        public void run(){
            while(!(flag2 && eventObjects.isEmpty())){
                GitHubEvent event = null;
                try {
                    event = eventObjects.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if(eventObjects.isEmpty() && flag1)
                        break;
                    else
                        continue;
                }
                System.out.println("after take 2");
                Data.addRepoID(event.repo.id);
                Data.addActorID(event.actor.id);
            }
            System.out.println(Data.getMostFrequentRepo());
            System.out.println(Data.getMostFrequentActor());
        }
    }

}
