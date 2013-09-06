import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;

public class TaskThread extends Thread
{
    private PriorityBlockingQueue<String> queue;
    private int num;
    private long runTime;

    public TaskThread(int i, PriorityBlockingQueue<String> q)
    {
        queue = q;
        num = i;
        runTime = 0;
    }

    public void run()
    {
        // Make output file for this thread
        String outDir = "/tmp/kaks" + num;
        File dir = new File(outDir);
        dir.mkdirs();

        int i = 0;
        while (queue.size() != 0)
        {
            String s = queue.poll();

            // Make temporary files
            String inName = outDir + "/temp.axt";
            File in = new File(inName);
            String outName = outDir + "/result" + i + ".kaks";
            File out = new File(outName);
            try
            {
                in.createNewFile();
                
                PrintWriter temp = new PrintWriter(in);
                temp.println(s);
                temp.close();

                out.createNewFile();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }

 
            try
            {
                System.out.println("Starting chunk on thread " + num);
                List<String> commands = new ArrayList<String>();
                commands.add("./KaKs_Calculator");
                commands.add("-i");
                commands.add(inName);
                commands.add("-o");
                commands.add(outName);
                System.out.println();
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.redirectErrorStream(true);

                long start = System.currentTimeMillis();
                final Process proc = pb.start();
                proc.waitFor();
                long end = System.currentTimeMillis();
                runTime += (end - start);
                System.out.println("Finished chunk on thread " + num);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                System.err.println("Exec error");
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            i++;
        }
    }

    public long getRunTime()
    {
        return runTime;
    }
}
