
import com.satori.rtm.*;
import com.satori.rtm.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyClass {

    static final String endpoint = "wss://open-data.api.satori.com";
    static final String appkey = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
    static final String channel = "github-events";
    static String path = "";
    static private DataAnalyser dataAnalyser = new DataAnalyser();

    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<>();
    static private BlockingQueue<AnyJson> listOfMessages = new LinkedBlockingQueue<>();
    static private Parser parser = new Parser();
    static private boolean isWorkEnded = false;
    static int j = 0;
    static private int delayinMin = 1;

    static private JSONObject jsonObject = new JSONObject();
    static private JSONArray array = new JSONArray();

    static final long startTime = System.currentTimeMillis();

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
            int i = 0;

            @Override
            public void onSubscriptionData(SubscriptionData data) {
                long currentTime = System.currentTimeMillis();


//                System.out.println("num of data packets = " + ++i);
                for (AnyJson json : data.getMessages()) {
//                    System.out.println("num of messages = " + ++j);
//                    j++;
                    try {
                        jsonMessages.put(json);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                if ((currentTime - startTime) > 1 * 60 * 1000) {
//                    isWorkEnded = true;
//                    client.shutdown();
//                    System.out.println("stops");
//                }
            }
        };

        client.createSubscription(channel, SubscriptionMode.SIMPLE, listener);

        client.start();

        parser.start();

        Scanner scanner = new Scanner(System.in);
        int startTime = scanner.nextInt();
        int stopTime = scanner.nextInt();
        Writer writer = new Writer(System.currentTimeMillis() - (long) (startTime * 60 * 1000), System.currentTimeMillis() - (long) (stopTime * 60 * 1000));
        writer.start();
    }

    static private class Parser extends Thread {

        public void run() {
            long t = System.currentTimeMillis();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(new File(path + System.currentTimeMillis()) + ".txt", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!(jsonMessages.isEmpty() && isWorkEnded)) {
                AnyJson json = jsonMessages.poll();
                if (json == null)
                    continue;

                GitHubEvent event = json.convertToType(GitHubEvent.class);
//                array.add(event.repo.id);
//                array.add(event.actor.id);
//                jsonObject.put(System.currentTimeMillis(), array);
                try {
//                    fileWriter.write(jsonObject.toString() + " ");
                    fileWriter.write(String.valueOf(System.currentTimeMillis()));
                    fileWriter.write(" ");
                    fileWriter.write(event.repo.id);
                    fileWriter.write(" ");
                    fileWriter.write(event.actor.id);
                    fileWriter.write("\n");
                    fileWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if ((System.currentTimeMillis() - t) > delayinMin * 60 * 1000)
                    try {
                        System.out.println(delayinMin + " min passed.");
                        fileWriter.close();
                        fileWriter = new FileWriter(new File(path + System.currentTimeMillis()) + ".txt");
                        t = System.currentTimeMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                array = new JSONArray();
//                jsonObject = new JSONObject();
//                dataAnalyser.addRepoID(event.repo.id);
//                dataAnalyser.addActorID(event.actor.id);
            }
//            System.out.println(dataAnalyser.getMostFrequentRepo());
//            System.out.println(dataAnalyser.getMostFrequentActor());
//            System.out.println("number of data received : " + j);
        }
    }

    static private class Writer extends Thread {

        private long start;
        private long end;
        private int firstIndex;
        private int lastIndex;

        public Writer(long mStart, long mEnd) {
            start = mStart;
            end = mEnd;
        }

        public void run() {
            File directory = new File(path);
            String[] filesInDir = directory.list();
            System.out.println(filesInDir.toString());
            for (int i = 0; i < filesInDir.length; i++)
                if (Long.parseLong(filesInDir[i]) >= start) {
                    firstIndex = i - 1;
                    break;
                }

            for (int i = filesInDir.length - 1; i >= 0; i--)
                if (Long.parseLong(filesInDir[i]) <= end) {
                    lastIndex = i;
                }

            for (int i = firstIndex; i <= lastIndex; i++) {
                if (i == firstIndex) {
                    processFirstFile(new File(path + filesInDir[i]), start);
                    continue;
                }
                if (i == lastIndex) {
                    processLastFile(new File(filesInDir[i]), start);
                    continue;
                }
                process(new File(path + filesInDir[i]));
            }
            System.out.println(dataAnalyser.getMostFrequentRepo());
            System.out.println(dataAnalyser.getMostFrequentActor());
        }
    }

    private static void process(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                String s[] = line.split(" ");
                dataAnalyser.addRepoID(s[1]);
                dataAnalyser.addActorID(s[2]);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFirstFile(File file, long time) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                String s[] = line.split(" ");
                if (s[0].equals(String.valueOf(time))) {
                    while ((line = br.readLine()) != null) {
                        String array[] = line.split(" ");
                        dataAnalyser.addRepoID(array[1]);
                        dataAnalyser.addActorID(array[2]);
                    }
                    break;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void processLastFile(File file, long time) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                String s[] = line.split(" ");
                dataAnalyser.addRepoID(s[1]);
                dataAnalyser.addActorID(s[2]);
                if (s[0].equals(String.valueOf(time)))
                    break;
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}