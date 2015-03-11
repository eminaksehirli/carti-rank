package cart.kulua;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.io.NullOutputStream;

import cart.Cartifier;
import cart.io.InputFile;
import dm.cartification.rank.RankCartifier;

public abstract class RankTiler
{
	protected PrintStream out;

	protected int numOfItems;
	protected InputFile input;

	public RankTiler(InputFile input, int numOfItems)
	{
		this.input = input;
		this.numOfItems = numOfItems;
		initializeLogOut();
	}

	protected Collection<Tile> runFor(int theta, int topK) throws IOException
	{
		out.println(theta);
		int numOfCarts = numOfItems;
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
		// RankTiler.printMatrix(rankMat, out);

		// int topK = 1000;
		// List<Tile> tiles = new TopKList<>(topK);
		List<Tile> allTiles = new TopKList<>(topK);

		for (int i = 0; i < topK; i++)
		{
			List<Tile> tiles = new TopKList<>(1);
			// Collection<Tile> tiles = new CoveringMinimals();
			// sc: start column, sr: start row, ec: end column, er: end row
			int psr = 0;
			int per = rankMat.length;
			findTiles(rankMat, tiles, psr, per);

			Tile tile = tiles.get(0);
			for (int r = tile.sr; r <= tile.er; r++)
			{
				for (int c = tile.sc; c <= tile.ec; c++)
				{
					rankMat[r][c] = rankMat.length * 3;
				}
			}

			allTiles.add(tile);
		}
		out.flush();
		return allTiles;
		// List<Tile> coverTiles = new ArrayList<>();
		// Set<Integer> cover = RankTiler.findCoveringTiles(tiles, coverTiles);

		// System.out.println("# Coverage: " + cover.size() + " of " + numOfItems);
		// printMerge(mergeMap, coverTiles);
		// return coverTiles;
	}

	protected static Tile newTile(int score, int sr, int sc, int er, int ec)
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

	protected static Set<Integer> findCoveringTiles(Collection<Tile> tiles,
			List<Tile> coverTiles)
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

	protected abstract void findTiles(int[][] rankMat, List<Tile> tiles, int psr,
			int per);

	private void initializeLogOut()
	{
		out = new PrintStream(new NullOutputStream());
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

}