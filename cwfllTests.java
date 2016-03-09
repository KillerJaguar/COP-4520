import java.util.Random;

public class cwfllTests
{
	static final int NUM_TESTS = 500000;
	static final int RAND_MAX  = 250000;
	
	public static void main(String[] args)
	{		
		cwfll5 test;
		
		test = new cwfll5(1);
		
		System.out.println("Testing one thread...");
		System.out.println("Execution time 25i/75d: " + test1Thread(test,.25) + " ms");
		System.out.println("Execution time 50i/50d: " + test1Thread(test,.5) + " ms");
		System.out.println("Execution time 75i/25d: " + test1Thread(test,.75) + " ms");
		
		test = new cwfll5(2);
		
		try
		{
			System.out.println("Testing two threads...");
			System.out.println("Execution time 25i/75d: " + test2Threads(test,.25) + " ms");
			System.out.println("Execution time 50i/50d: " + test2Threads(test,.5) + " ms");
			System.out.println("Execution time 75i/25d: " + test2Threads(test,.75) + " ms");
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
			
		test = new cwfll5(4);
		
		try
		{
			System.out.println("Testing four threads...");
			System.out.println("Execution time 25i/75d: " + test4Threads(test,.25) + " ms");
			System.out.println("Execution time 50i/50d: " + test4Threads(test,.5) + " ms");
			System.out.println("Execution time 75i/25d: " + test4Threads(test,.75) + " ms");
			
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
		
		test = new cwfll5(8);
		
		try
		{
			System.out.println("Testing eight threads...");
			System.out.println("Execution time 25i/75d: " + test8Threads(test,.25) + " ms");
			System.out.println("Execution time 50i/50d: " + test8Threads(test,.5) + " ms");
			System.out.println("Execution time 75i/25d: " + test8Threads(test,.75) + " ms");
		}
		catch (InterruptedException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	static long test1Thread(cwfll5 test,double dist)
	{
		long startTime = System.currentTimeMillis();
		
		for (int i=0;i<NUM_TESTS;i++){
			if (Math.random()<dist)
				testInsert(test, 1,0);
			else
				testDelete(test, 1,0);
		}
		
		//testInsert(test, NUM_TESTS/2);
		//testDelete(test, NUM_TESTS/2);
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test2Threads(cwfll5 test,double dist)
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
			if (Math.random()<dist)
				testInsert(test, 1,0);
			else
				testDelete(test, 1,0);
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
					if (Math.random()<dist)
						testInsert(test, 1,1);
					else
						testDelete(test, 1,1);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[1].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	static long test4Threads(cwfll5 test,double dist)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		Thread[] threads = new Thread[4];
		
		threads[0] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					if (Math.random()<dist)
						testInsert(test, 1,0);
					else
						testDelete(test, 1,0);
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
					if (Math.random()<dist)
						testInsert(test, 1,1);
					else
						testDelete(test, 1,1);
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
					if (Math.random()<dist)
						testInsert(test, 1,2);
					else
						testDelete(test, 1,2);
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
					if (Math.random()<dist)
						testInsert(test, 1,3);
					else
						testDelete(test, 1,3);
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
	
	static long test8Threads(cwfll5 test,double dist)
		throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
	
		Thread[] threads = new Thread[8];
		
		threads[0] = new Thread()
		{
			public void run()
			{
				for (int i=0;i<NUM_TESTS/2;i++){
					if (Math.random()<dist)
						testInsert(test, 1,0);
					else
						testDelete(test, 1,0);
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
					if (Math.random()<dist)
						testInsert(test, 1,1);
					else
						testDelete(test, 1,1);
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
					if (Math.random()<dist)
						testInsert(test, 1,2);
					else
						testDelete(test, 1,2);
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
					if (Math.random()<dist)
						testInsert(test, 1,3);
					else
						testDelete(test, 1,3);
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
					if (Math.random()<dist)
						testInsert(test, 1,4);
					else
						testDelete(test, 1,4);
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
					if (Math.random()<dist)
						testInsert(test, 1,5);
					else
						testDelete(test, 1,5);
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
					if (Math.random()<dist)
						testInsert(test, 1,6);
					else
						testDelete(test, 1,6);
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
					if (Math.random()<dist)
						testInsert(test, 1,7);
					else
						testDelete(test, 1,7);
				}
				//testDelete(test, NUM_TESTS);
			}
		};
		
		threads[7].start();
		
		for (Thread t : threads)
			t.join();
		
		return System.currentTimeMillis() - startTime;
	}
	
	// Inserts random values n times
	static void testInsert(cwfll5 tests, int n, int thr)
	{
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.insert(random.nextInt(RAND_MAX), thr);
	}
	
	// Deletes n random values
	static void testDelete(cwfll5 tests, int n, int thr)
	{
		Random random = new Random();
		
		for (int i = 0; i < n; i++)
			tests.delete(random.nextInt(RAND_MAX), thr);
	}
	
}