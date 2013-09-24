package edu.unh.schwartz.parawrap.worker;

import edu.unh.schwartz.parawrap.Chunk;
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
     * @return the statistics for the <code>Worker</code> in a csv table
     */
    public String getStats()
    {
        // Print the stats
        final StringBuilder sb = new StringBuilder();
        sb.append("Thread #,Runtime,Chunks Run,Avg Time Per Chunk");
        final String comma = ",";
        for (int i = 0; i < workers.length; i++)
        {
            final long runtime = workers[i].getRunTime();
            final int chunks = workers[i].getChunksRun();
            sb.append(i).append(comma).append(runtime).append(comma);
            sb.append(chunks).append(comma).append(runtime / chunks);
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
}
