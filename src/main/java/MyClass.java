
import com.satori.rtm.*;
import com.satori.rtm.model.*;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyClass {

    private static final String ENDPOINT = "wss://open-data.api.satori.com";
    private static final String APPKEY = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
    private static final String CHANNEL = "github-events";
    private static final String PATH = "/home/amirphl/Desktop/JSonFile/";

    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<>();
    static private BlockingQueue<AnyJson> listOfMessages = new LinkedBlockingQueue<>();
    static private boolean isWorkEnded = false;

    public static void main(String[] args) throws InterruptedException {
        final RtmClient client = new RtmClientBuilder(ENDPOINT, APPKEY)
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
                for (AnyJson json : data.getMessages()) {
                    try {
                        jsonMessages.put(json);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        client.createSubscription(CHANNEL, SubscriptionMode.SIMPLE, listener);

        client.start();

        new Parser().start();

        while (true)
            waitForUserCommand();
    }

    /**
     * This method waits for user to enter the startTime time and stop time.
     * startTime time : Time that user wants to startTime processing the json Files.
     * stop time : Time that user wants to terminate processing the json Files.
     *
     * @return returns nothing
     */
    private static void waitForUserCommand() {
        Scanner scanner = new Scanner(System.in);
        double startTime;
        double stopTime;
        try {
            startTime = scanner.nextDouble();
            stopTime = scanner.nextDouble();
        } catch (InputMismatchException e) {
            JOptionPane.showMessageDialog(null, "Incorrect Input . Enter a valid positive decimal number.", "Error", JOptionPane.ERROR_MESSAGE);
            waitForUserCommand();
            return;
        } catch (NoSuchElementException e) {
            JOptionPane.showMessageDialog(null, "Incorrect Input . Enter a valid positive decimal number.", "Error", JOptionPane.ERROR_MESSAGE);
            waitForUserCommand();
            return;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, "Incorrect Input . Enter a valid positive decimal number.", "Error", JOptionPane.ERROR_MESSAGE);
            waitForUserCommand();
            return;
        }
        Writer writer = new Writer(System.currentTimeMillis() - (long) (startTime * 60 * 1000), System.currentTimeMillis() - (long) (stopTime * 60 * 1000));
        writer.start();
    }

    /**
     * This class gets useful information from any Json object that is in list ,
     * then store them in some files
     * The path that information stores in it , each 1 minute changes.
     */
    private static class Parser extends Thread {

        public void run() {
            long initialTime = System.currentTimeMillis();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(new File(PATH + initialTime), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!(jsonMessages.isEmpty() && isWorkEnded)) {
                AnyJson json = jsonMessages.poll();
                if (json == null)
                    continue;

                GitHubEvent event = json.convertToType(GitHubEvent.class);
                try {
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

                if ((System.currentTimeMillis() - initialTime) > 1 * 60 * 1000) //1 * 60 *1000 is equals to 1 minute
                    try {
                        System.out.println("1 minute passed.");
                        fileWriter.close();
                        fileWriter = new FileWriter(new File(PATH + System.currentTimeMillis()));
                        initialTime = System.currentTimeMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private static class Writer extends Thread {

        private long startTime;
        private long terminateTime;
        private int firstIndex;
        private int lastIndex;

        public Writer(long mStart, long mEnd) {
            startTime = mStart;
            terminateTime = mEnd;
        }

        public void run() {
            File directory = new File(PATH);
            String[] filesInDir = directory.list();
            long[] longNums = new long[filesInDir.length];

            for (int i = 0; i < filesInDir.length; i++) {
                longNums[i] = Long.parseLong(filesInDir[i]);
            }

            quickSort(longNums, 0, filesInDir.length - 1);

            try {
                for (int i = 0; i < longNums.length; i++) {
                    if (longNums[i] >= startTime) {
                        firstIndex = i - 1;
                        break;
                    }
                }
                for (int i = longNums.length - 1; i >= 0; i--) {
                    System.out.println(longNums[i]);
                    if (longNums[i] <= terminateTime) {
                        lastIndex = i;
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(null, "There is no information in data base in time you want.", "Error", JOptionPane.ERROR_MESSAGE);
                waitForUserCommand();
                return;
            }
            System.out.println("first index :" + firstIndex + " " + "last index:" + lastIndex);
            for (int i = firstIndex; i <= lastIndex; i++) {
                if (i == firstIndex) {
                    processSpecialFiles(new File(PATH + filesInDir[i]), startTime, "I am First File");
                    continue;
                }
                if (i == lastIndex) {
                    processSpecialFiles(new File(PATH + filesInDir[i]), startTime, "I am Last File");
                    continue;
                }
                processCommonFiles(new File(PATH + filesInDir[i]));
            }
            JOptionPane.showMessageDialog(null, Data.getMostFrequentRepo(), "Output", JOptionPane.INFORMATION_MESSAGE);
            JOptionPane.showMessageDialog(null, Data.getMostFrequentActor(), "Output", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static int partition(long arr[], int left, int right) {
        int i = left, j = right;
        long tmp;

        long pivot = arr[(left + right) / 2];

        while (i <= j) {
            while (arr[i] < pivot)
                i++;

            while (arr[j] > pivot)
                j--;

            if (i <= j) {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }
        return i;
    }


    private static void quickSort(long arr[], int left, int right) {

        int index = partition(arr, left, right);

        if (left < index - 1)

            quickSort(arr, left, index - 1);

        if (index < right)

            quickSort(arr, index, right);

    }

    private static void processCommonFiles(File file) {
        System.err.println("I entered in processCommonFiles method");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String s[] = line.split(" ");
                Data.addRepoID(s[1]);
                Data.addActorID(s[2]);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processSpecialFiles(File file, long time, String identifier) {
        System.err.println("I entered in processSpecialFiles");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        if (identifier.equals("I am First File"))
            try {
                line = br.readLine();
                System.out.println(line);
                while ((line = br.readLine()) != null) {
                    String s[] = line.split(" ");
                    if (time > Long.parseLong(s[0])) {
                        System.err.println("First file stated to process.");
                        while ((line = br.readLine()) != null) {
                            String array[] = line.split(" ");
                            Data.addRepoID(array[1]);
                            Data.addActorID(array[2]);
                        }
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            try {
                while ((line = br.readLine()) != null) {
                    String s[] = line.split(" ");
                    Data.addRepoID(s[1]);
                    Data.addActorID(s[2]);
                    if (Long.parseLong(s[0]) > time) ;
                    break;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}