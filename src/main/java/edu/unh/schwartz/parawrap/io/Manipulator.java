package edu.unh.schwartz.parawrap.io;

import edu.unh.schwartz.parawrap.Chunk;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Does all of the IO functions for the process. Can split an input file based
 * on a pattern and merge it back together in the same order.
 */
public final class Manipulator
{
    /**
     * The pattern used to split the input file.
     */
    private Pattern pattern;
    
    /**
     * The chunks for the current input file.
     */
    private List<Chunk> chunks;

    /**
     * Constructs a spliter with a pattern that seperates every line (^.*$).
     */
    public Manipulator()
    {
        this("^.*$");
    }

    /**
     * Constructs a spliter with a custom pattern.
     *
     * @param regex - Regular expression to split on
     */
    public Manipulator(final String regex)
    {
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Splits the file and creates chunks.
     *
     * @param fileName - the name of the file
     * @throws IOException - when there is any kind of problem with the input
     */
    public void split(final String fileName) throws IOException 
    {
        this.chunks = new ArrayList<Chunk>();

        StringBuilder sb = new StringBuilder();
        
        final File f = new File(fileName);
        final BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        while (line != null)
        {
            sb.append(line);
            if (pattern.matcher(line).matches())
            {
                // Save the chunk
                final String content = sb.toString();

                // Make a directory for that file
                final File dir = new File("/tmp/" + content.hashCode());
                dir.mkdir();

                // Make the new file
                final File in = new File(dir.getName() + "/in");
                in.createNewFile();

                // Write the content to a file
                final PrintWriter inWriter = new PrintWriter(in);
                inWriter.println(content);
                inWriter.close();

                this.chunks.add(new Chunk(content, dir));

                sb = new StringBuilder();
            }

            line = reader.readLine();
        }
    }
    
    /**
     * @return the chunks as a thread-safe queue
     */
    public PriorityBlockingQueue<Chunk> getChunks()
    {
        return new PriorityBlockingQueue<Chunk>(this.chunks);
    }

    /**
     * Merges the chunks back together. Prints the results of the work in the
     * order of the original chunks.
     * @param fileName - the name of the output file
     */
    public void merge(final String fileName)
    {
        PrintWriter writer = null;
        try
        {
           writer = new PrintWriter(fileName);
           for (final Chunk c : this.chunks)
           {
               writer.println(c.getResult());
           }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Could not create output file called " + 
                fileName);
            return;
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Cleans up the chunks and temp out files and directories.
     */
    public void cleanUp()
    {
        for (final Chunk c : this.chunks)
        {
            c.clean();
        }
    }

    /**
     * Prints the statistics of the <code>Worker</code>s and the
     * <code>Chunk</code>s to a CSV file 'stats.csv' in the same output
     * directory as the program.
     * @param workerStats - the statstics of the Workers
     */
    public void printStats(final String workerStats)
    {
        // Open up the file to save to
        // TODO: print to correct directory
        try
        {
            final String comma = ",";
            final PrintWriter statsOut = new PrintWriter("stats.csv");
            statsOut.println(workerStats);

            statsOut.println("Chunk #,Runtime,Chunks Run,Avg Time Per Chunk");
            for (int i = 0; i < this.chunks.size(); i++)
            {
                final Chunk c = this.chunks.get(i);
                final StringBuilder sb = new StringBuilder();
                sb.append(c.hashCode()).append(comma).append(c.length());
                sb.append(comma).append(c.getRuntime());
                statsOut.println(sb.toString());
            }

            statsOut.close();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }
}
