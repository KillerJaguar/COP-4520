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
	ThreadId threadId;
	AtomicInteger nextId;
	
	// State array of operations for each thread
	List<State> threadState;
	
	// Current highest phase number 
	AtomicLong phaseNumber;
	int threadCount;
	
	@SuppressWarnings("unchecked")
	public ConcurrentWaitFreeLinkedList4(int numThreads)
	{
		threadCount=numThreads;
		this.tail = new Node(null, null);
		this.head = new Node(null, tail);
		
		this.threadId = new ThreadId();
		
		
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
		//int hash = item.hashCode(), hashIter;
		//Node iter = head.next.getReference();
		Window window = find2(item);
		if (window==null)
				return false;
		if (!window.pred.toString().equals("null")){
			//System.out.println("w"+window.pred.toString());
			if (window.pred.item.equals(item))
				return true;
		}
		if (!window.curr.toString().equals("null")){
			//System.out.println("w"+window.curr.toString());
			if (window.curr.item.equals(item))
				return true;
		}
		
		/*while (iter != null && (hashIter = iter.item.hashCode()) <= hash)
		{
			if (hash == hashIter && item.equals(iter.item))
				return !iter.next.isMarked();
		}*/
		
		return false;
	}
	
	// WARNING not multi-threaded, only a debug function to ensure correctness
	public String toString()
	{
		//if (head.equals(null))
		//return "ded";
		
		
		StringBuilder sb = new StringBuilder("[");
		for (Node iter = head; iter != null; iter = iter.next.getReference())
		{
			if (!iter.equals(tail))
			{
			sb.append(iter.toString());
			if (iter.next.toString() != "null")
			sb.append(", ");
			}else
				sb.append("null");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	void operate(Operation operation, E item)
	{
		long phaseNumber = this.phaseNumber.getAndIncrement();
		threadState.set(threadId.getId(), new State(phaseNumber, operation, item));
		
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
			//System.out.println("y");
			Window window = find(item);
			if (window==null)
				return false;
			Node pred = window.pred, curr = window.curr;
			if (item==null)
			return false;
			// Item already exists
			if(  item.toString().equals(curr.toString()) || item.toString().equals(pred.toString())    )
			//if (item.hashCode().equals(curr.item.hashCode())) 
			
				return false;
			
			//System.out.println(item.toString()+" "+curr.toString()+" "+pred.toString());
			
			// Create new node
			Node node = new Node(item, curr);
			
			// Install new node, else retry loop
			if (!success.get() && pred.next.compareAndSet(curr, node, false, false))
			{
				success.compareAndSet(false, true);
				//System.out.println("succeeded at "+item.toString());
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
			Window window = find2(item);
			
			if (window==null)
				return false;
				//System.out.println("want "+ item.toString() +","+this.toString());
			Node curr = window.curr;
			Node pred = window.pred;
			
			
			
			// Item is not found
			if (pred.toString().equals("null"))
				return false;
			
			
			
			if (item.hashCode() != curr.item.hashCode() || !item.toString().equals(curr.toString())){
				//System.out.println("dd"+item.toString()+" "+pred.toString()+" "+curr.toString());
				return false;
			}
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
		long phase=threadState.get(threadId.getId()).phaseNumber;
		boolean [ ] marked ={ false }; 
		boolean snip;

		retry : while(true){
			Node prev = head;
			Node curr = prev.next.getReference(); 
			while(true){
				Node succ = curr.next.get(marked); 
				while(marked [0]){ 
					snip = prev.next.compareAndSet(curr, succ, false, false);
					State curr2 = threadState.get(threadId.getId());
					if (!(curr2.phaseNumber == phase && (curr2.operation == Operation.INSERT || curr2.operation == Operation.DELETE)))
						return null;
					if(! snip) 
						continue retry;
					curr = succ;
					succ = curr.next.get(marked); 
				}if (succ==null || succ.toString().equals("null"))
					return new Window(prev, curr);
				
				if (item.toString().equals(succ.toString()))
					return new Window(curr, succ);
				
				if(item.hashCode() < succ.item.hashCode()){
				return new Window(curr, succ);
				}
				prev = curr; curr = succ;
			}
		}

	}
	Window find2(E item)
	{


		long phase=threadState.get(threadId.getId()).phaseNumber;
		boolean [ ] marked ={ false }; 
		boolean snip;

		retry : while(true){
			Node prev = head;
			Node curr = prev.next.getReference();
			while(true){
				Node succ = curr.next.get(marked);
				while(marked [0]){
					snip = prev.next.compareAndSet(curr, succ, false, false);
					
					State curr2 = threadState.get(threadId.getId());
					if (!(curr2.phaseNumber == phase && (curr2.operation == Operation.INSERT || curr2.operation == Operation.DELETE)))
						return null;
					
					if(! snip) 
						continue retry; 
					curr = succ; 
					succ = curr.next.get(marked); 
				}if (succ==null || succ.toString().equals("null"))
					return new Window(prev, curr);
				
				
				if (item.toString().equals(curr.toString())){
					
					return new Window(prev, curr);
				}
			
				if(item.hashCode() < succ.item.hashCode()){
				return new Window(prev, curr);
				}
				prev = curr; curr = succ;
			}
		}

	}
	
	
	

	
	
	
}

class ThreadId
{
	AtomicInteger nextId = new AtomicInteger();
	ThreadLocal<Integer> threadId = new ThreadLocal<Integer>()
	{
		@Override
		protected Integer initialValue()
		{
			return nextId.getAndIncrement();
		}
	};
	
	public int getId()
	{
		return nextId.get();
	}
}