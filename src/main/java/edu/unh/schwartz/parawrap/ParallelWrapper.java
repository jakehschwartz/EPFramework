package edu.unh.schwartz.parawrap;

import edu.unh.schwartz.parawrap.config.Configuration;
import edu.unh.schwartz.parawrap.config.ConfigWizard;
import edu.unh.schwartz.parawrap.io.Manipulator;
import edu.unh.schwartz.parawrap.worker.WorkerPool;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Application class. Blah blah blah.
 *
 * @author Jacob Schwartz
 */
final class ParallelWrapper
{
    private ParallelWrapper() 
    {
        // No-op
    }

    /**
     * Starts the parallel wrapper based on the <code>Configuration</code>.
     *
     * @param config - the instructions for the wrapper
     */
    private static void start(final Configuration config)
    {
        final String fileName = config.getInputFileName();
        final int threads = config.getNumberOfThreads();
        Chunk.setHeaderLines(config.getNumHeaderLines());

        // Read in the input file and get the chunks
        // Spliter spliter = new Spliter("$\n^");
        final Manipulator manip = new Manipulator(config.getSplitPattern());
        try
        {
            manip.split(fileName);
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        final PriorityBlockingQueue<Chunk> chunks = manip.getChunks();
        if (chunks.size() == 0)
        {
            System.err.println("Incorrect chunk pattern or empty input file");
            System.exit(1);
        }

        final WorkerPool wp = new WorkerPool(threads, chunks);
        wp.start();
        manip.merge("out");
        try
        {
            wp.printStats();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Main method. You can put a lot more here. Explain what the different args
     * do.
     *
     * @param args - Command line arguments
     */
    public static void main(final String[] args) 
    {
        if (args.length == 0)
        {
            // Allow the user to make a configuration file
            System.out.println("Opening Configuration Wizard");
            final Configuration config = 
                ConfigWizard.getInstance().createConfiguration();
            start(config);
        }
        else if (args.length == 1)
        {
            try
            {
                final Configuration config = new Configuration(args[0]);
                start(config);
            }
            catch (FileNotFoundException e1)
            {
                System.err.println("I LOVE AUDREY!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            catch (IOException e2)
            {
                System.err.println("Shit");
            }
        }
        else
        {
            // USAGE STATEMENT
            System.err.println("USAGE:");
        }
    }
}
