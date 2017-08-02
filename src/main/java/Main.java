
import com.satori.rtm.*;
import com.satori.rtm.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * created By Amir-PHL
 */
public class Main {

    private static int timeMaintainer = 1;
    private static int lengthOfOutputList = 10;
    private static final String PATH = "DataBase/";

    static private BlockingQueue<AnyJson> jsonMessages = new LinkedBlockingQueue<>();
    static private boolean isWorkTerminated = false;
    static private DataAnalyser dataAnalyser = new DataAnalyser();

    private static JFrame frame = new JFrame();
    private static JFrame resultFrame;
    private static JTextField startTimeText = new JTextField("Enter <Start time(min)> then press enter.");
    private static JTextField terminateTimeText = new JTextField("Enter <Terminate time(min)> then press enter.");
    private static JTextArea timeCounterText = new JTextArea("Fill sections ,\nThen click on one of buttons.");
    private static JTextArea resultText = new JTextArea();
    private static JButton actorButton = new JButton("Show Actors");
    private static JButton repoButton = new JButton("Show Repos");
    private static JButton typeButton = new JButton("Show Types");

    private static String mode; // actor , repo , ...

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

        createGUI();

        while (true)
            try {
                waitForUserCommand();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Incorrect Input . Enter a valid positive decimal number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    /**
     * This method creates a graphical user interface to get input from user.
     */
    private static void createGUI() {
        startTimeText.addActionListener(new TextHandler());
        terminateTimeText.addActionListener(new TextHandler());
        startTimeText.addFocusListener(new MyFocusHandler());
        terminateTimeText.addFocusListener(new MyFocusHandler());
        actorButton.addActionListener(new ButtonHandler());
        repoButton.addActionListener(new ButtonHandler());
        typeButton.addActionListener(new ButtonHandler());
        frame.setLayout(new GridLayout(6, 2));
        frame.setSize(500, 300);
        frame.setLocation(new Point(100, 100));
        frame.setResizable(false);
        timeCounterText.setEditable(false);
        resultText.setEditable(false);
        timeCounterText.setEnabled(false);
        resultText.setEnabled(false);
        timeCounterText.setDisabledTextColor(Color.BLUE);
        resultText.setDisabledTextColor(Color.BLUE);
        timeCounterText.setBackground(Color.YELLOW);
        resultText.setBackground(Color.GREEN);
        Font font = new Font("serif", Font.BOLD, 16);
        startTimeText.setFont(font);
        terminateTimeText.setFont(font);
        resultText.setFont(font);
        timeCounterText.setFont(font);
        frame.add(startTimeText);
        frame.add(terminateTimeText);
        frame.add(actorButton);
        frame.add(repoButton);
        frame.add(typeButton);
        frame.add(timeCounterText);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
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
            File dir = new File("DataBase");
            dir.mkdir();
            long initialTime = System.currentTimeMillis();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(new File(PATH + initialTime), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!(jsonMessages.isEmpty() && isWorkTerminated)) {
                AnyJson json = jsonMessages.poll();
                if (json == null)
                    continue;

                GitHubEvent event = json.convertToType(GitHubEvent.class);
                try {
                    fileWriter.write(String.valueOf(System.currentTimeMillis()));
                    fileWriter.write(" ");
                    fileWriter.write(event.getActor().getId());
                    fileWriter.write(" ");
                    fileWriter.write(event.getRepo().getId());
                    fileWriter.write(" ");
                    fileWriter.write(event.getType());
                    fileWriter.write("\n");
                    fileWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if ((System.currentTimeMillis() - initialTime) > timeMaintainer * 60 * 1000) //1 * 60 *1000 is equals to 1 minute
                    try {
                        timeCounterText.setText(timeCounter + " minute passed.");
                        timeCounter++;
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

            System.out.println("Number of files in the directory : " + filesInDir.length);
            System.out.println("--------------------------------------------------------");

            dataAnalyser = new DataAnalyser();

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
//                if (i == firstIndex) {
//                    processSpecialFiles(new File(PATH + filesInDir[i]), startTime, "I am First File");
//                    continue;
//                }
                if (i == lastIndex) {
                    processSpecialFiles(new File(PATH + nameOfFiles[i]), startTime, "I am Last File");
                    continue;
                }
                processCommonFiles(new File(PATH + nameOfFiles[i]));
            }
            switch (mode) {
                case "actor":
                    resultText.setText(String.valueOf(dataAnalyser.getMostFrequentActor(lengthOfOutputList)));
                    break;
                case "repo":
                    resultText.setText(String.valueOf(dataAnalyser.getMostFrequentRepo(lengthOfOutputList)));
                    break;
                case "type":
                    resultText.setText(String.valueOf(dataAnalyser.getMostFrequentType(lengthOfOutputList)));
                    break;
            }
            resultText.setSize(600, 600);
            showInf();
        }
    }

    /**
     * This class shows results in GUI . JFrame .
     */
    private static void showInf() {
        if (resultFrame != null) {
            resultFrame.setVisible(false);
        }
        resultFrame = new JFrame();
        resultFrame.setLayout(new FlowLayout());
        resultFrame.setTitle("result");
        resultFrame.setSize(600, 600);
        resultFrame.setLocation(150, 150);
        resultFrame.add(resultText);
        resultFrame.setVisible(true);

        try {
            File dir = new File("Result2");
            dir.mkdir();
            FileWriter fw = new FileWriter(new File("Result2/myResult.txt"));
            fw.write(dataAnalyser.getMostFrequentActor(lengthOfOutputList));
            fw.write("\n");
            fw.write(dataAnalyser.getMostFrequentRepo(lengthOfOutputList));
            fw.write("\n");
            fw.write(dataAnalyser.getMostFrequentType(lengthOfOutputList));
            fw.write("\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(dataAnalyser.getMostFrequentActor(lengthOfOutputList));
//        System.out.println(dataAnalyser.getMostFrequentRepo(lengthOfOutputList));
//        System.out.println(dataAnalyser.getMostFrequentType(lengthOfOutputList));
    }

    private static class TextHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == startTimeText) {
                startTime = Double.parseDouble(startTimeText.getText());
            } else {
                terminateTime = Double.parseDouble(terminateTimeText.getText());
            }
        }
    }

    private static class MyFocusHandler implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {

        }

        @Override
        public void focusLost(FocusEvent e) {
            try {
                if (e.getSource() == startTimeText)
                    startTime = Double.parseDouble(startTimeText.getText());
                else
                    terminateTime = Double.parseDouble(terminateTimeText.getText());
            } catch (NumberFormatException e1) {

            }
        }
    }

    private static class ButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == actorButton)
                mode = "actor";
            else if (e.getSource() == repoButton)
                mode = "repo";
            else
                mode = "type";
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
     * then stores it in hashMap
     *
     * @param file file that we want to read and process
     */
    private static void processCommonFiles(File file) {
        System.out.println("Entering in the processCommonFiles method.");
        int i = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String s[] = line.split(" ");
                dataAnalyser.addRepoID(s[1]);
                dataAnalyser.addActorID(s[2]);
                dataAnalyser.addType(s[3]);
                i++;
            }
            System.out.println("i :" + i);
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
        String line;
        int i = 0;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (identifier.equals("I am First File"))
            try {
                while ((line = br.readLine()) != null) {
                    String s[] = line.split(" ");
                    if (time > Long.parseLong(s[0])) {
                        while ((line = br.readLine()) != null) {
                            String array[] = line.split(" ");
                            dataAnalyser.addRepoID(array[1]);
                            dataAnalyser.addActorID(array[2]);
                            dataAnalyser.addType(array[3]);
                            i++;
                        }
                        System.out.println("first i : " + i);
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
                Scanner scanner = new Scanner(new FileReader(file));
                while ((line = scanner.nextLine()) != null) {
                    String s[] = line.split(" ");
                    dataAnalyser.addRepoID(s[1]);
                    dataAnalyser.addActorID(s[2]);
                    dataAnalyser.addType(s[3]);
                    i++;
                    if (Long.parseLong(s[0]) > time)
                        break;
                }
                System.out.println("last i : " + i);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.err.println("Error in reading last file.");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error in reading last file.");
            } catch (NoSuchElementException e) {

            }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}