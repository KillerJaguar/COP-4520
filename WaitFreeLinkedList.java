public interface WaitFreeLinkedList<E extends Comparable<E>>
{
	void insert(E item);
	void delete(E item);
	boolean contains(E item);
}