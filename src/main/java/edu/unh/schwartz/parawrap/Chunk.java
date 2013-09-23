package edu.unh.schwartz.parawrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Piece of work to do be done by the <code>Worker</code>s. Also contains
 * statstical information that can be used to get the ideal configuration.
 */
public final class Chunk
{
    /**
     * The number of header lines used int he output file.
     */
    private static int HEADER_LINES;

    /**
     * The runtime of the chunk in miilis.
     */
    private long runtime;
    
    /**
     * The content of the chunk.
     */
    private String content;

    /**
     * The directory where the in and out files are saved.
     */
    private File directory;

    /**
     * Constructs a chunk based off some content.
     * @param content - what the chunk represents
     * @param directory - the directory for the chunk's IO
     */
    public Chunk(final String content, final File directory)
    {
        this.content = content;
        this.directory = directory;
    }

    /**
     * @return the length of the chunk's content
     */
    public int length()
    {
        return this.content.length();
    }

    /**
     * @return the number of milliseconds taken to compute the chunk
     */
    public long getRuntime()
    {
        return this.runtime;
    }

    /**
     * Set the runtime of the chunk.
     * @param runtime - the runtime of the chunk in milliseconds
     */
    public void setRuntime(final long runtime)
    {
        this.runtime = runtime;
    }

    /**
     * @return the directory where the content is saved
     */
    public File getDirectory()
    {
        return this.directory;
    }

    /**
     * Deletes the directory and its files.
     */
    public void clean()
    {
        // Delete the files in the directories
        for (final File f : this.directory.listFiles())
        {
            f.delete();
        }
        this.directory.delete();

        this.runtime = 0;
    }

    /**
     * @return the result of the run on the chunk
     */
    public String getResult()
    {
        final StringBuilder sb = new StringBuilder();
        
        try 
        {
            // Open out file
            final BufferedReader br = 
                new BufferedReader(new FileReader(getOutFileName()));

            // Skip the header lines
            for (int i = 0; i < HEADER_LINES; i++)
            {
                br.readLine();
            }

            // Get the actual lines
            String line = br.readLine();
            while (line != null) 
            {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }

            // Close the file
            br.close();
            
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
        
        return sb.toString();
    }

    /**
     * Creates the outfile file for use later.
     */
    public void createOutFile()
    {
        final String outName = getOutFileName();
        final File out = new File(outName);
        try
        {
            out.createNewFile();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    /**
     * @return the input file for this chunk
     */
    public String getInFileName()
    {
        return this.directory.getAbsolutePath() + "in";
    }

    /**
     * @return the output file for this chunk
     */
    public String getOutFileName()
    {
        return this.directory.getAbsolutePath() + "out";
    }

    /**
     * Set the number of header lines in an output file.
     * @param headerLines - the number of header lines in an output file
     */
    public static void setHeaderLines(final int headerLines)
    {
        HEADER_LINES = headerLines;
    }

}
