package cart.kulua;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cart.Cartifier;
import cart.io.InputFile;
import cart.maximizer.CartiMaximizer;
import cern.colt.Arrays;

import com.google.common.collect.HashMultimap;

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

		RankExpander tiler = new RankExpander(InputFile.forMime(fileName), numOfItems);
		List<Tile> coverTiles = tiler.runFor(theta, 1000);
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

	List<Tile> runFor(int theta, int topK) throws IOException
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

//		int topK = 1000;
		List<Til> tiles = new TopKList<>(topK);
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
//					out.printf("Checking %d, %d, %d, %d\n", sr, sc, er, ec);
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

						tiles.add(new Til(sr, sc, er, ec, tileSum));
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
						tiles.add(new Til(sr, sc, er, ec, tileSum));
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

		//
		// Map<Integer, Integer> freqs = findMaximals(dataArr, theta);
		// for (Entry<Integer, Integer> freq : freqs.entrySet())
		// {
		// out.println(freq.getValue() + " => " + freq.getKey());
		// }
		// out.println();
		//
		// List<Tile> maxims = addMaximals(theta, tiles, rankMat, freqs);
		// // System.out.println("# of maximals: " + maxims.size());
		// Collections.sort(maxims, new Comparator<Tile>() {
		// @Override
		// public int compare(Tile o1, Tile o2)
		// {
		// return Integer.compare(o1.rows[0], o2.rows[0]);
		// }
		// });
		//
		// HashMultimap<Double, int[]> mergeMap = HashMultimap.create();
		//
		// for (ListIterator<Tile> it = maxims.listIterator(); it.hasNext();)
		// {
		// Tile tile_1 = it.next();
		// int sr = tile_1.rows[0];
		// int sc = tile_1.cols[0];
		// ListIterator<Tile> it2 = maxims.listIterator(it.nextIndex());
		// while (it2.hasNext())
		// {
		// Tile tile_2 = it2.next();
		// int er = tile_2.rows[tile_2.rows.length - 1];
		// int ec = tile_2.cols[tile_2.cols.length - 1];
		// int score = scoreOfFreqTile(theta, rankMat, sc, sr, ec + 1, er + 1);
		// Tile newTile = RankTiler.newTile(score, sr, sc, er - 1, ec - 1);
		// mergeMap.put(newTile.score, new int[]
		// { it.previousIndex(), it2.previousIndex() });
		//
		// tiles.add(newTile);
		// }
		// }

		List<Tile> boringTiles = new ArrayList<>(tiles.size());

		for (Til tile : tiles)
		{
			int[] rows = new int[tile.er - tile.sr + 1];
			for (int i = tile.sr; i <= tile.er; i++)
			{
				rows[i - tile.sr] = i;
			}
			int[] cols = new int[tile.ec - tile.sc + 1];
			for (int i = tile.sc; i <= tile.ec; i++)
			{
				cols[i - tile.sc] = i;
			}
			boringTiles.add(new Tile(tile.score, rows, cols));
		}
		out.flush();
		return boringTiles;
		// List<Tile> coverTiles = new ArrayList<>();
		// Set<Integer> cover = RankTiler.findCoveringTiles(tiles, coverTiles);

		// System.out.println("# Coverage: " + cover.size() + " of " + numOfItems);
		// printMerge(mergeMap, coverTiles);
		// return coverTiles;
	}

	private static void printMerge(HashMultimap<Double, int[]> mergeMap,
			List<Tile> coverTiles)
	{
		for (Tile tile : coverTiles)
		{
			Set<int[]> ts = mergeMap.get(tile.score);
			if (ts.size() == 0)
			{
				System.out.println("orig!");
			} else
			{
				for (int[] merged : ts)
				{
					System.out.print(Arrays.toString(merged) + " ");
				}
				System.out.println();
			}
		}
	}

	private static void findAndAddMaximals(double[][] dataArr, int theta,
			List<Tile> tiles, int[][] rankMat)
	{
		Map<Integer, Integer> freqs = findMaximals(dataArr, theta);
		addMaximals(theta, tiles, rankMat, freqs);
	}

	private static List<Tile> addMaximals(int theta, List<Tile> tiles,
			int[][] rankMat, Map<Integer, Integer> freqs)
	{
		List<Tile> freqTiles = new ArrayList<>(freqs.size());
		for (Entry<Integer, Integer> freq : freqs.entrySet())
		{
			final Integer start = freq.getValue();
			final int end = freq.getKey() - 1;
			int score = scoreOfFreqTile(theta, rankMat, start, start, freq.getKey(),
					freq.getKey());
			Tile tile = RankTiler.newTile(score, start, start, end, end);

			freqTiles.add(tile);
			tiles.add(tile);
		}
		return freqTiles;
	}

	private static int scoreOfFreqTile(int theta, int[][] rankMat, int sc,
			int sr, int ec, int er)
	{
		int tileSum = 0;
		for (int r = sr; r < er; r++)
		{
			for (int c = sc; c < ec; c++)
			{
				tileSum += rankMat[r][c];
			}
		}
		int score = tileSum - (ec - sc) * (er - sr) * theta;
		return score;
	}

	static Map<Integer, Integer> findMaximals(double[][] dataArr, int theta)
	{
		double[] dim = new double[dataArr.length];
		for (int i = 0; i < dataArr.length; i++)
		{
			dim[i] = dataArr[i][0];
		}
		CartiMaximizer maximer = new CartiMaximizer();
		Map<Integer, Integer> freqs = maximer.mineOneDim(dim, theta + 1);
		return freqs;
	}

	private static class Til implements Comparable<Til>
	{
		int score;
		final int sr, sc, er, ec;
		boolean dr, dd;

		public Til(int sr, int sc, int er, int ec, int score)
		{
			this.sr = sr;
			this.sc = sc;
			this.er = er;
			this.ec = ec;
			this.score = score;
			dr = true;
			dd = true;
		}

		public Til toLeft(int colSum)
		{
			Til newTile = new Til(this.sr, this.sc, this.er, this.ec + 1, this.score
					+ colSum);
			newTile.dr = this.dr;
			newTile.dd = this.dd;
			return newTile;
		}

		@Override
		public int compareTo(Til o)
		{
			return Integer.compare(score, o.score);
		}

		@Override
		public String toString()
		{
			return "[score=" + score + "," + sr + "," + sc + "," + er + "," + ec
					+ ", dr=" + dr + ", dd=" + dd + "]";
		}
	}
}
