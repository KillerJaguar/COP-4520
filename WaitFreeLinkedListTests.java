import java.util.Random;

public class WaitFreeLinkedListTests
{
	static final int NUM_TESTS = 500000;
	static final int RAND_MAX = 1000000;
	
	public static void main(String[] args)
	{		
		WaitFreeLinkedList<Integer> test;
		
		test = new SequentialWaitFreeLinkedList<Integer>();
		
		System.out.println("Testing one thread...");
		System.out.println("Execution time: " + test1Thread(test) + " ms");
		
		test = new SequentialWaitFreeLinkedList<Integer>();
		
		try
		{
			System.out.println("Testing two threads...");
			System.out.println("Execution time: " + test2Threads(test) + " ms");
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
			
		test = new SequentialWaitFreeLinkedList<Integer>();
		
		try
		{
			System.out.println("Testing four threads...");
			System.out.println("Execution time: " + test4Threads(test) + " ms");
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
		
		test = new SequentialWaitFreeLinkedList<Integer>();
		
		try
		{
			System.out.println("Testing eight threads...");
			System.out.println("Execution time: " + test8Threads(test) + " ms");
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	static long test1Thread(WaitFreeLinkedList<Integer> test)
	{
		long startTime = System.currentTimeMillis();
		
		testInsert(test, NUM_TESTS/2);
		testDelete(test, NUM_TESTS/2);
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test2Threads(WaitFreeLinkedList<Integer> test)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		Thread[] threads = new Thread[2];
		
		// First thread inserts
		threads[0] = new Thread()
		{
			public void run()
			{
				testInsert(test, NUM_TESTS);
			}
		};
		
		threads[0].start();
		
		// Second thread deletes
		threads[1] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test4Threads(WaitFreeLinkedList<Integer> test)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		Thread[] threads = new Thread[4];
		
		threads[0] = new Thread()
		{
			public void run()
			{
				testInsert(test, NUM_TESTS);
			}
		};
		
		threads[0].start();
		
		threads[1] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		threads[2] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS/2);
				testInsert(test, NUM_TESTS/2);
			}
		};
		
		threads[2].start();
		
		threads[3] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS/4);
				testInsert(test, NUM_TESTS/4);
				testDelete(test, NUM_TESTS/4);
				testInsert(test, NUM_TESTS/4);
			}
		};
		
		threads[3].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test8Threads(WaitFreeLinkedList<Integer> test)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
	
		Thread[] threads = new Thread[8];
		
		threads[0] = new Thread()
		{
			public void run()
			{
				testInsert(test, NUM_TESTS);
			}
		};
		
		threads[0].start();
		
		threads[1] = new Thread()
		{
			public void run()
			{
				testInsert(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		threads[2] = new Thread()
		{
			public void run()
			{
				testInsert(test, NUM_TESTS);
			}
		};
		
		threads[2].start();
		
		threads[3] = new Thread()
		{
			public void run()
			{
				testInsert(test, NUM_TESTS);
			}
		};
		
		threads[3].start();
		
		threads[4] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS);
			}
		};
		
		threads[4].start();
		
		threads[5] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS);
			}
		};
		
		threads[5].start();
		
		threads[6] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS);
			}
		};
		
		threads[6].start();
		
		threads[7] = new Thread()
		{
			public void run()
			{
				testDelete(test, NUM_TESTS);
			}
		};
		
		threads[7].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	// Inserts random values n times
	static void testInsert(WaitFreeLinkedList<Integer> tests, int n)
	{
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.insert(random.nextInt(RAND_MAX));
	}
	
	// Deletes n random values
	static void testDelete(WaitFreeLinkedList<Integer> tests, int n)
	{
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.delete(random.nextInt(RAND_MAX));
	}
}