package edu.unh.schwartz.parawrap.io;

import edu.unh.schwartz.parawrap.Chunk;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ManipulatorTest
{
    @Test
    public void splitTest()
    {
        Manipulator manip = new Manipulator("^$", 0);
        try
        {
            manip.split("src/test/java/edu/unh/schwartz/parawrap/io/in.txt");
        }
        catch (IOException e)
        {
            assertTrue(e.getMessage(), false);
        }
        PriorityBlockingQueue<Chunk> splits = manip.getChunks();
        assertTrue("Incorrect number of chunks" , splits.size() == 16);
    }
}
