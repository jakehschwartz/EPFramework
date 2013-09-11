package edu.unh.schwartz.parawrap.spliter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Pattern;

/**
 * Jacob Schwartz
 * Independent Study - Split submodule
 *
 * Used to split up files based on a regular expression that will define the
 * last line in the chunk.
 */
public final class Spliter
{
    private Pattern pattern;

    /**
     * Constructs a spliter with a pattern that seperates every line (^.*$).
     */
    public Spliter()
    {
        pattern = Pattern.compile("^.*$");
    }

    /**
     * Constructs a spliter with a custom pattern.
     *
     * @param regex - Regular expression to split on
     */
    public Spliter(final String regex)
    {
        pattern = Pattern.compile(regex);
    }

    /**
     * Splits the file and returns a thread-safe priority queue of chunks.
     *
     * @param f - the file to split
     * @return a queue of the chunks
     * @throws IOException if the input file cannot be found or there is a
     * problem reading the file
     */
    public PriorityBlockingQueue<String> split(final File f) throws IOException
    {
        final PriorityBlockingQueue<String> res = 
            new PriorityBlockingQueue<String>();

        StringBuilder sb = new StringBuilder();
        
        final BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        while (line != null)
        {
            sb.append(line);
            if (pattern.matcher(line).matches())
            {
                res.put(sb.toString());
                sb = new StringBuilder();
            }

            line = reader.readLine();
        }

        return res;
    }
}
