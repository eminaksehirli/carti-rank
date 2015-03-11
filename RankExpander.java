package cart.kulua;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import cart.Cartifier;
import cart.io.InputFile;
import dm.cartification.rank.RankCartifier;

public class RankExpander
{
	public static void main(String[] args) throws IOException
	{
		// final String fileName =
		// "/home/memin/research/data/synth/6c10d/6c10d.mime";
		// int numOfItems = 660;
		// int numOfCarts = numOfItems;
		// int theta = 90;
		final String fileName = "/home/memin/research/data/synth/1d-simple/nonsep-r4d3s25.csv";
		int numOfItems = 75;
		int theta = 25;
		// final String fileName =
		// "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_20.mime";
		// int numOfItems = 20;
		// int numOfCarts = numOfItems;
		// int theta = 10;

		RankExpander tiler = new RankExpander(InputFile.forMime(fileName),
				numOfItems);
		Collection<Tile> coverTiles = tiler.runFor(theta, 1000);
		System.out.println("========================");
		for (Tile tile : coverTiles)
		{
			System.out.println(tile);
		}
	}

	private InputFile input;
	private int numOfItems;
	private static PrintStream out;

	static
	{
		try
		{
			out = new PrintStream(File.createTempFile("expand-log", ".txt"));
		} catch (IOException e)
		{
			out = System.err;
			e.printStackTrace();
		}
	}

	public RankExpander(InputFile input, int numOfItems)
	{
		this.input = input;
		this.numOfItems = numOfItems;
	}

	Collection<Tile> runFor(int theta, int topK) throws IOException
	{
		out.println(theta);
		int numOfCarts = numOfItems;
		// InputFile input = InputFile.forMime(fileName);
		double[][] dataArr = input.getData().toArray(new double[0][]);
		// int minSupport = 10;
		// RankTiler.topK = 1000;
		// List<Tile> tiles = new TopKList<>(1000);

		// List<Pair> pairs = RankTiler.dataToPairs(dataArr);
		//
		// List<List<Double>> db = RankTiler.toSortedDb(dataArr, pairs);

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
		List<Tile> tiles = new TopKList<>(topK);
		// List<Tile> allTiles = new TopKList<>(4);

		// for (int i = 0; i < 10; i++)
		// {
		// List<Tile> tiles = new TopKList<>(2);
		// Collection<Tile> tiles = new CoveringMinimals();
		// sc: start column, sr: start row, ec: end column, er: end row
		int psr = 0;
		int per = rankMat.length;
		startRow:
		for (int sr = psr; sr < per; sr++)
		{
			for (int sc = 0; sc < rankMat[sr].length; sc++)
			{
				int tileSum = rankMat[sr][sc];
				while (tileSum > 0 && sc < sr)
				{
					sc++;
					tileSum = rankMat[sr][sc];
				}
				if (tileSum > 0)
				{
					continue startRow;
				}

				int er = sr;
				int ec = sc;

				boolean extendRight = true;
				boolean extendDown = true;
				while (extendRight || extendDown)
				{
					// out.printf("Checking %d, %d, %d, %d\n", sr, sc, er, ec);
					while (ec < rankMat.length - 1 && extendRight)
					{
						int newColSum = 0;
						for (int r = sr; r <= er; r++)
						{
							newColSum += rankMat[r][ec + 1];
						}
						if (newColSum > 0 && ec >= er)
						{
							extendRight = false;
							break;
						}
						tileSum += newColSum;
						ec++;

						tiles.add(new Tile(tileSum, sr, sc, er, ec));
						extendDown = true;
					}
					while (er < rankMat.length - 1 && extendDown)
					{
						int newRowSum = 0;
						for (int c = sc; c <= ec; c++)
						{
							newRowSum += rankMat[er + 1][c];
						}
						if (newRowSum > 0 && ec <= er)
						{
							extendDown = false;
							break;
						}
						tileSum += newRowSum;
						er++;
						tiles.add(RankTiler.newTile(tileSum, sr, sc, er, ec));
						extendRight = true;

					}
					if (ec >= rankMat.length - 1)
					{
						extendRight = false;
					}
					if (er >= rankMat.length - 1)
					{
						extendDown = false;
					}
				}
			}
		}

		// Tile tile = tiles.get(0);
		// for (int r = tile.sr; r <= tile.er; r++)
		// {
		// for (int c = tile.sc; c <= tile.ec; c++)
		// {
		// rankMat[r][c] = rankMat.length * 3;
		// }
		// }
		//
		// allTiles.add(tile);
		// }
		out.flush();
		return tiles;
		// List<Tile> coverTiles = new ArrayList<>();
		// Set<Integer> cover = RankTiler.findCoveringTiles(tiles, coverTiles);

		// System.out.println("# Coverage: " + cover.size() + " of " + numOfItems);
		// printMerge(mergeMap, coverTiles);
		// return coverTiles;
	}
}
