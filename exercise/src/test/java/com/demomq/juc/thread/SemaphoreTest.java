package com.demomq.juc.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreTest {
  public static void main(String[] args) {
    Semaphore semaphore = new Semaphore(3);
    Thread producer = new Thread(() -> {
      Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
        try {
          System.out.println("producer try acquire");
          semaphore.acquire();
          System.out.println("producer acquired");
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }, 1, 1, TimeUnit.SECONDS);
    });
    Thread consumer = new Thread(() -> {
      Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
        semaphore.release();
        System.out.println("consumer released");
      }, 3, 3, TimeUnit.SECONDS);
    });

    producer.start();
    consumer.start();
  }
}
