package com.takipi.tests.counters.implementations;

import com.takipi.tests.counters.Counter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RWLock implements Counter
{
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	private Lock rLock = rwLock.readLock();
	private Lock wLock = rwLock.writeLock();
	
	private long counter;
	
	public long getCounter()
	{
		try
		{
			rLock.lock();
			return counter;
		}
		finally
		{
			rLock.unlock();
		}
	}
	
	public void increment()
	{
		try
		{
			wLock.lock();
			++counter;
		}
		finally
		{
			wLock.unlock();
		}
	}
}
