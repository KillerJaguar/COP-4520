import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class ConcurrentWaitFreeLinkedList4<E extends Comparable<E>>
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
		public String toString(){
			if (item!=null)
			return item.toString();
		else
			return "null";
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
	List<State> threadState;
	
	// Current highest phase number 
	AtomicLong phaseNumber;
	
	@SuppressWarnings("unchecked")
	public ConcurrentWaitFreeLinkedList4(int numThreads)
	{
		this.tail = new Node(null, null);
		this.head = new Node(null, tail);
		
		this.threadId = new ThreadLocal<Integer>();
		this.nextId = new AtomicInteger();
		
		this.threadState = new ArrayList<State>(numThreads);
		this.phaseNumber = new AtomicLong(0);
		
		for (int i = 0; i < numThreads; i++)
			threadState.add(null);
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
			if (!iter.equals(tail))
			{
				if (!iter.next.isMarked()){
			sb.append(iter.toString());
			if (iter.next.toString() != "null")
				sb.append(", ");}
		
			}else
				sb.append("null");
		}
		sb.append("]");
		return sb.toString();
	}
	
	int getThreadId()
	{
		Integer index = threadId.get();
		if (index == null)
			threadId.set(index = nextId.getAndIncrement());
		return index;
	}
	
	void operate(Operation operation, E item)
	{
		long phaseNumber = this.phaseNumber.getAndIncrement();
		threadState.set(getThreadId(), new State(phaseNumber, operation, item));
		
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
			System.out.println("y");
			Window window = find(item);
			System.out.println("n");
			Node pred = window.pred, curr = window.curr;
			
			// Item already exists
			if(item.equals(pred.item)){
				System.out.println("dupe");
			if (item.hashCode() == pred.item.hashCode()) 
			
				return false;
			}
			if(item.equals(curr.item)){
				System.out.println("dupe");
			if (item.hashCode() == curr.item.hashCode()) 
			
				return false;
			}
			
			// Create new node
			Node node = new Node(item, curr);
			
			// Install new node, else retry loop

			if (!success.get() && pred.next.compareAndSet(curr, node, false, false))
			{
				success.compareAndSet(false, true);
				System.out.println("succeeded at "+item.toString());
				return true;
			}
		}
		
		// The operation succeeded by another thread, therefore it is true 
		// that the item was inserted
		// OBJECTION it is possible the item was already inserted and therefore returned false
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
		// If list changes while traversed, start over
		retry: while(true)
		{
			// Start looking from head
			Node pred = head;
			Node curr = pred.next.getReference();
			
			if (curr.equals(tail)){
				//System.out.println("a");
			return new Window(head, tail);
			}
			
			// Move down the list
			while (true)
			{
				boolean[] marked = {false};
				Node succ = curr.next.get(marked);
				System.out.println("saw "+curr.toString());
				while (marked[0])
				{
					
					// Try to snip out node
					// If predecessor's next field changed, must retry whole traversal
					if (!pred.next.compareAndSet(curr, succ, false, false))
						continue retry;
					
					curr = succ;
					succ = curr.next.get(marked);
					
				}
				
				
				//System.out.println(pred.item);
				if (succ.toString().equals("null"))
				return new Window(curr, tail);
				
				if (item.hashCode() >= succ.item.hashCode() || item.equals(succ.item))
					return new Window(pred, succ);
				
				pred = curr;
				curr = succ;
			}
		}
	}
	
	/*Window find(E item){
		
		Node curr = null; 
		Node prev = null;
		
		boolean [ ] marked ={ false }; 
		boolean snip;
		// If list changes while traversed, start over
		while(true){
			System.out.println("q");
			//System.out.println(item);
			// Start looking from head
			prev = head;
			curr = prev.next.getReference();
			int broken=0;
			
			// Move down the list
			while (true)
			{
				System.out.println("r");
				//boolean[] marked = {false};
				Node succ = curr.next.get(marked);

				while (marked[0])
				{
					System.out.println("s");
					// Try to snip out node
					// If predecessor's next field changed, must retry whole traversal
					if (!prev.next.compareAndSet(curr, succ, false, false)){
						broken=1; 
						break;
					}
						
					
					curr = succ;
					succ = curr.next.get(marked);
				}
				
				if (broken==1){
					break;
				}
				if (curr.equals(null))
				System.out.println("a");
				if (succ.equals(null))
				System.out.println("b");
				if (prev.equals(null))
				System.out.println("c");
				
				if (succ.equals(null))
					return new Window(prev, curr);
				if (curr.item.compareTo(item)>=0)
					return new Window(prev, curr);
				System.out.println("u");
				
				prev = curr;
				curr = succ;
			}
		}
		
	}*/
	
}