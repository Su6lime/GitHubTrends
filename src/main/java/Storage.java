import java.io.*;

/**
 * Created by Ali on 7/30/17.
 */
public class Storage {

    synchronized public static void storeInFile(GitHubEvent event) {
        FileWriter fw = null;
        try {
            fw = new FileWriter("Data.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        StringBuilder sb = new StringBuilder(100);
            sb.append(System.currentTimeMillis());
            sb.append(" ");
            sb.append(event.getId());
            sb.append(" ");
            sb.append(event.getType());
            sb.append(" ");
            sb.append(event.getRepo().getId());
            sb.append(" ");
            sb.append(event.getRepo().getName());
            sb.append(" ");
            sb.append(event.getActor().getId());
            sb.append("\n");
        try {
            fw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
