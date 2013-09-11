package edu.unh.schwartz.parawrap.worker;

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
    public WorkerPool(final int num, final PriorityBlockingQueue<String> chunks) 
    {
        // Start the threads
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
     * Print the statistics for the workers.
     */
    public void printStats()
    {
        long kaks_time = 0;
        for (int i = 0; i < workers.length; i++)
        {
            kaks_time += workers[i].getRunTime();
        }
        System.out.println(kaks_time / 1000);
    }
}
