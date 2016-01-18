package com.creamsugardonut;

import org.junit.Test;

/**
 * Created by lks21c on 16. 1. 14.
 */
public class AgentTest {

    @Test
    public void shouldInstantiateSleepingInstance() throws InterruptedException {
        Sleeping sleeping = new Sleeping();
        sleeping.randomSleep();
    }
}
