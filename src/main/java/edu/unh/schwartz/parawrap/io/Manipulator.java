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
     * The default pattern for a manipulator.
     */
    private static final String DEFAULT_PATTERN = "^.*$";

    /**
     * The pattern used to split the input file.
     */
    private Pattern pattern;
    
    /**
     * The chunks for the current input file.
     */
    private List<Chunk> chunks;

    /**
     * The number of header lines to put in the final output file.
     */
    private int headerLines;

    /**
     * Constructs a manipulator with the default pattern and 0 header lines.
     */
    public Manipulator()
    {
        this(DEFAULT_PATTERN, 0);
    }

    /**
     * Constructs a manipulator with the default pattern and some number of 
     * header lines.
     *
     * @param headerLines - the number of header lines to take from output files
     */
    public Manipulator(final int headerLines)
    {
        this(DEFAULT_PATTERN, headerLines);
    }

    /**
     * Constructs a manipulator with a custom pattern and some number of header
     * lines.
     *
     * @param regex - Regular expression to split on
     * @param headerLines - the number of header lines to take from output files
     */
    public Manipulator(final String regex, final int headerLines)
    {
        this.pattern = Pattern.compile(regex);
        this.headerLines = headerLines;
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
                // final File in = new File(dir.getName() + "/in");
                // in.createNewFile();

                // Write the content to a file
                final PrintWriter inWriter = new PrintWriter(dir.getAbsolutePath() + "/in");
                inWriter.println(content);
                inWriter.close();

                this.chunks.add(new Chunk(content, dir));

                sb = new StringBuilder();
            }
            else
            {
                sb.append("\n");
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
            writer.print(this.chunks.get(0).getHeader());

            for (final Chunk c : this.chunks)
            {
                writer.print(c.getResult());
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println(e.getMessage()); 
            return;
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
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
     * @param outDir - the directory to output the file to
     */
    public void printStats(final String workerStats, final String outDir)
    {
        try
        {
            final String comma = ",";
            final PrintWriter statsOut = new PrintWriter(outDir + "/stats.csv");
            statsOut.println(workerStats);

            statsOut.println("Chunk #,Length,Runtime(ms)");
            for (int i = 0; i < this.chunks.size(); i++)
            {
                final Chunk c = this.chunks.get(i);
                final StringBuilder sb = new StringBuilder();
                sb.append(c.hashCode()).append(comma).append(c.length());
                sb.append(comma).append(c.getRuntime()).append('\n');
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
