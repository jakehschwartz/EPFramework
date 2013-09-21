package edu.unh.schwartz.parawrap;

import edu.unh.schwartz.parawrap.config.Configuration;
import edu.unh.schwartz.parawrap.config.ConfigWizard;
import edu.unh.schwartz.parawrap.spliter.Spliter;
import edu.unh.schwartz.parawrap.worker.WorkerPool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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

        // Read in the input file and get the chunks
        // Spliter spliter = new Spliter("$\n^");
        final Spliter spliter = new Spliter(config.getSplitPattern());
        PriorityBlockingQueue<String> chunks = null; 
        try
        {
            final File inFile = new File(fileName);
            chunks = spliter.split(inFile);
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (chunks.size() == 0)
        {
            System.err.println("Incorrect chunk pattern or empty input file");
            System.exit(1);
        }

        final WorkerPool wp = new WorkerPool(threads, chunks);
        wp.start();
        merge(threads);
        wp.printStats();
    }

    /**
     * Merges the output together and deletes the temporary files.
     *
     * @param numThreads - the number of threads used by the application
     */ 
    private static void merge(final int numThreads) 
    {
        PrintWriter outputWriter = null;
        try
        {
           outputWriter = new PrintWriter("output.kaks");
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Could not create output file");
            System.exit(1);
        }

        // Delete the old files
        final String mergeLoc = "/tmp/kaks";
        for (int i = 0; i < numThreads; i++)
        {
            // Remove temp file
            final File temp = new File(mergeLoc + i + "/temp.axt" );
            temp.delete();

            final File folder = new File(mergeLoc + i);
            for (File file : folder.listFiles())
            {
                try
                {
                    System.out.println("Attempting to read " + file.getName());
                    final BufferedReader reader = new BufferedReader(new FileReader(file));
                    reader.readLine();
                    outputWriter.println(reader.readLine());
                    file.delete();
                }
                catch (FileNotFoundException e1)
                {
                    System.out.println();
                }
                catch (IOException e2)
                {
                    System.out.println();
                }
            }
            folder.delete();
        }
        outputWriter.close();
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
                System.err.println("FUck");
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
