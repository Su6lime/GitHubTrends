import java.io.*;

/**
 * Created by Ali on 7/30/17.
 */
public class Storage {

    private static FileWriter fw;

    static {
        try {
            fw = new FileWriter("Data.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storeInFile(GitHubEvent event) {
        String st = "";
        st = st + (System.currentTimeMillis());
        st = st + (" ");
        st = st + (event.id);
        st = st + (" ");
        st = st + (event.repo.id);
        st = st + (" ");
        st = st + (event.repo.name);
        st = st + (" ");
        st = st + (event.actor.id);
        st = st + "\n";

        try {
            fw.write(st);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static int j = 10000000;

    public static void main(String[] args) {
//        GitHubEvent event = new GitHubEvent();
//        event.id = "2132432";
//        event.repo = event.new Repo();
//        event.actor = event.new Actor();
//        event.repo.id = "2132432";
//        event.repo.name = "name";
//        event.actor.id = "2132432";
//        try {
//            fw = new FileWriter("Logs1.txt", false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        long t1 = System.currentTimeMillis();
//        test1(event);
//        long t2 = System.currentTimeMillis();
//        try {
//            fw = new FileWriter("Logs2.txt", false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        long t3 = System.currentTimeMillis();
////        test2(event);
//        long t4 = System.currentTimeMillis();
//
//        System.out.println("test1 : " + (t2-t1) + " test2 : " + (t4-t3));
        System.out.println(System.currentTimeMillis());
    }

    public static void test1(GitHubEvent event) {
        for (int i = 0; i < j; i++) {
            String st = "";
            st = st + (event.id);
            st = st + (" ");
            st = st + (event.repo.id);
            st = st + (" ");
            st = st + (event.repo.name);
            st = st + "\n";

            try {
                fw.write(st);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void test2(GitHubEvent event) {
        for (int i = 0; i < j; i++) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(event.id);
            sb.append(" ");
            sb.append(event.repo.id);
            sb.append(" ");
            sb.append(event.repo.name);
            sb.append("\n");
//            pw.println(event.id + " " + event.repo.id + " " + event.repo.name + " " + event.actor.id);
            try {
                fw.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
