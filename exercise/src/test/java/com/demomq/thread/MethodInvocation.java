package com.demomq.thread;

import javax.security.auth.callback.Callback;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

interface RequestCallback extends Callback {
    void requestCompleted(Boolean result);
}

public class MethodInvocation {
    public static void main(String[] args) {
        RequestHandler requestHandler = new RequestHandler();
        for (long i = 0; i < 10; i++) {
            long requestId = i;
            requestHandler.doRequestAsync(requestId, new RequestCallback() {
                @Override
                public void requestCompleted(Boolean result) {
                    System.out.printf("%s,%s,%s\n", Thread.currentThread().getName(), requestId, result);
                }
            });
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        for (long i = 0; i < 10; i++) {
            long requestId = i;
            requestHandler.doRequestFuture(requestId).whenComplete((result, throwable) -> {
                System.out.printf("%s,%s,%s\n", Thread.currentThread().getName(), requestId, result);
            });
        }
        requestHandler.close();
    }
}

class RequestHandler {
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Boolean doRequestSync(Long requestId) {
        return internalDoRequest(requestId);
    }

    public CompletableFuture<Boolean> doRequestFuture(Long requestId) {
        return CompletableFuture.supplyAsync(() -> internalDoRequest(requestId), threadPool);
    }

    public void doRequestAsync(Long requestId, RequestCallback callback) {
        threadPool.submit(() -> {
            Boolean result = internalDoRequest(requestId);
            callback.requestCompleted(result);
        });
    }

    public void close(){
        threadPool.shutdown();
    }

    private Boolean internalDoRequest(Long requestId) {
        try {
            System.out.println("received request " + requestId);
            TimeUnit.SECONDS.sleep(3);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

}

