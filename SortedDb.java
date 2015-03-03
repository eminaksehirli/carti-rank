package cart.kulua;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedDb
{
	List<List<Double>> db;
	private int[] loc2Id;

	static SortedDb from(double[][] data)
	{
		return new SortedDb(data);
	}

	private SortedDb(double[][] data)
	{
		List<Pair> pairs = dataToPairs(data);
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
		// this.db = db;
	}

	static List<Pair> dataToPairs(double[][] dataArr)
	{
		List<Pair> pairs = new ArrayList<>(dataArr.length);
		int ix = 0;
		for (double[] obj : dataArr)
		{
			pairs.add(new Pair(ix, obj[0]));
			ix++;
		}
		Collections.sort(pairs);
		return pairs;
	}

	int[] loc2Ids(int[] arr)
	{
		int[] mapped = new int[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			mapped[i] = loc2Id[arr[i]];
		}
		return mapped;
	}
}
