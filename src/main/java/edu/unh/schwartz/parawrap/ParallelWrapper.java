import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Scanner;

/**
 * Jacob Schwartz
 *
 * ParallelWrapper.java
 *
 */
public class ParallelWrapper
{
    // Number of threads to use for the processes
    private int numThreads;

    // The output writer
    private PrintWriter outputWriter;

    // The queue
    private PriorityBlockingQueue<String> chunks;

    /**
     *
     * @param manifestFileName - name of the file that will give us the program
     * information
     */
    public ParallelWrapper(int threads, String fileName) throws Exception
    {
        numThreads = threads;

        // Read in the input file and get the chunks
        Spliter spliter = new Spliter("$\n^");
        chunks = spliter.split(new File(fileName));

        // If there are no chunk
        if (chunks.size() == 0)
        {
            System.err.println("Incorrect chunk pattern or empty input file");
            System.exit(1);
        }

    }

    /**
     * Runs the executable on the number of specified input threads.
     */
    public void run() throws Exception
    {
        // Start the threads
        TaskThread[] threads = new TaskThread[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new TaskThread(i, chunks);
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++)
        {
            threads[i].join();
        }

        /*
        long kaks_time = 0;
        for (int i = 0; i < numThreads; i++)
        {
            kaks_time += threads[i].getRunTime();
        }
        System.out.println(kaks_time / 1000);
        */
    }

    /**
     * Merges the output together and deletes the temporary files
     */ 
    public void merge() throws Exception
    {
        // Delete the old files
        for (int i = 0; i < numThreads; i++)
        {
            // Remove temp file
            File temp = new File("/tmp/kaks" + i + "/temp.axt" );
            temp.delete();

            File folder = new File("/tmp/kaks" + i);
            for (File file : folder.listFiles())
            {
                System.out.println("Attempting to read " + file.getName());
                Scanner s = new Scanner(file);
                s.nextLine();
                outputWriter.println(s.nextLine());
                file.delete();
            }
            folder.delete();
        }
        outputWriter.close();
    }

    /**
     * Main method
     *
     * @param args - Command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        ParallelWrapper app = new ParallelWrapper(Integer.valueOf(args[0]),
            args[1]);
        app.run();
        app.merge();
    }
}
