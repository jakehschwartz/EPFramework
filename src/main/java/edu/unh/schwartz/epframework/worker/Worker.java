package edu.unh.schwartz.epframework.worker;

import edu.unh.schwartz.epframework.Chunk;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the process on the queued up input. Each worker has an id and keeps
 * various statistical information to use later to determine if the
 * configuration could be better.
 */
public final class Worker extends Thread
{
    /**
     * The path to the executable.
     */
    private static String EXEC;

    /**
     * The in flag for the executable.
     */
    private static String IN_FLAG;

    /**
     * The out flag for the executable.
     */
    private static String OUT_FLAG;

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
        this.chunksRun = 0;
    }

    /**
     * Set the path to the executable.
     *
     * @param exec - the path to the executable
     */
    public static void setExecutable(final String exec)
    {
        System.err.println(exec);
        EXEC = exec;
    }
    
    /**
     * Set the flag to signify where the input goes for the executable.
     *
     * @param inFlag - the in flag used to signify the input file 
     */
    public static void setInFlag(final String inFlag)
    {
        IN_FLAG = inFlag;
    }

    /**
     * Set the flag to signify where the output goes for the executable.
     *
     * @param outFlag - the flag used to signify the output file 
     */
    public static void setOutFlag(final String outFlag)
    {
        OUT_FLAG = outFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        // Until we run out of chunks
        while (this.queue.size() != 0)
        {
            // Grab a chunk and get it ready for output
            final Chunk c = this.queue.poll();
            c.createOutFile();

            try
            {
                // Create the process
                final ProcessBuilder pb = createProcess(c);
                pb.redirectErrorStream(true);

                // Start the work and capture the time it takes to run
                System.out.println("Starting chunk on thread " + this.idNum);
                final long start = System.currentTimeMillis();
                final Process proc = pb.start();
                proc.waitFor();
                final long end = System.currentTimeMillis();
                c.setRuntime(end - start);
                runTime += (end - start);
                System.out.println("Finished chunk " + c.hashCode() + 
                        " on thread " + this.idNum + " in " + (end - start));
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
    
    /**
     * Create the process.
     * @param c - the chunk of work being done
     */
    private ProcessBuilder createProcess(final Chunk c)
    {
        // Get the location of the in and out files
        final String in = c.getInFileName();
        final String out = c.getOutFileName();

        // Create the executable
        final List<String> commands = new ArrayList<String>();
        commands.add(EXEC);

        if (IN_FLAG != null)
        {
            commands.add(IN_FLAG);
        }
        else
        {
            commands.add("<");
        }
        commands.add(in);

        if (OUT_FLAG != null)
        {
            commands.add(OUT_FLAG);
        }
        else
        {
            commands.add(">");
        }
        commands.add(out);

        for (String s : commands)
        {
            System.out.print(s + " ");
        }
        System.out.println();
 
        return new ProcessBuilder(commands);
    }

    /**
     * @return the total run time of the executable of this worker
     */
    public long getRunTime()
    {
        return this.runTime;
    }

    /**
     * @return the number of chunks run by this thread
     */
    public int getChunksRun()
    {
        return this.chunksRun;
    }
}
