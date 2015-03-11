package cart.kulua;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cart.Cartifier;
import cart.io.InputFile;
import dm.cartification.rank.RankCartifier;

public class RankTiler
{
	private static PrintStream out;
	// private static PrintStream matrixOut = new PrintStream(new
	// NullOutputStream());
	static
	{
		try
		{
			out = new PrintStream(File.createTempFile("ranktiler-log", ".txt"));
		} catch (IOException e)
		{
			out = System.err;
			e.printStackTrace();
		}
	}

	// private int[] loc2Id;

	public static void main(String[] args) throws IOException
	{
		// final String fileName = "/home/memin/Private/research/data/synth/....";
		// final String fileName =
		// "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_20.mime";
		// int numOfItems = 20;
		// int theta = 4;
		final String fileName = "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_12.mime";
		int numOfItems = 12;
		int theta = 6;
		RankTiler tiler = new RankTiler(InputFile.forMime(fileName), numOfItems);
		// final String fileName =
		// "/home/memin/Private/workspace/MIME/data/2c2d/2c2d.txt";
		// final String fileName =
		// "/home/memin/research/data/synth/6c10d/6c10d.mime";
		// int numOfItems = 660;
		// int theta = 90;
		// topK = 1000;
		// int minSupport = 10;
		// final String fileName =
		// "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_20.mime";
		// int numOfItems = 12;
		// // int minSupport = 10;
		// int numOfCarts = 12;
		// topK = 1000;

		List<Tile> tiles = tiler.runFor(theta, 1000);

		// for (Tile tile : tiles)
		// {
		// System.out.println(tile);
		// }

		// System.out.println("Mine time:" + (mineComplete - conversionEnd)
		// + "ms, Total time: " + (mineComplete - start));
	}

	private InputFile input;
	private int numOfItems;

	public RankTiler(InputFile input, int numOfItems)
	{
		this.input = input;
		this.numOfItems = numOfItems;
	}

	List<Tile> runFor(int theta, int topK) throws IOException
	{
		out.println(theta);
		int numOfCarts = numOfItems;
		long start = System.currentTimeMillis();
		// InputFile input = InputFile.forMime(fileName);
		double[][] dataArr = input.getData().toArray(new double[0][]);

		SortedDb db = SortedDb.from(dataArr);

		Cartifier cartifier = new Cartifier(db.db);
		String filename = "/a/cartified.gz";
		cartifier.cartifyNumeric(new int[]
		{ 0 }, filename);

		int[][] carts = RankCartifier.readCarts(filename, numOfCarts);

		int[][] rankMat = RankCartifier.cartsToRankMatrix(numOfItems, carts);

		for (int rowIx = 0; rowIx < rankMat.length; rowIx++)
		{
			for (int cIx = 0; cIx < rankMat[rowIx].length; cIx++)
			{
				rankMat[rowIx][cIx] -= theta;
			}
		}
		printMatrix(rankMat, out);

		long conversionEnd = currentTimeMillis();
		List<Tile> tiles = new TopKList<Tile>(topK);
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
						// int score = tileSum - (ec - sc + 1) * (er - sr + 1) * theta;
						int score = tileSum;
						Tile tile = newTile(score, sr, sc, er, ec);

						tiles.add(tile);
					}
				}
			}
		}

		long mineComplete = System.currentTimeMillis();
		// printMatrix(rankMat);
		out.flush();
		return tiles;
	}

	private static List<List<Double>> arr2List(double[][] dataArr)
	{
		List<List<Double>> db = new ArrayList<>();
		for (double[] row : dataArr)
		{
			List<Double> objList = new ArrayList<>(row.length);
			for (double val : row)
			{
				objList.add(val);
			}
			db.add(objList);
		}
		return db;
	}

	static void printMatrix(int[][] rankMat, PrintStream matrixOut)
	{
		for (int[] row : rankMat)
		{
			for (int v : row)
			{
				// System.out.printf("%2d ", loc2Id[v]);
				matrixOut.printf("%3d", v);
			}
			matrixOut.println();
		}
	}

	// static int addToTiles(Tile tile, List<Tile> tiles)
	// {
	// if (tiles.size() == 0)
	// {
	// tiles.add(tile);
	// return 0;
	// }
	//
	// // if (tile.score >= tiles.get(tiles.size() - 1).score)
	// // {
	// // if (tiles.size() == topK)
	// // {
	// // return -1;
	// // }
	// // tiles.add(tile);
	// // return tiles.size()-1;
	// // }
	//
	// int ix = Collections.binarySearch(tiles, tile);
	// int insertPoint = ix;
	// if (ix < 0)
	// {
	// insertPoint = -ix - 1;
	// }
	// if (insertPoint == tiles.size())
	// {
	// return -1;
	// }
	// if (tiles.size() == topK)
	// {
	// tiles.remove(tiles.size() - 1);
	// }
	// tiles.add(insertPoint, tile);
	// return insertPoint;
	// }

	static Tile newTile(int score, int sr, int sc, int er, int ec)
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
		// Tile tile = new Tile(score, rows, cols);
		Tile tile = new Tile(score, sr, sc, er, ec);
		return tile;
	}

	static Set<Integer> findCoveringTiles(Collection<Tile> tiles, List<Tile> coverTiles)
	{
		Set<Integer> cover = new HashSet<>();
		nextTile:
		for (Tile tile : tiles)
		{
			for (int col : tile.cols())
			{
				if (cover.contains(col))
				{
					continue nextTile;
				}
			}
			coverTiles.add(tile);
			for (int col : tile.cols())
			{
				cover.add(col);
			}
		}
		return cover;
	}
}
