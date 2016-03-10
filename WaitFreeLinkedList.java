public interface WaitFreeLinkedList<E>
{
	void insert(E item);
	void delete(E item);
	boolean contains(E item);
}