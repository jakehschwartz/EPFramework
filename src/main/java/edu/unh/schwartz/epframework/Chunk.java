package edu.unh.schwartz.epframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
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
     * The log.
     */
    private static final Log LOG = LogFactory.getLog(Chunk.class);

    /**
     * The runtime of the chunk in millis.
     */
    private long runtime;
    
    /**
     * The name of the chunk.
     */
    private String name;

    /**
     * The directory where the in and out files are saved.
     */
    private File directory;

    /**
     * Constructs a chunk.
     * @param name - the name of the chunk
     * @param directory - the directory for the chunk's IO
     */
    public Chunk(final String name, final File directory)
    {
        this.name = name;
        this.directory = directory;
    }

    /**
     * Constructs a chunk based off a file and the directory for the chunk.
     * @param inFile - the file that contains the chunk's content
     * @param directory - the directory for the chunk's IO
     * @throws IOException if ther is a problem copying the file
     */
    public Chunk(final File inFile, final File directory) throws IOException
    {
        this.directory = directory;
        this.name = inFile.getName();

        // Write the content to a file in the directory
        Files.copy(inFile.toPath(), new File(getInFileName()).toPath()); 

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
        delete(this.directory);
        this.runtime = 0;
    }

    /**
     * Recursively deletes file system rooted at file.
     *
     * @param file - the file to delete
     */
    private void delete(final File file)
    {
        if (file.isDirectory())
        {
            for (final File f : file.listFiles())
            {
                delete(f);
            }
        }

        file.delete();
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
     *
     * @param dir - true iff the output file should be a directory
     */
    public void createOutFile(final boolean dir)
    {
        final String outName = getOutFileName();
        final File out = new File(outName);
        try
        {
            if (dir)
            {
                out.mkdir(); 
            }
            else
            {
                out.createNewFile();
            }
        }
        catch (IOException e)
        {
            LOG.error("createOutFile: " + e.getMessage());
        }
    }

    /**
     * @return the name of the chunk
     */
    public String getName()
    {
        return this.name;
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
