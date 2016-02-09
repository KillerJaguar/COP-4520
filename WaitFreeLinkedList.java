public class WaitFreeLinkedList<E extends Comparable<E>>
{
	class Node
	{
		E item;
		Node next;
		
		// To signify that the entry in the node is logically deleted
		boolean markBit;
		
		public Node(E item, Node next)
		{
			this.item = item;
			this.next = next;
			this.markBit = false;
		}
	}
	
	enum Operation
	{
		INSERT,
		DELETE,
	}
	
	Node head, tail;
	
	int[] state;
	int phaseNumber;
	
	public WaitFreeLinkedList(int numThreads)
	{
		this.head = null;
		this.tail = null;
		
		state = new int[numThreads];
		phaseNumber = 0;
	}
	
	public void insert(E item)
	{
		// Base implementation, non-threaded
		
		if (head == null)
		{
			head = new Node(item, null);
			return;
		}
		
		Node iter = head, prev = null;
		while (iter != null)
		{
			int compare = item.compareTo(iter.item);
			
			// Same value, item already exists, nothing else needed
			if (compare == 0)
				return;
			
			// Less than current, so goes behind current
			else if (compare < 0)
			{
				if (prev != null)
					prev.next = new Node(item, iter.next);
				
				else 
					head = new Node(item, head);
				
				return;
			}
			
			prev = iter;
			iter = iter.next;
		}
		
		// Insert at tail
		prev.next = new Node(item, null); 
	}
	
	public void delete(E item)
	{
		// Base implementation, non-threaded
		
		Node iter = head, prev = null;
		while (iter != null)
		{
			if (item.compareTo(iter.item) == 0)
			{
				// prev will only be null if item is the head
				if (prev != null)
					prev.next = iter.next;
				
				// prev == null means it is at the head
				else
					head = head.next;
				
				return;
			}
			
			prev = iter;
			iter = iter.next;
		}
	}
	
	public boolean contains(E item)
	{
		// Base implementation, non-threaded
		
		Node iter = head;
		while (iter != null)
		{
			if (item.compareTo(iter.item) == 0)
				return true;
			
			iter = iter.next;
		}
		
		return false;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder("[");
		for (Node iter = head; iter != null; iter = iter.next)
		{
			sb.append(iter.item.toString());
			if (iter.next != null)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
}