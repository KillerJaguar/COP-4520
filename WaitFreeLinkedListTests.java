public class WaitFreeLinkedListTests
{
	public static void main(String[] args)
	{
		WaitFreeLinkedList<Integer> test;
		test = new SequentialWaitFreeLinkedList<Integer>();
		
		// Insert
		
		test.insert(5);
		test.insert(3);
		test.insert(10);
		test.insert(13);
		test.insert(1);
		test.insert(-1);
		test.insert(6);
		test.insert(18);
		
		System.out.println(test.toString());

		// Insert duplicate
		
		test.insert(10);
		test.insert(5);
		
		System.out.println(test.toString());
		
		// Delete
		
		test.delete(-1);
		test.delete(19);
		test.delete(13);
		test.delete(5);
		
		System.out.println(test.toString());
		
		// Contains
		
		System.out.println("test.contains(5) = " + test.contains(5));
		System.out.println("test.contains(-1) = " + test.contains(-1));
		System.out.println("test.contains(6) = " + test.contains(6));
		System.out.println("test.contains(18) = " + test.contains(18));
	}
}