package edu.unh.schwartz.parawrap.worker;

import edu.unh.schwartz.parawrap.Chunk;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the workers.
 */
public final class WorkerPool
{
    /**
     * The workers to be utilizated.
     */
    private Worker[] workers;

    /**
     * The chunks for the workers to work on.
     */
    private List<Chunk> chunks;

    /**
     * Constructs the workers.
     *
     * @param num - the number of workers
     * @param chunks - the work for the workers
     */
    public WorkerPool(final int num, final PriorityBlockingQueue<Chunk> chunks)
    {
        // Initialize the threads
        workers = new Worker[num];
        for (int i = 0; i < num; i++)
        {
            workers[i] = new Worker(i, chunks);
        }

        this.chunks = new ArrayList<Chunk>(chunks);
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
     * @throws IOException - for now
     */
    public void printStats() throws IOException
    {
        // Open up the file to save to
        // TODO: print to correct directory
        final PrintWriter statsOut = new PrintWriter("stats.csv");

        // Print the stats
        statsOut.println("Thread #,Runtime,Chunks Run,Avg Time Per Chunk");
        final String comma = ",";
        for (int i = 0; i < workers.length; i++)
        {
            final long runtime = workers[i].getRunTime();
            final int chunks = workers[i].getChunksRun();
            final StringBuilder sb = new StringBuilder();
            sb.append(i).append(comma).append(runtime).append(comma);
            sb.append(chunks).append(comma).append(runtime / chunks);

            statsOut.println(sb.toString());
        }
        statsOut.println();

        // Chunk info
        statsOut.println("Chunk #,Runtime,Chunks Run,Avg Time Per Chunk");
        for (int i = 0; i < this.chunks.size(); i++)
        {
            final Chunk c = this.chunks.get(i);
            final StringBuilder sb = new StringBuilder();
            sb.append(c.hashCode()).append(comma).append(c.length());
            sb.append(comma).append(c.getRuntime());
            statsOut.println(sb.toString());
        }

        statsOut.close();
    }
}
