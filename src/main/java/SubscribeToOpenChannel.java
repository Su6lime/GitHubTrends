/**
 * Created by Ali on 7/25/17.
 */

import com.satori.rtm.*;
import com.satori.rtm.model.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class SubscribeToOpenChannel {

    static final String endpoint = "wss://open-data.api.satori.com";
    static final String appkey = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
    static final String channel = "github-events";

    static private Queue<AnyJson> messages = new LinkedList<AnyJson>();
    static private Analyser analyser = new Analyser();
    static private boolean flag = true;

    public static void main(String[] args) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        System.out.println(startTime);
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
                        messages.add(json);
                        long currentTime = System.currentTimeMillis();
                        if(currentTime - startTime > 1*60*1000) {
                            flag = false;
                            break;
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
            while(!messages.isEmpty() || flag){
                AnyJson json = messages.poll();
                if(json == null)
                    continue;
                GitHubEvent event = json.convertToType(GitHubEvent.class);
                Data.addRepoID(event.repo.id);
            }
            System.out.println(Data.getMost());
        }
    }

}
