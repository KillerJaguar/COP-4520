import java.util.Random;

public class cwfllTests2
{
	static final int NUM_TESTS = 500000;
	static final int RAND_MAX  = 1000;//250000;
	static final boolean SIMPLE= true;
	
	public static void main(String[] args)
	{		
		ConcurrentWaitFreeLinkedList4<Integer> test;
		
		test = new ConcurrentWaitFreeLinkedList4<Integer>(1);
		
		System.out.println("Testing one thread...");
		if (SIMPLE){
			simple1(test);
		}
		else{
			System.out.println("Execution time 10i/80d/10c: " + test1Thread(test,.1,.8) + " ms");
			System.out.println(test);
			System.out.println("Execution time 80i/10d/10c: " + test1Thread(test,.8,.1) + " ms");
			System.out.println("Execution time 10i/10d/80c: " + test1Thread(test,.1,.1) + " ms");
		}
		test = new ConcurrentWaitFreeLinkedList4<Integer>(2);
		
		try
		{
			if (SIMPLE){
				//simple2(test);
			}else{
				System.out.println("Testing two threads...");
				System.out.println("Execution time 10i/80d/10c: " + test2Threads(test,.1,.8) + " ms");
				System.out.println("Execution time 80i/10d/10c: " + test2Threads(test,.8,.1) + " ms");
				System.out.println("Execution time 10i/10d/80c: " + test2Threads(test,.1,.1) + " ms");
			}
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
			
		test = new ConcurrentWaitFreeLinkedList4<Integer>(4);
		
		try
		{
			if (SIMPLE){
				//simple4(test);
			}else{
				System.out.println("Testing four threads...");
				System.out.println("Execution time 10i/80d/10c: " + test4Threads(test,.1,.8) + " ms");
				System.out.println("Execution time 80i/10d/10c: " + test4Threads(test,.8,.1) + " ms");
				System.out.println("Execution time 10i/10d/80c: " + test4Threads(test,.1,.1) + " ms");
			}
			
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
		
		test = new ConcurrentWaitFreeLinkedList4<Integer>(8);
		
		try
		{
			if (SIMPLE){
				//simple8(test);
			}else{
				System.out.println("Testing eight threads...");
				System.out.println("Execution time 10i/80d/10c: " + test8Threads(test,.1,.8) + " ms");
				System.out.println("Execution time 80i/10d/10c: " + test8Threads(test,.8,.1) + " ms");
				System.out.println("Execution time 10i/10d/80c: " + test8Threads(test,.1,.1) + " ms");
			}
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	static long test1Thread(ConcurrentWaitFreeLinkedList4 test,double dist1, double dist2)
	{
		long startTime = System.currentTimeMillis();
		
		for (int i=0;i<NUM_TESTS;i++){
			double temp=Math.random();
			if (temp<=dist1)
				testInsert(test, 1,0);
			else if (temp<=dist2)
				testDelete(test, 1,0);
			else
				testContains(test, 1,0);
		}
		
		//testInsert(test, NUM_TESTS/2);
		//testDelete(test, NUM_TESTS/2);
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test2Threads(ConcurrentWaitFreeLinkedList4 test,double dist1, double dist2)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		Thread[] threads = new Thread[2];
		
		// First thread inserts
		threads[0] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
			}
				//testInsert(test, NUM_TESTS);
			}
		};
		
		threads[0].start();
		
		// Second thread deletes
		threads[1] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test4Threads(ConcurrentWaitFreeLinkedList4 test,double dist1, double dist2)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		Thread[] threads = new Thread[4];
		
		threads[0] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testInsert(test, NUM_TESTS);
			}
		};
		
		threads[0].start();
		
		threads[1] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		threads[2] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS/2);
				//testInsert(test, NUM_TESTS/2);
			}
		};
		
		threads[2].start();
		
		threads[3] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS/4);
				//testInsert(test, NUM_TESTS/4);
				//testDelete(test, NUM_TESTS/4);
				//testInsert(test, NUM_TESTS/4);
			}
		};
		
		threads[3].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test8Threads(ConcurrentWaitFreeLinkedList4 test,double dist1, double dist2)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
	
		Thread[] threads = new Thread[8];
		
		threads[0] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testInsert(test, NUM_TESTS);
			}
		};
		
		threads[0].start();
		
		threads[1] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testInsert(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		threads[2] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testInsert(test, NUM_TESTS);
			}
		};
		
		threads[2].start();
		
		threads[3] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testInsert(test, NUM_TESTS);
			}
		};
		
		threads[3].start();
		
		threads[4] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[4].start();
		
		threads[5] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[5].start();
		
		threads[6] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[6].start();
		
		threads[7] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					double temp=Math.random();
					if (temp<=dist1)
						testInsert(test, 1,0);
					else if (temp<=dist2)
						testDelete(test, 1,0);
					else
						testContains(test, 1,0);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[7].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	static void simple1(ConcurrentWaitFreeLinkedList4 tests){
		System.out.println(tests);
		tests.insert(10);
		System.out.println(tests);
		tests.insert(23);
		System.out.println(tests);
		tests.insert(5);
		tests.insert(6);
		System.out.println(tests);
		//tests.insert(17);
		tests.delete(5);
		System.out.println(tests);
		//tests.insert(5);
		//tests.insert(23);
		//tests.insert(10);
		//tests.insert(11);
		if (tests.contains(23))
			System.out.println("ye");
		else
			System.out.println("na");
		
		System.out.println(tests);
	}
	
	// Inserts random values n times
	static void testInsert(ConcurrentWaitFreeLinkedList4 tests, int n, int thr)
	{
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.insert(random.nextInt(RAND_MAX));
	}
	
	// Deletes n random values
	static void testDelete(ConcurrentWaitFreeLinkedList4 tests, int n, int thr)
	{
		//testContains(tests, n, thr);
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.delete(random.nextInt(RAND_MAX));
	}
	
	// checks for n random values
	static void testContains(ConcurrentWaitFreeLinkedList4 tests, int n, int thr)
	{
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.contains(random.nextInt(RAND_MAX));
	}
	
}