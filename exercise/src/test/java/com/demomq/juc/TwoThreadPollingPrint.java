package com.demomq.juc;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 两个线程交替执行输出100以内的随机数，直到1个线程的随机数跟13去余为0，则结束
 */
public class TwoThreadPollingPrint {

    static class LockConditionSolution {

        static int num = -1;
        static int turn = 1;
        static int count = 0;
        static boolean gameOver = false;
        static Lock lock = new ReentrantLock();

        public static void main(String[] args) {
            Condition condition = lock.newCondition();
            new Thread(() -> playBall("Tom", 1, 2, condition)).start();
            new Thread(() -> playBall("Jack", 2, 1, condition)).start();
        }

        public static void playBall(String name, int curTurn, int nextTurn, Condition condition) {
            while (!gameOver) {
                try {
                    lock.lock();
                    while (turn != curTurn) {
                        condition.await();
                    }
                    if (gameOver) {
                        return;
                    }
                    if (num % 13 == 0) {
                        gameOver = true;
                        System.out.println(name + " win the game");
                    } else {
                        num = new Random().nextInt(100);
                        System.out.println(name + " take the ball " + count++ + " by " + num);
                    }
                    turn = nextTurn;
                    condition.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    static class SemaphoreSolution {
        static int num = -1;
        static int count = 0;
        static boolean gameOver = false;

        public static void main(String[] args) {
            Semaphore semaphore1 = new Semaphore(1);
            Semaphore semaphore2 = new Semaphore(0);

            new Thread(() -> playBall("Tom", semaphore1, semaphore2)).start();
            new Thread(() -> playBall("Jack", semaphore2, semaphore1)).start();
        }

        public static void playBall(String name, Semaphore current, Semaphore next) {
            while (!gameOver) {
                try {
                    current.acquire();
                    if (gameOver) {
                        return;
                    }
                    if (num % 13 == 0) {
                        gameOver = true;
                        System.out.println(name + " win the game");
                    } else {
                        num = new Random().nextInt(100);
                        System.out.println(name + " take the ball " + count++ + " by " + num);
                    }
                    next.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class SynchronizedSolution2 {
        /**
         * 1、Monitor机制
         * 每个Java对象（如代码中的lock对象）都关联一个Monitor，包含同步队列（竞争锁的线程队列）和等待队列（调用wait()的线程队列）。
         * 线程进入synchronized代码块时，会尝试通过monitorenter指令获取Monitor所有权；退出时通过monitorexit释放锁。
         * 2、锁升级过程
         * 偏向锁：无竞争时，锁记录线程ID，减少同步开销。
         * 轻量级锁：通过CAS自旋竞争锁，适用于低并发场景。
         * 重量级锁：竞争激烈时，线程进入阻塞状态，依赖操作系统互斥量（Mutex）
         * 3、synchronized默认非公平锁，可能导致线程饥饿。若需公平性，需改用ReentrantLock
         */
        static Object lock = new Object();
        static int num = -1;
        static int count = 0;
        static int turn = 1;
        static boolean gameOver = false;

        public static void main(String[] args) {
            new Thread(() -> playBall("Tom", 1, 2)).start();
            new Thread(() -> playBall("Jack", 2, 1)).start();
        }

        static void playBall(String name, int curTurn, int nextTurn) {
            while (!gameOver) {
                //竞争的线程进入lock的同步队列
                synchronized (lock) {
                    //if可能导致虚假唤醒
                    while (turn != curTurn) {
                        try {
                            //wait调用后，释放锁，当前线程进入锁的等待队列
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (gameOver) {
                        return;
                    }
                    lock.notify();
                    turn = nextTurn;
                    if (num % 13 == 0) {
                        gameOver = true;
                        System.out.println(name + " win the game");
                    } else {
                        num = new Random().nextInt(100);
                        System.out.println(name + " take the ball " + count++ + " by " + num);
                    }
                }

            }
        }
    }

    static class SynchronizedSolution {
        public static void main(String[] args) throws InterruptedException {
            Object lock = new Object();
            AtomicInteger count = new AtomicInteger(0);
            AtomicBoolean gameOver = new AtomicBoolean(false);
            new BallPlayer("Tom", lock, count, gameOver).start();
            new BallPlayer("Jack", lock, count, gameOver).start();
        }

        static class BallPlayer extends Thread {
            private final String name;
            private final Object lock;
            private final AtomicInteger count;
            private final AtomicBoolean gameOver;

            public BallPlayer(String name, Object lock, AtomicInteger count, AtomicBoolean gameOver) {
                this.name = name;
                this.lock = lock;
                this.count = count;
                this.gameOver = gameOver;
            }

            @Override
            public void run() {
                while (!gameOver.get()) {
                    synchronized (lock) {
                        lock.notifyAll();
                        int num = new Random().nextInt() * 100;
                        System.out.println(name + ":" + count.getAndIncrement());
                        if (num % 13 == 0) {
                            gameOver.set(true);
                            System.out.println(name + " lose the game");
                            return;
                        }
                        System.out.println(name + " take ball " + count.getAndIncrement());
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

    }
}