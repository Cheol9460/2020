package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool
{
    public static ThreadPoolExecutor OnlyOne(String name)
    {
        ThreadPoolExecutor exe = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(name), new AbortPolicy());
        return exe;
    }
    
    public static ThreadPoolExecutor WaitAndRun(String name, int t, int q)
    {
        ThreadPoolExecutor exe = new ThreadPoolExecutor(t, t, 5000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(q, true), new DefaultThreadFactory(name), new ThreadPoolExecutor.CallerRunsPolicy());
        return exe;
    }
    
    static class DefaultThreadFactory implements ThreadFactory
    {
        final ThreadGroup  tgrp;
        final String       name;
        AtomicInteger      pnum = new AtomicInteger(0); // poolNumber
        AtomicInteger      tnum = new AtomicInteger(1); // threadNumber
        
        DefaultThreadFactory(String name)
        {
            SecurityManager s = System.getSecurityManager();
            this.tgrp = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.name = "pool - " + name + " " + pnum.getAndIncrement();
        }

        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(tgrp, r, name + tnum.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY + 3);
            return t;
        }
    }
    
    public static void WaitAndShutDown(ExecutorService exe, int sec)
    {
        try
        {
            exe.shutdown();
            while (exe.awaitTermination(sec, TimeUnit.SECONDS) == false) {}
        }
        catch (Exception _) {}
        
    }
}