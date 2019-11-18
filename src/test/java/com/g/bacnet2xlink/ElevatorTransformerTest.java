package com.g.bacnet2xlink;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ElevatorTransformerTest {

    @Test
    public void run() throws IOException, ClassNotFoundException {
        Configuration cfg = Configuration.fromResource("/config.json");

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

        ElevatorTransformer task = new ElevatorTransformer(cfg, null);
        final ScheduledFuture<?> future = executor.scheduleWithFixedDelay(task,
                10, 30, TimeUnit.SECONDS);

        System.out.println("Start client...");

        while (!future.isDone()) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}