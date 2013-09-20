package edu.unh.schwartz.parawrap.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;

/**
 * The settings for the current run of the application. 
 *
 * Talk about the various fields it contains
 */
public final class Configuration
{
    /**
     * The number of threads used.
     */
    private int numThreads;
    
    /**
     * The name of the input file.
     */
    private String inFileName;
    
    /**
     * The regex pattern to split the input file on.
     */
    private String splitPattern;

    /**
     * Constructs an empty configuration file.
     */
    public Configuration()
    {
        System.err.println("TODO");
    }

    /**
     * Constructs a configuration file from a past configuration file that was
     * saved to the disk.
     *
     * @param fileName - the path to the file on disk
     * @throws IOException - if there is any problem reading the file
     */
    public Configuration(final String fileName) throws IOException
    {
        final JsonFactory jf = new JsonFactory();
        final JsonParser jp = jf.createJsonParser(new File(fileName));

        while (jp.nextToken() != JsonToken.END_OBJECT)
        {
            final String fieldname = jp.getCurrentName();
            if ("name".equals(fieldname)) 
            {
                jp.nextToken();
                System.out.println(jp.getText()); 
            }
        }
        jp.close();
    }

    /**
     * @return the number of threads the program will use
     */
    public int getNumberOfThreads()
    {
        return numThreads;
    }

    /**
     * @return the path to the input file
     */
    public String getInputFileName()
    {
        return inFileName;
    }

    /**
     * @return the regex pattern used the split the input file
     */
    public String getSplitPattern()
    {
        return splitPattern;
    }

    public int getNumHeaderLine()
    {
        return 1;
    }
}
