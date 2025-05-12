package com.demomq.juc.leecode;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 对AtomicReference及AtomicReferenceFieldUpdater做了比较，一个是对对象引用的CAS，一个是对对象字段的CAS
 */
public class AtomicTools {
    static AtomicReferenceFieldUpdater<AtomicTools, String> updater = AtomicReferenceFieldUpdater.newUpdater(AtomicTools.class, String.class, "vale");
    static AtomicReference<AtomicTools> atomicReference = new AtomicReference<>();
    volatile String vale;

    public static void main(String[] args) {
        System.out.println(updater.updateAndGet(new AtomicTools(), (a) -> "big"));
        atomicReference.compareAndSet(null, new AtomicTools());
    }
}
