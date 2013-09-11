public class WorkerFactory
{
    private Worker[] workers;

    public WorkerFactory() 
    {
        // Start the threads
        workers = new Worker[numThreads];
        for (int i = 0; i < numThreads; i++)
        {
            workers[i] = new Worker(i, chunks);
        }
    }

    public void start()
    {
        for (int i = 0; i < numThreads; i++)
        {
            workers[i].start();
        }

        for (int i = 0; i < numThreads; i++)
        {
            workers[i].join();
        }
    }

    public void printStats()
    {
        long kaks_time = 0;
        for (int i = 0; i < numThreads; i++)
        {
            kaks_time += workers[i].getRunTime();
        }
        System.out.println(kaks_time / 1000);
    }
}
