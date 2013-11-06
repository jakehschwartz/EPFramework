package edu.unh.schwartz.epframework.worker;

import edu.unh.schwartz.epframework.Chunk;
import edu.unh.schwartz.epframework.config.Configuration;
import java.util.concurrent.PriorityBlockingQueue;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

/**
 * Creates and starts the <code>Worker</code>s.
 */
public final class WorkerPool
{
    /**
     * The Log.
     */
    private static final Log LOG = LogFactory.getLog(WorkerPool.class);

    /**
     * The workers to be utilizated.
     */
    private Worker[] workers;

    /**
     * Constructs the workers.
     *
     * @param config - information for the run
     * @param chunks - the work for the workers
     */
    public WorkerPool(final Configuration config, 
        final PriorityBlockingQueue<Chunk> chunks)
    {
        // Set the executable information in the worker
        Worker.setExecutable(config.getExecutable());
        Worker.setInFlag(config.getInFlag());
        Worker.setOutFlag(config.getOutFlag());

        // Initialize the threads
        workers = new Worker[config.getNumberOfThreads()];
        for (int i = 0; i < workers.length; i++)
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
            LOG.fatal("Worker interrupted: " + e.getMessage());
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
        sb.append("Thread #,Runtime(ms),Chunks Run,Avg Time Per Chunk(ms)\n");
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
