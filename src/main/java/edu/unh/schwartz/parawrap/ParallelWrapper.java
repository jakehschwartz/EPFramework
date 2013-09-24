package edu.unh.schwartz.parawrap;

import edu.unh.schwartz.parawrap.config.Configuration;
import edu.unh.schwartz.parawrap.config.ConfigWizard;
import edu.unh.schwartz.parawrap.io.Manipulator;
import edu.unh.schwartz.parawrap.worker.WorkerPool;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Application class. Takes an optional command line parameter for the run
 * configuration. If none, is given, goes through the wizard to create one,
 * which will then allow the user to save it for later runs. Calls the split
 * operation, starts the workers, calls the move operation and then prints
 * statistics about the run to 'stats.csv'.
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
        final Manipulator manip = new Manipulator(config.getSplitPattern());
        try
        {
            manip.split(fileName);
        }
        catch (IOException e)
        {
            System.err.print(e.getMessage());
            return;
        }

        final PriorityBlockingQueue<Chunk> chunks = manip.getChunks();
        if (chunks.size() == 0)
        {
            System.err.println("Incorrect chunk pattern or empty input file");
            return;
        }

        final WorkerPool wp = new WorkerPool(threads, chunks);
        wp.start();
        manip.merge("out");
        manip.printStats(wp.getStats());
    }

    /**
     * Main method. You can put a lot more here. Explain what the different args
     * do.
     *
     * @param args - Command line arguments
     */
    public static void main(final String[] args) 
    {
        // If there are no cmd line args, create a configuration file
        if (args.length == 0)
        {
            // Allow the user to make a configuration file
            System.out.println("Opening Configuration Wizard");
            final Configuration config = 
                ConfigWizard.getInstance().createConfiguration();
            start(config);
        }
        // First command line arg is a configuration file
        else if (args.length == 1)
        {
            try
            {
                final Configuration config = new Configuration(args[0]);
                start(config);
            }
            catch (IOException e)
            {
                System.err.println("Problem reading configuation file: " + 
                        e.getMessage());
            }
        }
        else
        {
            System.err.print("Usage:\n\tTakes only one option args:\n\t\t");
            System.err.print("- The configuration file's location\n\n");
            System.err.println("No arguments will bring up the wizard");
        }
    }
}
