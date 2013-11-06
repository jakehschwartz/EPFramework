package edu.unh.schwartz.epframework;

import edu.unh.schwartz.epframework.config.Configuration;
import edu.unh.schwartz.epframework.config.ConfigWizard;
import edu.unh.schwartz.epframework.io.Manipulator;
import edu.unh.schwartz.epframework.worker.WorkerPool;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Application class. Takes an optional command line parameter for the run
 * configuration. If none is given, goes through the wizard to create one,
 * which will then allow the user to save it for later runs. Calls the split
 * operation, starts the workers, calls the move operation and then prints
 * statistics about the run to 'stats.csv'.
 *
 * @author Jacob Schwartz
 */
final class EPFramework
{
<<<<<<< HEAD:src/main/java/edu/unh/schwartz/parawrap/ParallelWrapper.java
    /**
     * The Log.
     */
    private static Log LOG = LogFactory.getLog(ParallelWrapper.class);

    private ParallelWrapper() 
=======
    private EPFramework() 
>>>>>>> d0b9a24644e979bcd84a296ab9f48a3ad1dfed4b:src/main/java/edu/unh/schwartz/epframework/EPFramework.java
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
        Chunk.setHeaderLines(config.getNumHeaderLines());

        // Read in the input file and get the chunks
        final Manipulator manip = new Manipulator(config.getSplitPattern(), 
            config.getNumHeaderLines());

        try
        {
            manip.split(config.getInputFileName());
        }
        catch (IOException e)
        {
            LOG.fatal("split: " + e.getMessage());
            return;
        }

        final PriorityBlockingQueue<Chunk> chunks = manip.getChunks();
        if (chunks.size() == 0)
        {
            LOG.fatal("Incorrect chunk pattern or empty input file");
            return;
        }

        final WorkerPool wp = new WorkerPool(config, chunks);
        wp.start();
        manip.merge(config.getOutputDirectory() + "/done.txt");
        if (config.makeStats())
        {
            manip.printStats(wp.getStats(), config.getOutputDirectory());
        }
        manip.cleanUp();
    }

    /**
     * Main method. Can be run with 0 or 1 cmd line args.
     *
     * @param args - Command line arguments
     */
    public static void main(final String[] args) 
    {
        // If there are no cmd line args, create a configuration file
        if (args.length == 0)
        {
            // Allow the user to make a configuration file
            LOG.debug("Opening Configuration Wizard");
            final Configuration config = 
                ConfigWizard.getInstance().createConfiguration();
            if (config != null)
            {
                start(config);
            }
        }
        // First command line arg is a configuration file
        // else if (args.length == 1)
        // {
            // try
            // {
                // final Configuration config = new Configuration(args[0]);
                // start(config);
            // }
            // catch (IOException e)
            // {
                // System.err.println("Problem reading configuation file: " + 
                        // e.getMessage());
            // }
        // }
        // else
        // {
            // System.err.print("Usage:\n\tTakes only one option args:\n\t\t");
            // System.err.print("- The configuration file's location\n\n");
            // System.err.println("No arguments will bring up the wizard");
        // }
    }
}
