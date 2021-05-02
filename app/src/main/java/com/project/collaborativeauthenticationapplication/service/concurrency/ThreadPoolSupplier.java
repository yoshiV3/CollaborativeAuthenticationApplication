package com.project.collaborativeauthenticationapplication.service.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolSupplier {

    private static final int                        corePoolSize       = 8;
    private static final int                        coreMaximumSize    = 12; //max number of remote clients: 7
    private static final long                       keepAliveTime      = 2;
    private static final int                        queueSize          = 20;
    private static final TimeUnit                   keepAliveTimeUnit  = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> queue                 =  new ArrayBlockingQueue<Runnable>(queueSize);


    private static final ThreadPoolExecutor  threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, coreMaximumSize, keepAliveTime, keepAliveTimeUnit, queue);

    public static final ThreadPoolExecutor getSupplier(){
        return threadPoolExecutor;
    }
}
