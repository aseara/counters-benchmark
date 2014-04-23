package com.takipi.tests.counters;

import com.takipi.tests.counters.implementations.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main
{
	public static long TARGET_NUMBER 	= 100000000l;
	public static int THREADS 			= 10;
	public static int ROUNDS 			= 10;
	private static String COUNTER 		= Counters.DIRTY.toString();
	
	private static ExecutorService es;
	
	private static int round;
	private static long start;
	
	private static Boolean[] rounds;
	
	private static enum Counters
	{
		DIRTY,
		VOLATILE,
		SYNCHRONIZED,
		RWLOCK,
		ATOMIC,
		ADDER
	}
	
	public static void main(String[] args) {
		COUNTER = Counters.ADDER.toString();
		
		rounds = new Boolean[ROUNDS];
		
		System.out.println("Using " + COUNTER + ". threads: " + THREADS + ". rounds: " + ROUNDS +
				". Target: " + TARGET_NUMBER);
		
		for (round = 0; round < ROUNDS; round++) {
			rounds[round] = Boolean.FALSE;
			
			final Counter counter = getCounter();
			
			es = Executors.newFixedThreadPool(THREADS);
			
			start = System.currentTimeMillis();
			
			for (int j = 0; j < THREADS; j+=2) {
                // counter reade thread
				es.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (Thread.interrupted())
                                break;

                            long count = counter.getCounter();

                            if (count > Main.TARGET_NUMBER) {
                                publish(System.currentTimeMillis());
                                break;
                            }
                        }
                    }
                });
                // counter write thread
				es.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (Thread.interrupted())
                                break;

                            counter.increment();
                        }
                    }
                });
			}
			
			try
			{
				es.awaitTermination(10, TimeUnit.MINUTES);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static Counter getCounter()
	{
		Counters counterTypeEnum = Counters.valueOf(COUNTER);
		
		switch (counterTypeEnum)
		{
			case ADDER:
				return new Adder();
			case ATOMIC:
				return new Atomic();
			case DIRTY:
				return new Dirty();
			case RWLOCK:
				return new RWLock();
			case SYNCHRONIZED:
				return new Synchronized();
			case VOLATILE:
				return new Volatile();
		}
		
		return null;
	}
	
	public static void publish(long end)
	{
		synchronized (rounds[round])
		{
			if (rounds[round] == Boolean.FALSE)
			{
				System.out.println(end-start);
				
				rounds[round] = Boolean.TRUE;
				
				es.shutdownNow();
			}
		}
	}
}
