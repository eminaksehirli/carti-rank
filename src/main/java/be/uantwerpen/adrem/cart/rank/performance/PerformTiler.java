package cart.kulua.performance;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PerformTiler
{
	private static int topK;

	public static void main(String[] args) throws FileNotFoundException
	{
		// final String fileName = "/home/memin/Private/research/data/synth/....";
		// final String fileName =
		// "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_20.mime";
		// final String fileName =
		// "/home/memin/Private/workspace/MIME/data/2c2d/2c2d.txt";
		// int numOfItems = 200;
		// // int minSupport = 10;
		// int numOfCarts = numOfItems;
		// int theta = 90;
		// final String fileName =
		// "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_20.mime";
		// int numOfItems = 12;
		// // int minSupport = 10;
		// int numOfCarts = 12;
		// topK = 1000;

		// long start = System.currentTimeMillis();
		// InputFile input = InputFile.forMime(fileName);
		// double[][] dataArr = input.getData().toArray(new double[0][]);
		//
		// List<Pair> pairs = dataToPairs(dataArr);
		//
		// List<List<Double>> db = toSortedDb(dataArr, pairs);
		//
		// Cartifier cartifier = new Cartifier(db);
		// String filename = "/a/cartified.gz";
		// cartifier.cartifyNumeric(new int[]
		// { 0 }, filename);
		//
		// int[][] carts = RankCartifier.readCarts(filename, numOfCarts);
		//
		// int[][] rankMat = RankCartifier.cartsToRankMatrix(numOfItems, carts);

		String filename = "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/data200.ssv";
		topK = 1000;
		int theta = 100;

		List<int[]> dataList = new ArrayList<>();
		Scanner sca = new Scanner(new File(filename));

		while (sca.hasNextLine())
		{
			String l = sca.nextLine();
			String[] arr = l.split(" ");
			int[] obj = new int[arr.length];
			for (int i = 0; i < arr.length; i++)
			{
				obj[i] = Integer.parseInt(arr[i]);
			}
			dataList.add(obj);
		}

		int[][] rankMat = dataList.toArray(new int[][] {});

		long conversionEnd = currentTimeMillis();
		List<Tile> tiles = new ArrayList<>(topK);
		// sc: start column, sr: start row, ec: end column, er: end row
		int psr = 0;
		int per = rankMat.length;
		for (int sr = psr; sr < per; sr++)
		{
			for (int sc = 0; sc < rankMat[sr].length; sc++)
			{
				for (int er = sr; er < rankMat.length; er++)
				{
					int tileSum = 0;
					for (int ec = sc; ec < rankMat[er].length; ec++)
					{
						for (int r = sr; r <= er; r++)
						{
							tileSum += rankMat[r][ec];
						}
						int score = tileSum - (ec - sc + 1) * (er - sr + 1) * theta;
						addToTiles(score, sr, sc, er, ec, tiles);
					}
				}
			}
		}

		long mineComplete = currentTimeMillis();
//		for (int[] row : rankMat)
//		{
//			for (int v : row)
//			{
//				// System.out.printf("%2d ", loc2Id[v]);
//				System.out.printf("%2d ", v);
//			}
//			System.out.println();
//		}

		System.out.println("Mine time:" + (mineComplete - conversionEnd) + "ms");

		for (Tile tile : tiles)
		{
			System.out.println(tile);
		}

	}

	static void addToTiles(int score, int sr, int sc, int er, int ec,
			List<Tile> tiles)
	{
		// int[] rows = new int[er - sr + 1];
		// for (int r = sr; r <= er; r++)
		// {
		// rows[r - sr] = r;
		// }
		// int[] cols = new int[ec - sc + 1];
		// for (int c = sc; c <= ec; c++)
		// {
		// cols[c - sc] = c;
		// }
		Tile tile = new Tile(score, sr, sc, er, ec);
		if (tiles.size() == 0)
		{
			tiles.add(tile);
			return;
		}

		if (score >= tiles.get(tiles.size() - 1).score)
		{
			if (tiles.size() == topK)
			{
				return;
			}
			tiles.add(tile);
			return;
		}

		int ix = Collections.binarySearch(tiles, tile);
		int insertPoint = ix;
		if (ix < 0)
		{
			insertPoint = -ix - 1;
		}
		if (insertPoint == tiles.size())
		{
			return;
		}
		if (tiles.size() == topK)
		{
			tiles.remove(tiles.size() - 1);
		}
		tiles.add(insertPoint, tile);
	}

	// static List<List<Double>> toSortedDb(double[][] dataArr, List<Pair> pairs)
	// {
	// loc2Id = new int[dataArr.length];
	// List<List<Double>> db = new ArrayList<>(dataArr.length);
	// int ix = 0;
	// for (Pair pair : pairs)
	// {
	// loc2Id[ix] = pair.ix;
	// List<Double> objList = new ArrayList<>(dataArr[ix].length);
	// for (double v : dataArr[pair.ix])
	// {
	// objList.add(v);
	// }
	// db.add(objList);
	// ix++;
	// }
	// return db;
	// }
	//
	// static List<Pair> dataToPairs(double[][] dataArr)
	// {
	// List<Pair> pairs = new ArrayList<>(dataArr.length);
	// int ix = 0;
	// for (double[] obj : dataArr)
	// {
	// pairs.add(new Pair(ix, obj[0]));
	// ix++;
	// }
	// Collections.sort(pairs);
	// return pairs;
	// }

	static class Tile implements Comparable<Tile>
	{
		double score;
		// int[] rows, cols;
		int sr, sc, er, ec;

		// Tile(double score, int[] rows, int[] cols)
		// {
		// this.score = score;
		// this.rows = rows;
		// this.cols = cols;
		// }

		@Override
		public int compareTo(Tile o)
		{
			return Double.compare(score, o.score);
		}

		// @Override
		// public String toString()
		// {
		// return score + ", " + Arrays.toString(loc2Ids(rows)) + "] ["
		// + Arrays.toString(loc2Ids(cols));
		// }

		// private int[] loc2Ids(int[] arr)
		// {
		// int[] mapped = new int[arr.length];
		// for (int i = 0; i < arr.length; i++)
		// {
		// mapped[i] = loc2Id[arr[i]];
		// }
		// return mapped;
		// }

		public Tile(double score, int sr, int sc, int er, int ec)
		{
			this.score = score;
			this.sr = sr;
			this.sc = sc;
			this.er = er;
			this.ec = ec;
		}

		@Override
		public String toString()
		{
			return score + ", " + sr + ", " + sc + ", " + er + ", " + ec;
		}
	}

	// static class Pair implements Comparable<Pair>
	// {
	// int ix;
	// double v;
	//
	// public Pair(Integer ix, Double v)
	// {
	// this.ix = ix;
	// this.v = v;
	// }
	//
	// @Override
	// public int compareTo(Pair o)
	// {
	// return Double.compare(this.v, o.v);
	// }
	//
	// @Override
	// public String toString()
	// {
	// return "{" + this.ix + ", " + this.v + "}";
	// }
	// }
}