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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class ConcurrentWaitFreeLinkedList<E>
	implements WaitFreeLinkedList<E>
{
	class Node
	{
		final E item;
		AtomicMarkableReference<Node> next;
		
		public Node(E item)
		{
			this.item = item;
			this.next = new AtomicMarkableReference<Node>(null, true);
		}
		
		public Node(E item, AtomicMarkableReference<Node> next)
		{
			this.item = item;
			this.next = new AtomicMarkableReference<Node>(next.getReference(), next.isMarked());
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
	
	final Node head;
	//AtomicReference<Node> tail;
	
	// ID of executing thread
	ThreadLocal<Integer> threadId;
	AtomicInteger nextId;
	
	// State array of operations for each thread
	State[] threadState;
	
	// Current highest phase number 
	AtomicLong phaseNumber;
	
	@SuppressWarnings("unchecked")
	public ConcurrentWaitFreeLinkedList(int numThreads)
	{
		this.head = new Node(null);
		//this.tail = new AtomicReference<Node>(new Node(null));
		
		this.threadId = new ThreadLocal<Integer>();
		this.nextId = new AtomicInteger();
		
		this.threadState = (State[]) new Object[numThreads];
		this.phaseNumber = new AtomicLong(0);
	}
	
	public void insert(E item)
	{
		operate(Operation.INSERT, item);
	}
	
	public void delete(E item)
	{
		operate(Operation.DELETE, item);
	}
	
	public boolean contains(E item)
	{
		int hash = item.hashCode(), hashIter;
		Node iter = head.next.getReference();
		
		while (iter != null && (hashIter = iter.item.hashCode()) <= hash)
		{
			if (hash == hashIter && item.equals(iter.item))
				return iter.next.isMarked();
		}
		
		return false;
	}
	
	// WARNING not multi-threaded, only a debug function to ensure correctness
	public String toString()
	{
		StringBuilder sb = new StringBuilder("[");
		for (Node iter = head; iter != null; iter = iter.next.getReference())
		{
			sb.append(iter.item.toString());
			if (iter.next != null)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	int getThreadId()
	{
		Integer index = threadId.get();
		if (index == null)
			threadId.set(nextId.getAndIncrement());
		return index;
	}
	
	void operate(Operation operation, E item)
	{
		long phaseNumber = this.phaseNumber.getAndIncrement();
		threadState[getThreadId()] = new State(phaseNumber, operation, item);
		
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
	
	// WAIT-FREE
	void _insert(E item, AtomicBoolean success)
	{
		Node iter = head.next.getReference(), prev = head;
		
		while (iter != null)
		{
			Node next = iter.next.getReference();
			
			// If item is logically deleted, set and move to the next node
			// TODO confirm works -- mark bit should specify this node, not next
			if (prev.next.compareAndSet(iter, next, false, true))
			{
				iter = next;
				continue;
			}
			
			int compare = item.hashCode() - iter.item.hashCode();
			
			// If both hash and item are the same, element already exists
			// Conflicting hash values iterate until there is no conflict
			if (compare == 0 && item.equals(iter.item))
				return;
			
			// Iterator has larger value, so stop here
			else if (compare > 0)
				break;
			
			prev = iter;
			iter = iter.next.getReference();
		}
		
		// Linearization point
		if (success.compareAndSet(false, true))
			prev.next.set(new Node(item, prev.next), true);
	}
	
	void _delete(E item, AtomicBoolean success)
	{
		// search_delete
		
		/*Node iter = head, prev = null;
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
		}*/
	}
}