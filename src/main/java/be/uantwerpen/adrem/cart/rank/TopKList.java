package be.uantwerpen.adrem.cart.rank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * A list implementation that sorts and holds only the first {@code k} elements.
 * Order of the items are determined by the {@code compare} method of {@code T}.
 * 
 * The elements that cannot make it to the top-k will be discarded during
 * {@link TopKList.add}.
 * 
 * @author Emin Aksehirli
 * 
 * @param <T>
 *          Element type of the list.
 */
public class TopKList<T extends Comparable<T>> implements RandomAccess,
		List<T>, Cloneable
{
	ArrayList<T> list;
	private int k;

	/**
	 * Checks whether the {@code e} can make it to the list. Inserts it to the
	 * correct location and returns {@code true} if it can. If it cannot the
	 * element is discarded and {@code false} is returned.
	 * 
	 * @return {@code true} if the element is added, {@code false} if it is not.
	 */
	@Override
	public boolean add(T e)
	{
		if (list.size() == 0)
		{
			list.add(e);
			return true;
		}

		int ix = Collections.binarySearch(list, e);
		int insertPoint = ix;
		if (ix < 0)
		{
			insertPoint = -ix - 1;
		}
		if (insertPoint == k)
		{
			return false;
		}
		if (list.size() == k)
		{
			list.remove(list.size() - 1);
		}
		list.add(insertPoint, e);
		return true;
	}

	/**
	 * Initialize a Top-{@code K} list. Elements that cannot make it to the top-k
	 * will be discarded.
	 * 
	 * @param k
	 *          The size of the list.
	 */
	public TopKList(int k)
	{
		this.k = k;
		list = new ArrayList<T>(k);
	}

	public void trimToSize()
	{
		list.trimToSize();
	}

	public void ensureCapacity(int minCapacity)
	{
		list.ensureCapacity(minCapacity);
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return list.containsAll(c);
	}

	@Override
	public int indexOf(Object o)
	{
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return list.lastIndexOf(o);
	}

	@Override
	public Object clone()
	{
		TopKList<T> nl = new TopKList<T>(k);
		nl.list = (ArrayList<T>) list.clone();
		return nl;
	}

	@Override
	public Object[] toArray()
	{
		return list.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a)
	{
		return list.toArray(a);
	}

	@Override
	public T get(int index)
	{
		return list.get(index);
	}

	@Override
	public String toString()
	{
		return list.toString();
	}

	@Override
	public T set(int index, T element)
	{
		return list.set(index, element);
	}

	@Override
	public void add(int index, T element)
	{
		list.add(index, element);
	}

	@Override
	public boolean equals(Object o)
	{
		return list.equals(o);
	}

	@Override
	public T remove(int index)
	{
		return list.remove(index);
	}

	@Override
	public boolean remove(Object o)
	{
		return list.remove(o);
	}

	@Override
	public int hashCode()
	{
		return list.hashCode();
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return list.retainAll(c);
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		return list.listIterator(index);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return list.listIterator();
	}

	@Override
	public Iterator<T> iterator()
	{
		return list.iterator();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		return list.subList(fromIndex, toIndex);
	}
}
