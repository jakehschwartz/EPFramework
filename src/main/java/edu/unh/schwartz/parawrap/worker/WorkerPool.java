package edu.unh.schwartz.parawrap.worker;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Runs the workers.
 */
public final class WorkerPool
{
    /**
     * The workers to be utilizated.
     */
    private Worker[] workers;

    private List<Chunk> chunks;

    /**
     * Constructs the workers.
     *
     * @param num - the number of workers
     * @param chunks - the work for the workers
     */
    public WorkerPool(final int num, final PriorityBlockingQueue<File> chunks) 
    {
        // Initialize the threads
        workers = new Worker[num];
        for (int i = 0; i < num; i++)
        {
            workers[i] = new Worker(i, chunks);
        }

        this.chunks = chunks;
    }

    /**
     * Start the workers.
     */
    public void start()
    {
        for (int i = 0; i < workers.length; i++)
        {
            workers[i].start();
        }

        try
        {
            for (int i = 0; i < workers.length; i++)
            {
                workers[i].join();
            }
        }
        catch (InterruptedException e)
        {
            System.err.println("Thread interrupted");
            System.exit(1);
        }
    }

    /**
     * Print the statistics for the workers.
     */
    public void printStats()
    {
        // Open up the file to save to
        // TODO: print to correct directory
        PrintWriter statsOut = new PrintWriter("stats.csv");

        // Print the stats
        statsOut.println("Thread #,Runtime,Chunks Run,Avg Time Per Chunk");
        final String comma = ",";
        for (int i = 0; i < workers.length; i++)
        {
            long runtime = workers[i].getRunTime();
            int chunks = workers[i].getChunksRun();
            StringBuilder sb = new StringBuilder();
            sb.append(i).append(comma).append(runtime).append(comma);
            sb.append(chunks).append(comma).append(runtime / chunks);

            statsOut.println(sb.toString());
        }
        statsOut.println();

        // Chunk info
        statsOut.println("Chunk #,Runtime,Chunks Run,Avg Time Per Chunk");
        for (int i = 0; i < this.chunks.size(); i++)
        {
            Chunk c = this.chunks.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append(c.hashCode()).append(comma).append(c.length());
            sb.append(comma).append(c.getRuntime());
            statsOut.println(sb.toString());
        }

        statsOut.close();
    }
}
