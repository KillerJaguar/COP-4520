import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.Semaphore;//just in case
import java.util.concurrent.TimeUnit;

enum Operation
{
		NONE,
		INSERT,
		SDELETE,
		EDELETE,
		CONTAINS,
}
	
public class cwfll2 {
	
	// State array of operations for each thread
	State[] threadState;
	
	
	// Map to link thread to state array
	ConcurrentMap<Thread, Integer> threadMap;
	Thread[] threads;
	
	// Current highest phase number 
	AtomicLong phaseNumber;
	
	//shouldn't all of these be atomic?
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
	
	
	long startTime;
	
	public static void main(String[] args) throws Exception{
	
		int numThreads=4;//decide how many, somehow
		Thread[] threads=new Thread[numThreads];
		threadMap = new ConcurrentHashMap<Thread, Integer>(numThreads);
		threadState = (State[]) new Object[numThreads];
		startTime = System.currentTimeMillis();
		
		ConcurrentWaitFreeLinkedList list= new ConcurrentWaitFreeLinkedList<Integer>;
		
		startTime = System.currentTimeMillis();
		
		phaseNumber=new AtomicLong(0);
		
		temp=0;
		while (temp<threadMap.size()){
			
			//initialise the thread state array
			threadState[threadId] = new State((long)0, (Operation)NONE, 0);
			
			//make the threads and put them in the map
			threads[temp]=new Thread(new RunnableThread("thread"+temp,temp,startTime, phaseNumber),"thread"+temp);
			threadMap.put(threads[temp]);
			
			temp+=1;
		}
	
	
	
	
	
	
	
	
	}
	
	
	
}

class RunnableThread implements Runnable{
	
	String name;
	long startTime;
	Atomiclong phaseNumber;
	int index;

	Thread runner;
	public RunnableThread() {
	}
	
	public RunnableThread(String threadName,int tin, long start, AtomicLong phase) {
		name=threadName;
		startTime=start;
		index=tin;
		phaseNumber = phase;
        runner = new Thread(this, threadName);   // (1) Create a new thread.

        runner.start();                          // (2) Start the thread.
	}
	
	
	
	public void run(){
		while (true)//for now, just do this forever. will need to change that later
		{
			for (int i=0;i<threadState.size();i++)//we need i because we need to know which thread it is that we are helping
			{
				state=threadState[i];
				// No state exists, skip
				if (state == null)
					continue;
				
				// Higher phase number, skip
				if (state.phaseNumber.get() > phaseNumber.get())
					continue;
				
				// Operation already completed. skip
				if (state.success.get() || state.operation!=NONE)
					continue;
				
				// Perform operation
				switch (state.operation)
				{
					//as written, this won't work. we need to figure out how to give the thread the linked list so it can operate on it.
					//we also need to "properly report success or failure to each of the threads that initiated the operation"
					//and ensure for EDELETE, "only the threads that initiated the operation (and not the helping threads) compete on setting the additional success bit"
					case INSERT: _insert(item, state.success,threads[i]); 
						break;
					case SDELETE: _searchDelete(item, state.success,threads[i]); 
						break;
					case EDELETE: _executeDelete(item, state.success,threads[i]); 
						break;
					case CONTAINS: 
					//and we need to figure out how to return the result of this operation
					_contains(item, state.success,threads[i]); 
					break;
					
				}
			}
	}
			
			
			
			
			
			
			
	}
}

















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

	Node head, tail;

	@SuppressWarnings("unchecked")
	public ConcurrentWaitFreeLinkedList(int numThreads)
	{
		this.head = null;
		this.tail = null;
		
		this.threadId = 0;
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
	
	//this is a weird one because the thread that calls it MIGHT NOT be the one that finds the answer. 
	//This will need to be resolved. As it stands, this function will definitely not work as intended.
	public void contains(E item)
	{
		return operate(getThreadCurrentId(), Operation.CONTAINS, item);
	}
	
	//all this should do is mark the state information!
	//the thread needs to be looking for other threads to help at all times, whether or not it has been called upon to initiate an operation yet
	boolean operate(int threadId, Operation operation, E item)
	{
		//if we used getAndIncrement, it wouldn't be higher than the previous phase number on the first time!
		long phaseNum = this.phaseNumber.incrementAndGet();
		threadState[threadId] = new State(phaseNum, operation, item);
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
	
	
	//these 4 functions may need to be moved, but I left them here for now
	void _insert(E item, AtomicBoolean success,int threadIdHelping)
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
			if (compare == 0){
				success.compareAndSet(false, true);
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
	
	void _searchDelete(E item, AtomicBoolean success,int threadIdHelping)
	{
		Node iter = head, prev = null;
		while (iter != null)
		{
			if (item.compareTo(iter.item) == 0)
				return;
				break;
			
			prev = iter;
			iter = iter.next;
		}
		
		if (iter == null)
			return;
	}	
	
		
	void _executeDelete(E item, AtomicBoolean success,int threadIdHelping)
	{
		if (!iter.deleted.get())
		{
			//in this case, the flag needs to be set by the thread that first wanted it done, not by the one that actually completed it (unless they're the same thread)
			iter.deleted.set(true);
			
			if (prev != null)
				prev.next = iter.next;
			
			else
				head = head.next;
		}
	}
	
	boolean _contains(E item, AtomicBoolean success,int threadIdHelping)//incomplete
	{
		
		Node iter = head, prev = null;
		while (iter != null)
		{
			if (item.compareTo(iter.item) == 0){

				success.compareAndSet(false, true);
				return true;
				break;
			}
			
			prev = iter;
			iter = iter.next;
		}
		
		if (iter == null){
			success.compareAndSet(false, true);
			return false;
		}
		
		
	}
	
	
	
}