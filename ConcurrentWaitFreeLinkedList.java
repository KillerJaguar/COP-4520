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
		final AtomicMarkableReference<Node> next;
		
		public Node(E item, Node next)
		{
			this.item = item;
			this.next = new AtomicMarkableReference<Node>(next, false);
		}
	}
	
	class State
	{
		final long phaseNumber;
		final Operation operation;
		final E item;
		
		final AtomicBoolean success;
		
		public State(long phaseNumber, Operation operation, E item)
		{
			this.phaseNumber = phaseNumber;
			this.operation = operation;
			this.item = item;
			this.success = new AtomicBoolean(false);
		}
	}
	
	class Window
	{
		final Node pred, curr;
		
		public Window(Node pred, Node curr)
		{
			this.pred = pred;
			this.curr = curr;
		}
	}
	
	enum Operation
	{
		INSERT,
		DELETE,
	}
	
	final Node head, tail;
	
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
		this.tail = new Node(null, null);
		this.head = new Node(null, tail);
		
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
	
	// WAIT-FREE
	public boolean contains(E item)
	{
		int hash = item.hashCode(), hashIter;
		Node iter = head.next.getReference();
		
		while (iter != null && (hashIter = iter.item.hashCode()) <= hash)
		{
			if (hash == hashIter && item.equals(iter.item))
				return !iter.next.isMarked();
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
	
	// WAIT-FREE(?)
	boolean _insert(E item, AtomicBoolean success)
	{
		while (!success.get())
		{
			Window window = find(item);
			Node pred = window.pred, curr = window.curr;
			
			// Item already exists
			if (item.hashCode() == curr.item.hashCode() && item.equals(curr.item))
				return false;
			
			// Create new node
			Node node = new Node(item, curr);
			
			// Install new node, else retry loop
			if (!success.get() && pred.next.compareAndSet(curr, node, false, false))
			{
				success.compareAndSet(false, true);
				return true;
			}
		}
		
		// The operation succeeded by another thread, therefore it is true 
		// that the item was inserted
		return true;
	}
	
	// LOCK-FREE
	boolean _delete(E item, AtomicBoolean success)
	{
		while (true)
		{
			Window window = find(item);
			Node pred = window.pred, curr = window.curr;
			
			// Item is not found
			if (item.hashCode() != curr.item.hashCode() || !item.equals(curr.item))
				return false;
			
			Node succ = curr.next.getReference();
			
			// Try to mark node as deleted
			// If it doesn't work, just retry, otherwise job is done
			if (!curr.next.compareAndSet(succ, succ, false, true))
				continue;
			
			// Try to advance reference 
			// If we don't succeed, someone else did or will
			pred.next.compareAndSet(curr, succ, false, false);
			return true;
		}
		
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
	
	Window find(E item)
	{
		boolean snip;
		
		// If list changes while traversed, start over
		retry: while(true)
		{
			// Start looking from head
			Node pred = head;
			Node curr = pred.next.getReference();
			
			// Move down the list
			while (true)
			{
				boolean[] marked = {false};
				Node succ = curr.next.get(marked);
				
				while (marked[0])
				{
					// Try to snip out node
					// If predecessor's next field changed, must retry whole traversal
					if (!pred.next.compareAndSet(curr, succ, false, false))
						continue retry;
					
					curr = succ;
					succ = curr.next.get(marked);
				}
				
				
				if (item.hashCode() >= succ.item.hashCode() || item.equals(succ.item))
					return new Window(pred, succ);
				
				pred = curr;
				curr = succ;
			}
		}
	}
}