package edu.unh.schwartz.epframework.worker;

import edu.unh.schwartz.epframework.Chunk;
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
     * The arguments for the executable.
     */
    private static String[] arguments;

    /**
     * Whether the Chunks need output files or output directories.
     */
    private static boolean outputDirs;

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
     * Set the arguments for executable.
     *
     * @param argument - the command line argument string
     */
    public static void setArguments(final String argument)
    {
        LOG.debug("setArgument: " + argument);
        arguments = argument.split("\\s");
    }

    /**
     * Set the output file/directory setting.
     *
     * @param dirs - true iff output directories are supposed to be used
     */
    public static void setOutputDirs(final boolean dirs)
    {
        outputDirs = dirs;
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
            c.createOutFile(outputDirs);

            try
            {
                // Create the process
                final ProcessBuilder pb = createProcess(c);
                pb.redirectErrorStream(true);

                // Start the work and capture the time it takes to run
                LOG.info("Starting chunk " + c.hashCode() + " on Worker " + this.idNum);
                final long start = System.currentTimeMillis();
                final Process proc = pb.start();
                proc.waitFor();
                final long end = System.currentTimeMillis();
                c.setRuntime(end - start);
                runTime += (end - start);
                LOG.info("Finished chunk on Worker " + this.idNum + " in " + 
                        (end - start));
            }
            catch (InterruptedException|IOException e)
            {
                LOG.error("run: " + e.getMessage());
            }

            this.chunksRun++;
        }
        LOG.info("Worker " + this.idNum + " has finished");
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
        
        // Add the arguments in
        for (String a : arguments)
        {
            if (a.equals("@"))
            {
                commands.add(in);
            }
            else if (a.equals("$"))
            {
                commands.add(out);
            }
            else
            {
                commands.add(a);
            }
        }

        // System.out.println("\n\n\n");
        // for(String co : commands)
        // {
            // System.out.print(co);
            // System.out.print(" ");
        // }
        // System.out.println();

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
