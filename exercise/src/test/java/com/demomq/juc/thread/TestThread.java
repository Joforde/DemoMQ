package com.demomq.juc.thread;

import java.util.concurrent.TimeUnit;

public class TestThread extends ShutdownableThread {
    public TestThread(String name, boolean isInterruptible) {
        super(name, isInterruptible);
    }

    @Override
    public void doWork() {
        long time = System.currentTimeMillis();
        if (time%2==0){
            pause(2, TimeUnit.SECONDS);
        }
        System.out.println("doWork: " + time);
    }

    public static void main(String[] args) throws InterruptedException {
        TestThread t1 = new TestThread("t1", true);
        //main线程等待3秒后，开始执行t1线程，t1线程也可以自己调用pause方法，当条件不满足时，阻塞一会儿再执行
        t1.pause(3, TimeUnit.SECONDS);
        t1.start();
        TimeUnit.SECONDS.sleep(3);
        t1.shutdown();
    }
}
