package com.sensor.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianyi on 18/08/2017.
 */
public class Timer {
    private long timeoutMillisecond;
    private long startTimestamp;
    private static final Logger logger = LoggerFactory.getLogger(Timer.class);

    public Timer() {
        this.startTimestamp = System.currentTimeMillis();
    }

    public Timer(long var1) {
        this.startTimestamp = System.currentTimeMillis();
        this.timeoutMillisecond = var1 * 1000L;
    }

    public Timer(long startTimestamp, long timeoutMillisecond) {
        this.startTimestamp = startTimestamp;
        this.timeoutMillisecond = timeoutMillisecond * 1000L;
    }

    public void reset() {
        this.startTimestamp = System.currentTimeMillis();
    }

    public long elapsedSecond() {
        return this.elapsedMillisecond() / 1000L;
    }

    public long elapsedMillisecond() {
        return System.currentTimeMillis() - this.startTimestamp;
    }

    public boolean isTimeout() {
        return this.elapsedMillisecond() > this.timeoutMillisecond;
    }

    public static void sleepSec(int var0) {
        sleepMillisecond((long)(var0 * 1000));
    }

    public static void sleepMillisecond(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            logger.info("Timer has been interrupted, e:", ex);
        }

    }

    public static void sleepWithCondition(long var0, AtomicBoolean var2, boolean var3) {
        while(var0-- > 0L && var2.get() == var3) {
            sleepSec(1);
        }

    }

    public static void sleepMillSecWithCondition(long var0, AtomicBoolean var2, boolean var3) {
        long var4 = var0 / 1000L;
        sleepWithCondition(var4, var2, var3);
        long var6 = var0 % 1000L;
        if(var6 != 0L && var2.get() == var3) {
            sleepMillisecond(var6);
        }

    }

    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    public String toString() {
        return "Timer[" + this.elapsedMillisecond() + "ms]";
    }
}
