package edu.unh.schwartz.parawrap.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private int numberOfThreads;
    
    /**
     * The name of the input file.
     */
    private String inputFileName;
    
    /**
     * The regex pattern to split the input file on.
     */
    private String splitPattern;

    /**
     * The directory where the output should go.
     */
    private String outDirName;

    /**
     * The path to the executable.
     */
    private String execPath;

    /**
     * The flags for the executable to show how to enter input and outfile.
     */
    private String inFlag;
    private String outFlag;

    /**
     * The arguments for the execuatable.
     */
    private Map<String, String> execArgs;

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
        return this.numberOfThreads;
    }

    public void setNumberOfThreads(final int numberOfThreads)
    {
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * @return the path to the input file
     */
    public String getInputFileName()
    {
        return this.inputFileName;
    }

    public void setInputFileName(final String inputFileName)
    {
        this.inputFileName = inputFileName;
    }

    /**
     * @return the regex pattern used the split the input file
     */
    public String getSplitPattern()
    {
        return splitPattern;
    }

    public void setSplitPattern(final String splitPattern)
    {
        this.splitPattern = splitPattern;
    }

    public String getOutputDirectoryName()
    {
        return this.outDirName;
    }

    public void setOutputDirectoryName(final String outDirName)
    {
        this.outDirName = outDirName;
    }

    public String getExecutable()
    {
        return this.execPath;
    }

    public void setExecutable(final String execPath)
    {
        this.execPath = execPath;
    }

    public String getInputFileFlag()
    {
        return this.inFlag;
    }

    public void setInputFileFlag(final String inFlag)
    {
        this.inFlag = inFlag;
    }

    public String getOutputFileFlag()
    {
        return this.outFlag;
    }

    public void setOutputFileFlag(final String inFlag)
    {
        this.outFlag = outFlag;
    }

    /**
     * @return the arguments of the executable
     */
    public Map<String, String> getExecutableArgs()
    {
        return this.execArgs;
    }

    /**
     * Adds an argument for the executable. If the value is null, when no value
     * is used, just the flag name.
     *
     * @param flag - the flag name, with the - or --
     * @param value - the value of the flag
     */
    public void addExecutableArg(final String flag, final String value)
    {
        if (execArgs == null)
        {
            execArgs = new HashMap<String, String>();
        }

        execArgs.put(flag, value);
    }
}
