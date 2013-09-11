package edu.unh.schwartz.parawrap.spliter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This is a test class for the splitter submodule. It takes a file and a 
 * regex pattern to split on. It will return a collection of strings that will
 * be the resulting split.
 */
public class SpliterTest
{
    private Spliter spliter;

    public SpliterTest(String regex)
    {
        spliter = new Spliter(regex);
    }
    
    public PriorityBlockingQueue<String> execute(String fileName) 
        throws IOException
    {
        File f = new File(fileName); 
        return spliter.split(f);
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("There should be two arguments:");
            System.err.println("\t- The Regex Pattern\n\t- A File Name");
            return;
        }
        String regexPattern = args[0];
        String fileName = args[1];
        SpliterTest t = new SpliterTest(regexPattern);
        try
        {
            PriorityBlockingQueue<String> splits = t.execute(fileName);
            System.out.println("Found " + splits.size() + " pieces");
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }
}
