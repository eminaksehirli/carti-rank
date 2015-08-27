package be.uantwerpen.adrem.cart.model;

import java.util.Arrays;
import java.util.List;

import be.uantwerpen.adrem.cart.model.Dissimilarity;
import be.uantwerpen.adrem.cart.model.Obj;
import be.uantwerpen.adrem.cart.model.Pair;

public class RankCartifier
{
	protected Obj[] db;
	protected Dissimilarity dist;

	public static RankCartifier newCartifier(Obj[] db, Dissimilarity dist)
	{
		return new RankCartifier(db, dist);
	}

	public static RankCartifier newCartifier(List<List<Double>> db,
			Dissimilarity dist)
	{
		Obj[] objDb = new Obj[db.size()];
		int ix = 0;
		for (List<Double> obj : db)
		{
			double[] objArr = new double[obj.size()];
			for (int i = 0; i < objArr.length; i++)
			{
				objArr[i] = obj.get(i);
			}
			objDb[ix] = new Obj(ix, objArr);
			ix++;
		}
		return newCartifier(objDb, dist);
	}

	private RankCartifier(Obj[] db, Dissimilarity dist)
	{
		this.db = db;
		this.dist = dist;
	}

	public int[][] getRankMat()
	{
		int numOfItems = db.length;
		int[][] rankMat = new int[numOfItems][numOfItems];

		for (int i = 0; i < numOfItems; i++)
		{
			Pair[] distances = new Pair[numOfItems];
			for (int j = 0; j < numOfItems; j++)
			{
				distances[j] = new Pair(dist.between(db[i], db[j]), j);
			}

			Arrays.sort(distances);

			for (Pair neighbor : distances)
			{
				for (int cartOrder = 0; cartOrder < numOfItems; cartOrder++)
				{
					rankMat[i][distances[cartOrder].ix] = cartOrder;
				}
			}
		}
		return rankMat;
	}
}
