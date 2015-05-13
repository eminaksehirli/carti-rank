package cart.kulua;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import be.uantwerpen.adrem.cart.io.InputFile;
import cart.Cartifier;

import com.google.common.io.NullOutputStream;

import dm.cartification.rank.RankCartifier;

public class RankTiler
{

	public static RankTiler Naive(InputFile input, int numOfItems, int dimIx)
	{
		return new RankTiler(input, numOfItems, dimIx, new RankNaiveTiler());
	}

	public static RankTiler Expander(InputFile input, int numOfItems, int dimIx)
	{
		return new RankTiler(input, numOfItems, dimIx, new RankExpander());
	}

	public static RankTiler SquareExpander(InputFile input, int numOfItems,
			int dimIx)
	{
		return new RankTiler(input, numOfItems, dimIx, new RankSquareExpander());
	}

	public static RankTiler Pruner(InputFile input, int numOfItems, int dimIx)
	{
		return new RankTiler(input, numOfItems, dimIx, new RankPruner());
	}

	protected PrintStream out;

	protected int numOfItems;
	protected InputFile input;
	protected SortedDb db;

	private RankMatTiler matTiler;

	private RankTiler(InputFile input, int numOfItems, int dimIx,
			RankMatTiler matTiler)
	{
		this.input = input;
		this.numOfItems = numOfItems;
		this.matTiler = matTiler;
		initializeLogOut();
		double[][] dataArr;
		try
		{
			dataArr = input.getData().toArray(new double[0][]);
		} catch (FileNotFoundException e)
		{
			throw new InvalidParameterException(e.getMessage());
		}

		db = SortedDb.from(dataArr, dimIx);
	}

	protected Collection<Tile> runFor(int theta, int topK) throws IOException
	{
		out.println(theta);
		int[][] rankMat = rankMatOf(db, numOfItems);
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
			// tiles = new TopKList<>(1);
			// Collection<Tile> tiles = new CoveringMinimals();
			// sc: start column, sr: start row, ec: end column, er: end row
			// int psr = 0;
			// int per = rankMat.length;
			Tile tile = matTiler.findBestTile(rankMat);

			if (tile == null)
			{
				break;
			}
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

	public static int[][] rankMatOf(SortedDb db, int numOfCarts)
			throws IOException
	{
		Cartifier cartifier = new Cartifier(db.db);

		// String filename = "/a/cartified.gz";
		File tempFile = File.createTempFile("carts-", ".gz");
		tempFile.deleteOnExit();
		String filename = tempFile.getAbsolutePath();
		cartifier.cartifyNumeric(new int[]
		{ db.dimIx }, filename);

		int[][] carts = RankCartifier.readCarts(filename, numOfCarts);
		int[][] rankMat = RankCartifier.cartsToRankMatrix(numOfCarts, carts);
		return rankMat;
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