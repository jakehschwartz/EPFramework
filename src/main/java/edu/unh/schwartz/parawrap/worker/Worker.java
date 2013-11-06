package edu.unh.schwartz.parawrap.worker;

import edu.unh.schwartz.parawrap.Chunk;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

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
    private static String exec;

    /**
     * The in flag for the executable.
     */
    private static String inFlag;

    /**
     * The out flag for the executable.
     */
    private static String outFlag;

    /**
     * The Log.
     */
    private static final Log LOG = LogFactory.getLog(Worker.class);

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
     * @param e - the path to the executable
     */
    public static void setExecutable(final String e)
    {
        LOG.debug("setExecutable: " + e);
        exec = e;
    }
    
    /**
     * Set the flag to signify where the input goes for the executable.
     *
     * @param in - the in flag used to signify the input file 
     */
    public static void setInFlag(final String in)
    {
        inFlag = in;
    }

    /**
     * Set the flag to signify where the output goes for the executable.
     *
     * @param out - the flag used to signify the output file 
     */
    public static void setOutFlag(final String out)
    {
        outFlag = out;
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
                LOG.info("Starting chunk on thread " + this.idNum);
                final long start = System.currentTimeMillis();
                final Process proc = pb.start();
                proc.waitFor();
                final long end = System.currentTimeMillis();
                c.setRuntime(end - start);
                runTime += (end - start);
                LOG.info("Finished chunk " + c.hashCode() + " on thread " + 
                        this.idNum + " in " + (end - start));
            }
            catch (InterruptedException|IOException e)
            {
                LOG.error("run: " + e.getMessage());
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
        commands.add(exec);

        if (inFlag != null)
        {
            commands.add(inFlag);
        }
        else
        {
            commands.add("<");
        }
        commands.add(in);

        if (outFlag != null)
        {
            commands.add(outFlag);
        }
        else
        {
            commands.add(">");
        }
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

    /**
     * @return the number of chunks run by this thread
     */
    public int getChunksRun()
    {
        return this.chunksRun;
    }
}
