package com.creamsugardonut;

/**
 * Created by lks21c on 16. 1. 14.
 */
public class Sleeping {

    public void randomSleep() throws InterruptedException {
        long randomSleepDuration = (long) (500 + Math.random() * 700);
        System.out.printf("Sleeping for %d ms ..\n", randomSleepDuration);
        Thread.sleep(randomSleepDuration);
    }
}