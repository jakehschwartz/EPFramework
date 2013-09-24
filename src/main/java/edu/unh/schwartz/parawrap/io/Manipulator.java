package edu.unh.schwartz.parawrap.io;

import edu.unh.schwartz.parawrap.Chunk;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Pattern;

/**
 * Jacob Schwartz
 * Independent Study - Split submodule
 *
 * Used to split up files based on a regular expression that will define the
 * last line in the chunk.
 */
public final class Manipulator
{
    private Pattern pattern;
    private PriorityBlockingQueue<Chunk> chunks;

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
        this.chunks = new PriorityBlockingQueue<Chunk>();

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
    
    public PriorityBlockingQueue<Chunk> getChunks()
    {
        return this.chunks;
    }

    /**
     * Merges.
     * @param outFileName - the name of the output file
     */
    public void merge(final String outFileName)
    {
        PrintWriter outputWriter = null;
        try
        {
           outputWriter = new PrintWriter(outFileName);
           for (final Chunk c : this.chunks)
           {
               outputWriter.println(c.getResult());
           }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Could not create output file called " + 
                outFileName);
            return;
        }
        finally
        {
            outputWriter.close();
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
}
