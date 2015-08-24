package be.uantwerpen.adrem.cart.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedDb
{
	List<List<Double>> db;
	private int[] loc2Id;
	int dimIx;

	static SortedDb from(double[][] data)
	{
		return from(data, 0);
	}

	static SortedDb from(double[][] data, int dimIx)
	{
		return new SortedDb(data, dimIx);
	}

	private SortedDb(double[][] data, int dimIx)
	{
		this.dimIx = dimIx;
		List<Pair> pairs = dataToPairs(data, dimIx);
		toSortedDb(data, pairs);
	}

	private void toSortedDb(double[][] dataArr, List<Pair> pairs)
	{
		loc2Id = new int[dataArr.length];
		db = new ArrayList<>(dataArr.length);
		int ix = 0;
		for (Pair pair : pairs)
		{
			loc2Id[ix] = pair.ix;
			List<Double> objList = new ArrayList<>(dataArr[ix].length);
			for (double v : dataArr[pair.ix])
			{
				objList.add(v);
			}
			db.add(objList);
			ix++;
		}
	}

	private static List<Pair> dataToPairs(double[][] dataArr, int dimIx)
	{
		List<Pair> pairs = new ArrayList<>(dataArr.length);
		int ix = 0;
		for (double[] obj : dataArr)
		{
			pairs.add(new Pair(ix, obj[dimIx]));
			ix++;
		}
		Collections.sort(pairs);
		return pairs;
	}

	protected int[] loc2Ids(int[] arr)
	{
		int[] mapped = new int[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			mapped[i] = loc2Id[arr[i]];
		}
		return mapped;
	}

	public int[] loc2Ids(int sr, int er)
	{
		int[] mapped = new int[er - sr + 1];
		for (int i = sr; i <= er; i++)
		{
			mapped[i - sr] = loc2Id[i];
		}
		return mapped;
	}
}
