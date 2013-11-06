package edu.unh.schwartz.parawrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Piece of work to do be done by the <code>Worker</code>s. Also contains
 * statstical information that can be used to get the ideal configuration.
 */
public final class Chunk implements Comparable<Chunk>
{
    /**
     * The number of header lines used in the output file.
     */
    private static int headerLines;

    /**
     * the log.
     */
    private static final Log LOG = LogFactory.getLog(Chunk.class);

    /**
     * The runtime of the chunk in millis.
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
     * @return the header from the results of the processed chunk
     */
    public String getHeader()
    {
        final StringBuilder sb = new StringBuilder();
        
        try(final BufferedReader br = 
                new BufferedReader(new FileReader(getOutFileName())))
        {
            // Read the header lines
            for (int i = 0; i < headerLines; i++)
            {
                sb.append(br.readLine()).append("\n");
            }
        }
        catch (IOException e)
        {
            LOG.error("getHeader: " + e.getMessage());
        }
        
        return sb.toString();
 
    }

    /**
     * @return the result of the run on the chunk aka the content of the outfile
     */
    public String getResult()
    {
        final StringBuilder sb = new StringBuilder();
        
        try(final BufferedReader br = 
                new BufferedReader(new FileReader(getOutFileName())))
        {
            // Skip the header lines
            for (int i = 0; i < headerLines; i++)
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
        }
        catch (IOException e)
        {
            LOG.error("getResult: " + e.getMessage());
        }
        
        return sb.toString();
    }

    /**
     * Creates the output file for use my the worker.
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
            LOG.error("createOutFile: " + e.getMessage());
        }
    }

    /**
     * @return the input file for this chunk
     */
    public String getInFileName()
    {
        return this.directory.getAbsolutePath() + "/in";
    }

    /**
     * @return the output file for this chunk
     */
    public String getOutFileName()
    {
        return this.directory.getAbsolutePath() + "/out";
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public int hashCode() 
    {
        return this.content.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public int compareTo(final Chunk c) 
    {
        return 0;
    }

    /**
     * Set the number of header lines in an output file.
     * @param hl - the number of header lines in an output file
     */
    public static void setHeaderLines(final int hl)
    {
        headerLines = hl;
    }
}
