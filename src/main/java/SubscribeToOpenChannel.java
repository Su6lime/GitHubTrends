/**
 * Created by Ali on 7/25/17.
 */

import com.satori.rtm.*;
import com.satori.rtm.model.*;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscribeToOpenChannel {

    static final String endpoint = "wss://open-data.api.satori.com";
    static final String appkey = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
    static final String channel = "github-events";
    static private BlockingQueue<AnyJson> messages = new LinkedBlockingQueue<AnyJson>();
    static private Analyser analyser = new Analyser();

    public static void main(String[] args) throws InterruptedException {
        final RtmClient client = new RtmClientBuilder(endpoint, appkey)
                .setListener(new RtmClientAdapter() {
                    @Override
                    public void onEnterConnected(RtmClient client) {
                        System.out.println("Connected to Satori RTM!");
                    }
                })
                .build();


        SubscriptionAdapter listener = new SubscriptionAdapter() {
            @Override
            public void onSubscriptionData(SubscriptionData data) {
                for (AnyJson json : data.getMessages()) {
                    try {
                        messages.put(json);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        client.createSubscription(channel, SubscriptionMode.SIMPLE, listener);

        analyser.start();
        client.start();
    }

    static private class Analyser extends Thread {

        public void run(){
            while(isAlive()){
                AnyJson json = null;
                try {
                    json = messages.poll(100, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                GitHubEvent event = json.convertToType(GitHubEvent.class);
                Data.addRepoID(event.repo.id);
            }
        }
    }

}
