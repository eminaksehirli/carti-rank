package cart.kulua;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cart.Cartifier;
import cart.io.InputFile;
import cart.maximizer.CartiMaximizer;
import cern.colt.Arrays;

import com.google.common.collect.HashMultimap;

import dm.cartification.rank.RankCartifier;

public class RankBinaryExtender
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

		RankBinaryExtender tiler = new RankBinaryExtender(InputFile.forMime(fileName),
				numOfItems);
		List<Tile> coverTiles = tiler.runFor(theta);
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
			out = new PrintStream(File.createTempFile("binary-extender-log", ".txt"));
		} catch (IOException e)
		{
			out = System.err;
			e.printStackTrace();
		}
	}

	public RankBinaryExtender(InputFile input, int numOfItems)
	{
		this.input = input;
		this.numOfItems = numOfItems;
	}

	List<Tile> runFor(int theta) throws IOException
	{
		out.println(theta);
		int numOfCarts = numOfItems;
		// InputFile input = InputFile.forMime(fileName);
		double[][] dataArr = input.getData().toArray(new double[0][]);
		// int minSupport = 10;
		// RankTiler.topK = 1000;
		List<Tile> tiles = new TopKList<>(1000);

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

		// RankTiler.printMatrix(rankMat, out);

		Map<Integer, Integer> freqs = findMaximals(dataArr, theta);
		for (Entry<Integer, Integer> freq : freqs.entrySet())
		{
			out.println(freq.getValue() + " => " + freq.getKey());
		}
		out.println();

		List<Tile> maxims = addMaximals(theta, tiles, rankMat, freqs);
		// System.out.println("# of maximals: " + maxims.size());
		Collections.sort(maxims, new Comparator<Tile>() {
			@Override
			public int compare(Tile o1, Tile o2)
			{
				return Integer.compare(o1.sr, o2.sr);
			}
		});

		HashMultimap<Double, int[]> mergeMap = HashMultimap.create();

		for (ListIterator<Tile> it = maxims.listIterator(); it.hasNext();)
		{
			Tile tile_1 = it.next();
			int sr = tile_1.sr;
			int sc = tile_1.sc;
			ListIterator<Tile> it2 = maxims.listIterator(it.nextIndex());
			while (it2.hasNext())
			{
				Tile tile_2 = it2.next();
				int er = tile_2.er;
				int ec = tile_2.ec;
				int score = scoreOfFreqTile(theta, rankMat, sc, sr, ec + 1, er + 1);
				Tile newTile = RankNaiveTiler.newTile(score, sr, sc, er - 1, ec - 1);
				mergeMap.put(newTile.score, new int[]
				{ it.previousIndex(), it2.previousIndex() });

				tiles.add(newTile);
			}
		}

		out.flush();
		return tiles;
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
			Tile tile = RankNaiveTiler.newTile(score, start, start, end, end);

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
}
