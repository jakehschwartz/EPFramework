package edu.unh.schwartz.parawrap;

public final class Chunk
{
    private long runtime;
    private String content;

    public Chunk(final String content)
    {
        this.content = content;
    }

    public int getLength()
    {
        return this.content.length();
    }

    public long getRuntime()
    {
        return this.runtime;
    }

    public void setRuntime(final long runtime)
    {
        this.runtime = runtime;
    }
}
