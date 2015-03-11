package cart.kulua;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import cart.Cartifier;
import cart.io.InputFile;
import dm.cartification.rank.RankCartifier;

public class RankPruner extends RankTiler
{
	// static int topK = 1000;
	// private static PrintStream out;
	// private static PrintStream matrixOut = new PrintStream(new
	// NullOutputStream());
	// static
	// {
	// try
	// {
	// out = new PrintStream(File.createTempFile("pruner-log", ".txt"));
	// } catch (IOException e)
	// {
	// out = System.err;
	// e.printStackTrace();
	// }
	// }

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
		RankPruner tiler = new RankPruner(InputFile.forMime(fileName), numOfItems);
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

		Collection<Tile> tiles = tiler.runFor(theta, 1000);

		// for (Tile tile : tiles)
		// {
		// System.out.println(tile);
		// }

		// System.out.println("Mine time:" + (mineComplete - conversionEnd)
		// + "ms, Total time: " + (mineComplete - start));
	}

	public RankPruner(InputFile input, int numOfItems)
	{
		super(input, numOfItems);
	}

//	List<Tile> runFor(int theta, int topK) throws IOException
//	{
//		// out.println(theta);
//		int numOfCarts = numOfItems;
//		long start = System.currentTimeMillis();
//		// InputFile input = InputFile.forMime(fileName);
//		double[][] dataArr = input.getData().toArray(new double[0][]);
//
//		SortedDb db = SortedDb.from(dataArr);
//
//		Cartifier cartifier = new Cartifier(db.db);
//		String filename = "/a/cartified.gz";
//		cartifier.cartifyNumeric(new int[]
//		{ 0 }, filename);
//
//		int[][] carts = RankCartifier.readCarts(filename, numOfCarts);
//		int[][] rankMat = RankCartifier.cartsToRankMatrix(numOfItems, carts);
//		for (int rowIx = 0; rowIx < rankMat.length; rowIx++)
//		{
//			for (int cIx = 0; cIx < rankMat[rowIx].length; cIx++)
//			{
//				rankMat[rowIx][cIx] -= theta;
//			}
//		}
//		// printMatrix(rankMat, out);
//
//		long conversionEnd = currentTimeMillis();
//		List<Tile> tiles = new TopKList<Tile>(topK);
//		// sc: start column, sr: start row, ec: end column, er: end row
//		int psr = 0;
//		int per = rankMat.length;
//		findTiles(rankMat, tiles, psr, per);
//
//		long mineComplete = System.currentTimeMillis();
//		// printMatrix(rankMat);
//		// out.flush();
//		return tiles;
//	}

	@Override
	protected void findTiles(int[][] rankMat, List<Tile> tiles, int psr, int per)
	{
		startRow:
		for (int sr = psr; sr < per; sr++)
		{
			startColumn:
			for (int sc = 0; sc < rankMat[sr].length; sc++)
			{
				// int tileSum = rankMat[sr][sc];
				while (rankMat[sr][sc] > 0 && sc < sr)
				{
					sc++;
				}
				if (rankMat[sr][sc] > 0)
				{
					continue startRow;
				}

				// int prevTileSum = Integer.MAX_VALUE;
				for (int er = sr; er < rankMat.length; er++)
				{
					int tileSum = 0;
					for (int ec = sc; ec < rankMat[er].length; ec++)
					{
						int newColSum = 0;
						for (int r = sr; r <= er; r++)
						{
							newColSum += rankMat[r][ec];
						}
						// int score = tileSum - (ec - sc + 1) * (er - sr + 1) * theta;
						if (newColSum > 0 && ec >= er)
						{
							break;
						}
						tileSum += newColSum;
						if (tileSum > 0)
						{
							continue startColumn;
						}

						tiles.add(RankNaiveTiler.newTile(tileSum, sr, sc, er, ec));
					}
				}
			}
		}
	}
}
