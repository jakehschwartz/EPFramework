package edu.unh.schwartz.parawrap.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
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
    private List<File> dirs;
    private PriorityBlockingQueue<File> files;
    private List<Chunk> chunks;

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
        this.dirs = new ArrayList<File>();
    }

    /**
     * Splits the file and creates chunks.
     *
     * @param f - the file to split
     * @throws IOException if the input file cannot be found or there is a
     * problem reading the file
     */
    public void split(final File f) throws IOException
    {
        this.files = new PriorityBlockingQueue<File>();
        this.chunks = new ArrayList<Chunk>();

        StringBuilder sb = new StringBuilder();
        
        final BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        while (line != null)
        {
            sb.append(line);
            if (pattern.matcher(line).matches())
            {
                String content = sb.toString();

                // Make a directory for that file
                File dir = new File("/tmp/" + content.hashCode());
                dir.mkdir();
                this.dirs.add(dir);

                // Make the new file
                File in = new File(dir.getName() + "/in");
                in.createNewFile();
                this.files.add(in);

                // Save the chunk
                this.chunks.add(new Chunk(content));

                // Write the content to a file
                try
                {
                    PrintWriter inWriter = new PrintWriter(in);
                    inWriter.println(content);
                    inWriter.close();
                }
                catch (FileNotFoundException e)
                {
                    System.err.println("Could not create file");
                    return;
                }            

                sb = new StringBuilder();
            }

            line = reader.readLine();
        }
    }
    
    public PriorityBlockingQueue<File> getFiles()
    {
        return this.files;
    }

    public List<Chunks> getChunks()
    {
        return this.chunks;
    }

    public void merge(final String outFileName, int headerLines)
    {
        PrintWriter outputWriter = null;
        try
        {
           outputWriter = new PrintWriter(outFileName);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Could not create output file called " + 
                outFileName);
            return;
        }

        for (final File f : this.dirs)
        {
            try
            {
                String inFileName = f.getName() + "/out";
                final FileReader inFile = new FileReader(inFileName);
                final BufferedReader in = new BufferedReader(inFile);
                for (int i = 0; i < headerLines; i++)
                {
                    in.readLine();
                }
                outputWriter.println(in.readLine());
            }
            catch (FileNotFoundException e1)
            {
                System.out.println();
            }
            catch (IOException e2)
            {
                System.out.println();
            }
        }

        outputWriter.close();
    }

    public void cleanUp()
    {
        for (final File f : this.dirs)
        {
            for (final File f2 : f.listFiles())
            {
                f2.delete();
            }
            f.delete();
        }
    }
}
