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
    /**
     * The Log.
     */
    private static Log LOG = LogFactory.getLog(EPFramework.class);

    private EPFramework() 
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

        Manipulator manip = null;
        try
        {
            if (config.getInputFile() == null)
            {
                // Make a chunk for each presplit file
                manip = new Manipulator(config.getNumHeaderLines());
                manip.splitFiles(config.getInputDirectory());
            }
            else
            {
                // Read in the input file and get the chunks
                manip = new Manipulator(config.getSplitPattern(), 
                        config.getNumHeaderLines());

                manip.split(config.getInputFile());
            }
        }
        catch (IOException e)
        {
            LOG.fatal("split: " + e.getMessage());
            return;
        }

        // Get the chunks made my the manipulator
        final PriorityBlockingQueue<Chunk> chunks = manip.getChunks();
        if (chunks.size() == 0)
        {
            LOG.fatal("Incorrect chunk pattern or empty input file");
            return;
        }

        // Start the workers
        final WorkerPool wp = new WorkerPool(config, chunks);
        wp.start();

        // Merge the results back together
        manip.merge(config.getOutputDirectory() + "/done.txt", 
            config.getMergeMethod());

        // Make stats if instructed to 
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
        else if (args.length == 1)
        {
            try
            {
                LOG.debug("Loading configuration file");
                final Configuration config = new Configuration(args[0]);
                start(config);
            }
            catch (IOException e)
            {

                LOG.fatal("Problem loading config file: " + e.getMessage());
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