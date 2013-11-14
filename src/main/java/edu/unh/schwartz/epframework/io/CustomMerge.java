package edu.unh.schwartz.epframework.io;

import edu.unh.schwartz.epframework.Chunk;
import java.util.List;

/**
 * This class allows the end-user to write their own code to merge the results
 * of the chunks together to print in a file.
 */
public final class CustomMerge
{
    private CustomMerge()
    {
        // Prevent instantiation
        // Don't add code here
    }

    /**
     * Custom merge method. User is responsible for making an output stream of
     * their choice and writing to it with the results from the chunks.
     *
     * @param fileName - the name of the file to print to
     * @param chunks - the chunks
     */
    public static void merge(final String fileName, final List<Chunk> chunks)
    {
        // Use Chunk.getResult() to get the result of the chunk being processed
        // by the executable. Chunk.getHeader() to get the header from the file
        
        // Add your code here
    }
}
