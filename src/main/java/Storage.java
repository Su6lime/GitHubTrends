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
        StringBuilder sb = new StringBuilder(100);
            sb.append(System.currentTimeMillis());
            sb.append(" ");
            sb.append(event.id);
            sb.append(" ");
            sb.append(event.repo.id);
            sb.append(" ");
            sb.append(event.repo.name);
            sb.append(" ");
            sb.append(event.actor.id);
            sb.append("\n");
        try {
            fw.write(sb.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
