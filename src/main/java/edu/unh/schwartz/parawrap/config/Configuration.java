package edu.unh.schwartz.parawrap.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * The settings for the current run of the application. 
 *
 * Talk about the various fields it contains
 */
public final class Configuration
{
    /**
     * Key for in file setting.
     */
    public static final String IN_FILE_KEY = "inFile";

    /**
     * Key for split pattern setting.
     */
    public static final String SPLIT_PATTERN_KEY = "split";

    /**
     * Key for out file setting.
     */
    public static final String OUT_FILE_KEY = "outFile";

    /**
     * Key for executable location setting.
     */
    public static final String EXEC_LOC_KEY = "execLoc";

    /**
     * Key for in flag setting.
     */
    public static final String IN_FLAG_KEY = "inFlag";

    /**
     * Key for out flag setting.
     */
    public static final String OUT_FLAG_KEY = "outFlag";
    
    /**
     * Key for threading setting.
     */
    public static final String NUM_PROCESSES_KEY = "numProcesses";

    /**
     * Key for stats setting.
     */
    public static final String STATS_KEY = "stats";

    /**
     * Key for output file header setting.
     */
    public static final String NUM_HEADER_KEY = "numHeaderLines";

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
     * Whether or not to make a stats file.
     */
    private boolean makeStats;

    /**
     * Number of header lines in an output file from the executable.
     */
    private int numberOfHeaderLines;

    
    /**
     * Constructs a configuration file from a map.
     * @param map - the map that holds the configuration settings
     */
    public Configuration(final Map<String, Object> map)
    {
         final Iterator<String> it = map.keySet().iterator();
         while (it.hasNext()) 
         {
             final String key = it.next();
             final Object val = map.get(key);
             switch(key)
             {
                 case IN_FILE_KEY:
                    this.inputFileName = (String) val;
                    break;
                 case SPLIT_PATTERN_KEY:
                    this.splitPattern = (String) val;
                    break;
                 case OUT_FILE_KEY:
                    this.outDirName = (String) val;
                    break;
                 case EXEC_LOC_KEY:
                    this.execPath = (String) val;
                     break;
                 case IN_FLAG_KEY: 
                     this.inFlag = (String) val;
                     break;
                 case OUT_FLAG_KEY:
                     this.outFlag = (String) val;
                     break;
                 case NUM_PROCESSES_KEY:
                     this.numberOfThreads = Integer.valueOf((String) val);
                     break;
                 case STATS_KEY:
                     this.makeStats = Boolean.valueOf((String) val);
                     break;
                 case NUM_HEADER_KEY:
                     this.numberOfHeaderLines = Integer.valueOf((String) val);
                     break;
                 default:
                     assert(false);

             }
             System.out.println(key  + " => " + map.get(key));
         }
    }

    /**
     * Constructs a configuration file from a saved copy.
     * @param jsonFile - the file holding the saved configuration file
     */
    // public Configuration(final File jsonFile)
    // {

    // }

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

    /**
     * @return the path to the input file
     */
    public String getInputFileName()
    {
        return this.inputFileName;
    }

    /**
     * @return the regex pattern used the split the input file
     */
    public String getSplitPattern()
    {
        return splitPattern;
    }

    public String getOutputDirectoryName()
    {
        return this.outDirName;
    }

    public String getExecutable()
    {
        return this.execPath;
    }

    public String getInFlag()
    {
        return this.inFlag;
    }

    public String getOutFlag()
    {
        return this.outFlag;
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

    public int getNumHeaderLines()
    {
        return 1;
    }
}
