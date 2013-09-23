package edu.unh.schwartz.parawrap;

/**
 * Blah.
 */
public final class Chunk
{
    /**
     * The runtime of the chunk in miilis.
     */
    private long runtime;
    
    /**
     * The content of the chunk.
     */
    private String content;

    /**
     * Constructs a chunk based off some content.
     * @param content - what the chunk represents
     */
    public Chunk(final String content)
    {
        this.content = content;
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
}
