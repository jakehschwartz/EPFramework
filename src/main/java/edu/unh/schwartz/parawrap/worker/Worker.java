package edu.unh.schwartz.parawrap.worker;

import edu.unh.schwartz.parawrap.Chunk;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * Worker. Blah blah blah
 */
public final class Worker extends Thread
{
    /**
     * The queue of pieces to do work on.
     */
    private PriorityBlockingQueue<Chunk> queue;
    
    /**
     * The id number of this worker.
     */
    private int idNum;
    
    /**
     * The sum of run times of executable.
     */
    private long runTime;

    /**
     * The number of chunks run by this worker.
     */
    private int chunksRun;

    /**
     * Creates a worker.
     *
     * @param idNum - the id number of this worker
     * @param queue - the queue for the worker to take chunks from
     */
    public Worker(final int idNum, final PriorityBlockingQueue<Chunk> queue)
    {
        this.queue = queue;
        this.idNum = idNum;
        this.runTime = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        while (this.queue.size() != 0)
        {
            final Chunk c = this.queue.poll();
            c.createOutFile();

            try
            {
                final ProcessBuilder pb = createProcess(c);
                pb.redirectErrorStream(true);

                System.out.println("Starting chunk on thread " + this.idNum);
                final long start = System.currentTimeMillis();
                final Process proc = pb.start();
                proc.waitFor();
                final long end = System.currentTimeMillis();
                c.setRuntime(end - start);
                runTime += (end - start);
                System.out.println("Finished chunk on thread " + this.idNum);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e2)
            {
                e2.printStackTrace();
            }

            this.chunksRun++;
        }
    }

    private ProcessBuilder createProcess(final Chunk c)
    {
        final String in = c.getInFileName();
        final String out = c.getOutFileName();

        final List<String> commands = new ArrayList<String>();
        commands.add("./KaKs_Calculator");
        commands.add("-i");
        commands.add(in);
        commands.add("-o");
        commands.add(out);
 
        return new ProcessBuilder(commands);
    }

    /**
     * @return the total run time of the executable of this worker
     */
    public long getRunTime()
    {
        return this.runTime;
    }

    public int getChunksRun()
    {
        return this.chunksRun;
    }
}
