package horn_markov_logic_rules;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RunnableTask implements Runnable  {
    private String filename;
    private String content;

    public RunnableTask(String filename) {
        this.filename = filename;
        this.content = "";
        System.out.println("Create Task to get content from " + filename);
    }

    public String getFileName() {
        return filename;
    }

    public void run() {
        try {
            String currentDir = System.getProperty("user.dir");
            Path path = Paths.get(currentDir, "java-concurrency-readfiles", "files", filename);
            File file = path.toFile();

            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                content += line;
            }

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public String getContent() { return content; }

    public static void main(String[] args) {
        // create tasks
        List<RunnableTask> tasks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            RunnableTask task = new RunnableTask( (i + 1) + ".txt");
            Thread t = new Thread(task);
            t.start();
            tasks.add(task);
            threads.add(t);
        }

        // wait for threads to finish
        try {
            for (int i = 0; i < 5; i++) {
                threads.get(i).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // collect results
        String content = "";
        for (int i = 0; i< 5; i++) {
            content += tasks.get(i).getContent() + System.lineSeparator();
        }

        System.out.println("Result:");
        System.out.println(content);
    }
}
