
import com.satori.rtm.*;
import com.satori.rtm.model.*;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * created By Amir-PHL
 */
public class Main {

    private static final String PATH = "DataBase/";

    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<>();
    static private boolean isWorkEnded = false;
    static private DataAnalyser dataAnalyser = new DataAnalyser();

    private static JFrame frame = new JFrame();
    private static JTextArea textArea = new JTextArea("Fill sections , then click on button");
    private static JTextField startText = new JTextField("Start time");
    private static JTextField terminateText = new JTextField("Terminate time");
    private static JTextArea resultText = new JTextArea();
    private static JButton button = new JButton("show result");

    private static double startTime;
    private static double terminateTime;

    private static int timeCounter = 1;

    /**
     * This method reads JSon files from server of GitHub , then stores them in a Queue.
     * This queue is a thread safe queue.
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        final String ENDPOINT = "wss://open-data.api.satori.com";
        final String APPKEY = "783ecdCcb8c5f9E66A56cBFeeeB672C3";
        final String CHANNEL = "github-events";
        final RtmClient client = new RtmClientBuilder(ENDPOINT, APPKEY)
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
                        jsonMessages.put(json);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        client.createSubscription(CHANNEL, SubscriptionMode.SIMPLE, listener);

        client.start();

        new Writer().start();

        startText.addActionListener(new TextHandler());
        terminateText.addActionListener(new TextHandler());
        button.addActionListener(new ButtonHandler());
        frame.setLayout(new FlowLayout());
        frame.setSize(new Dimension(600, 300));
        frame.setLocation(new Point(500, 500));
        frame.setResizable(false);
        frame.setTitle("Processor");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        textArea.setEditable(false);
        resultText.setEditable(false);
        resultText.setDisabledTextColor(Color.BLUE);
        frame.add(startText);
        frame.add(terminateText);
        frame.add(button);
        frame.add(textArea);
        frame.add(resultText);
        frame.setVisible(true);

        while (true)
            try {
                waitForUserCommand();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Incorrect Input . Enter a valid positive decimal number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    /**
     * This method waits for user to enter the startTime time and stop time.
     * startTime time : Time that user wants to startTime processing the json Files.
     * stop time : Time that user wants to terminate processing the json Files.
     *
     * @return returns nothing
     * @throws NoSuchElementException
     * @throws IllegalStateException
     */
    private static void waitForUserCommand() throws NoSuchElementException, IllegalStateException {
        Scanner scanner = new Scanner(System.in);
        double startTime = scanner.nextDouble();
        double stopTime = scanner.nextDouble();
        Reader reader = new Reader(System.currentTimeMillis() - (long) (startTime * 60 * 1000), System.currentTimeMillis() - (long) (stopTime * 60 * 1000));
        reader.start();
    }

    /**
     * This class gets useful information from any Json object that is in list ,
     * then stores them in some files
     * The path that information stores in it , each 1 minute changes.
     */
    private static class Writer extends Thread {

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
                        textArea.setText(timeCounter + " minute passed.");
                        timeCounter++;
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

    /**
     * This class reads and process files and get information from it ,
     * then fills the hashMap.
     */
    private static class Reader extends Thread {

        private long startTime;
        private long terminateTime;
        private int firstIndex;
        private int lastIndex;

        public Reader(long mStart, long mEnd) {
            startTime = mStart;
            terminateTime = mEnd;
        }

        public void run() {
            File directory = new File(PATH);
            String[] filesInDir = directory.list();
            long[] nameOfFiles = new long[filesInDir.length];

            for (int i = 0; i < filesInDir.length; i++) {
                nameOfFiles[i] = Long.parseLong(filesInDir[i]);
            }

            quickSort(nameOfFiles, 0, filesInDir.length - 1);

            try {
                for (int i = 0; i < nameOfFiles.length; i++) {
                    if (nameOfFiles[i] >= startTime) {
                        firstIndex = i - 1;
                        break;
                    }
                }
                for (int i = nameOfFiles.length - 1; i >= 0; i--) {
                    if (nameOfFiles[i] <= terminateTime) {
                        lastIndex = i;
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Problem in processing the files.\nPlease enter inputs again.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("First index :" + firstIndex + "     " + "Last index :" + lastIndex);
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
            resultText.setText(dataAnalyser.getMostFrequentActor(10) + "\n" + dataAnalyser.getMostFrequentRepo(10));
            frame.repaint();
            System.out.println(dataAnalyser.getMostFrequentActor(10));
            System.out.println(dataAnalyser.getMostFrequentRepo(10));
//            JOptionPane.showMessageDialog(null, dataAnalyser.getMostFrequentRepo(), "Output", JOptionPane.INFORMATION_MESSAGE);
//            JOptionPane.showMessageDialog(null, dataAnalyser.getMostFrequentActor(), "Output", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static class TextHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == startText) {
                startTime = Double.parseDouble(startText.getText());
            } else {
                terminateTime = Double.parseDouble(terminateText.getText());
            }
        }
    }

    private static class ButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Reader reader = new Reader(System.currentTimeMillis() - (long) (startTime * 60 * 1000), System.currentTimeMillis() - (long) (terminateTime * 60 * 1000));
                reader.start();
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "Incorrect Input . Enter a valid positive decimal number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * This method being used for Quick Sort.
     *
     * @param arr   array that we want to sort.
     * @param left  first index of array to start to sort.
     * @param right last index of array to terminate sort.
     * @return
     */
    private static int partition(long arr[], int left, int right) {
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

    /**
     * This method reads files and gets information from it ,
     * then store it in hashMap
     *
     * @param file
     */
    private static void processCommonFiles(File file) {
        System.out.println("Entering in the processCommonFiles method.");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
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

    /**
     * This method works like processCommonFiles method
     *
     * @param file       file that we want to process
     * @param time       time that need to
     * @param identifier
     */
    private static void processSpecialFiles(File file, long time, String identifier) {
        System.err.println("Entering in the processSpecialFiles method.");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        if (identifier.equals("I am First File"))
            try {
                while ((line = br.readLine()) != null) {
                    String s[] = line.split(" ");
                    if (time > Long.parseLong(s[0])) {
                        System.out.println("First file added to process.");
                        while ((line = br.readLine()) != null) {
                            String array[] = line.split(" ");
                            dataAnalyser.addRepoID(array[1]);
                            dataAnalyser.addActorID(array[2]);
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
                copyFileUsingApacheCommonsIO(file, new File("copyOfLastFile"));
                br = new BufferedReader(new FileReader(new File("copyOfLastFile")));
                while ((line = br.readLine()) != null) {
                    String s[] = line.split(" ");
                    dataAnalyser.addRepoID(s[1]);
                    dataAnalyser.addActorID(s[2]);
                    if (Long.parseLong(s[0]) > time)
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

    private static void copyFileUsingApacheCommonsIO(File source, File dest) throws IOException {
        FileUtils.copyFile(source, dest);
    }
}