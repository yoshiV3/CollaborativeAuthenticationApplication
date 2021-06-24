package com.project.collaborativeauthenticationapplication.service.concurrency;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolSupplier {

    private static final int                        corePoolSize       = 14;
    private static final int                        coreMaximumSize    = 20; //max number of remote clients: 7
    private static final long                       keepAliveTime      = 2;
    private static final int                        queueSize          = 20;
    private static final TimeUnit                   keepAliveTimeUnit  = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> queue                 =  new ArrayBlockingQueue<Runnable>(queueSize);


    public static final String COMPONENT = "Thread pool supplier";
    private static final AndroidLogger logger = new AndroidLogger();




    private static final ThreadPoolExecutor  threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, coreMaximumSize, keepAliveTime, keepAliveTimeUnit, queue);

    public static final ThreadPoolExecutor getSupplier(){
        logger.logEvent(COMPONENT, "new thread", "high");
        return threadPoolExecutor;
    }
}
