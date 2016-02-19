/*
To achieve wait-freedom, a helping mechanism is used. The
helping mechanism employs a special state array, with an entry for
each thread. When a thread wishes to perform an operation on the
list, it first chooses a phase number, higher than all phase numbers
previously selected, and posts an operation-descriptor in its state
array entry. The operation descriptor describes the operation it
wishes to perform and also contains the phase number. Next, the
threadgoes throughall thestatearray, andhelps performoperations
with smaller or equal phase numbers. This ensures wait-freedom:
a delayed operation eventually receives help from all threads and
soon completes.
*/

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentWaitFreeLinkedList<E extends Comparable<E>>
	implements WaitFreeLinkedList<E>
{
	class Node
	{
		E item;
		Node next;
		
		// Flag to signify that the node is logically deleted
		AtomicBoolean deleted;
		
		public Node(E item, Node next)
		{
			this.item = item;
			this.next = next;
			this.deleted = new AtomicBoolean(false);
		}
	}
	
	class State
	{
		final long phaseNumber;
		final Operation operation;
		final E item;
		
		AtomicBoolean success;
		
		public State(long phaseNumber, Operation operation, E item)
		{
			this.phaseNumber = phaseNumber;
			this.operation = operation;
			this.item = item;
			this.success = new AtomicBoolean(false);
		}
	}
	
	enum Operation
	{
		INSERT,
		DELETE,
	}
	
	Node head, tail;
	
	// Map to link thread to state array
	ConcurrentMap<Thread, Integer> threadMap;
	
	// ID of thread to add into the threadMap
	volatile int threadId;
	
	// State array of operations for each thread
	State[] threadState;
	
	// Current highest phase number 
	AtomicLong phaseNumber;
	
	@SuppressWarnings("unchecked")
	public ConcurrentWaitFreeLinkedList(int numThreads)
	{
		this.head = null;
		this.tail = null;
		
		this.threadId = 0;
		this.threadMap = new ConcurrentHashMap<Thread, Integer>(numThreads);
		this.threadState = (State[]) new Object[numThreads];
		this.phaseNumber = new AtomicLong(0);
	}
	
	public void insert(E item)
	{
		operate(getThreadCurrentId(), Operation.INSERT, item);
	}
	
	public void delete(E item)
	{
		operate(getThreadCurrentId(), Operation.DELETE, item);
	}
	
	public boolean contains(E item)
	{
		// WARNING What if item is inserted before the current node iterator?
		
		for (Node iter = head; iter != null; iter = iter.next)
		{
			if (item.compareTo(iter.item) == 0)
				return !iter.deleted.get();
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
	
	int getThreadCurrentId()
	{
		Thread current = Thread.currentThread();
		if (threadMap.containsKey(current))
			return threadMap.get(current);
		else
			return threadMap.put(current, threadId++);
	}
	
	void operate(int threadId, Operation operation, E item)
	{
		long phaseNumber = this.phaseNumber.getAndIncrement();
		threadState[threadId] = new State(phaseNumber, operation, item);
		
		for (State state : threadState)
		{
			// No state exists, skip
			if (state == null)
				continue;
			
			// Higher phase number, skip
			if (state.phaseNumber > phaseNumber)
				continue;
			
			// Operation already completed. skip
			if (state.success.get())
				continue;
			
			// Perform operation
			switch (state.operation)
			{
				case INSERT: _insert(item, state.success); break;
				case DELETE: _delete(item, state.success); break;
			}
		}
	}
	
	void _insert(E item, AtomicBoolean success)
	{
		if (head == null)
		{
			if (success.compareAndSet(false, true))
				head = new Node(item, null);
			
			return;
		}
		
		Node iter = head, prev = null;
		while (iter != null)
		{
			// Item is deleted, so proceed to next without changing the prev
			if (iter.deleted.get())
			{
				iter = iter.next;
				continue;
			}
			
			int compare = item.compareTo(iter.item);
			
			// Same value, item already exists, nothing else needed
			if (compare == 0)
				return;
			
			// Less than current, so goes behind current
			else if (compare < 0)
			{
				if (success.compareAndSet(false, true))
				{
					// WARNING What happens if previous was deleted?
					
					if (prev != null)
						prev.next = new Node(item, iter.next);
					
					else 
						head = new Node(item, head);
				}
				
				return;
			}
			
			prev = iter;
			iter = iter.next;
		}
		
		// Insert at tail
		if (success.compareAndSet(false, true))
			prev.next = new Node(item, null);
	}
	
	void _delete(E item, AtomicBoolean success)
	{
		// search_delete
		
		Node iter = head, prev = null;
		while (iter != null)
		{
			if (item.compareTo(iter.item) == 0)
				break;
			
			prev = iter;
			iter = iter.next;
		}
		
		if (iter == null)
			return;
		
		// execute_delete
		
		if (!iter.deleted.get())
		{
			iter.deleted.set(true);
			
			if (prev != null)
				prev.next = iter.next;
			
			else
				head = head.next;
		}
	}
}