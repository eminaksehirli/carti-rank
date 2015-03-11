package cart.kulua;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mime.tool.Utils;
import weka.clusterquality.F1PrecissionMeasure;
import cart.io.InputFile;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Tester
{
	private static PrintStream out;

	public static void main(String[] args) throws IOException
	{
		// final String fileName =
		// "/home/memin/research/data/synth/1d-simple/nonsep-r4d3s50.csv";
		// int numOfItems = 150;
		// int clusterSize = 50;
		// InputFile input = InputFile.forMime(fileName);
		final String fileName = "/home/memin/research/data/synth/1d-simple/nonsep-r4d3s25.csv";
		int numOfItems = 75;
		int clusterSize = 25;
		InputFile input = InputFile.forMime(fileName);
		List<Set<Integer>> trueClusters = create3EqualClusters(clusterSize);
		// final String fileName =
		// "/home/memin/research/data/synth/1d-simple/sep-r4d4s25-50.csv";
		// int numOfItems = 100;
		// int clusterSize = 30;
		// InputFile input = InputFile.forMime(fileName);
		// List<Set<Integer>> trueClusters = createClusters(25, 50, 25);

		out = new PrintStream(File.createTempFile(
				"TilerLog-" + new File(fileName).getName(), ".txt"));
		int topK = 1000;

		Table<String, Integer, Long> times = HashBasedTable.create();
		// Map<String, Long> times = new HashMap<>();

		RankBinaryExtender binary = new RankBinaryExtender(input, numOfItems);
		RankTiler tiler = new RankTiler(input, numOfItems);
		RankExpander expander = new RankExpander(input, numOfItems);
		RankPruner pruner = new RankPruner(input, numOfItems);
		double[][] dataArr = input.getData().toArray(new double[0][]);
		for (int theta = (int) (clusterSize * 0.25); theta < clusterSize * 2; theta += 1)
		// for (int theta = 15; theta < 40; theta += 1)
		// for (int theta = 25; theta < 30; theta += 5)
		{
			// {
			// List<Tile> tiles = binary.runFor(theta, topK);
			// coverEvaluateAndPrint(tiles, trueClusters, theta, numOfItems,
			// "binCov");
			// double coverage = flatten(tiles).size() / ((double) numOfItems);
			// evaluatePrint(tiles, trueClusters, theta, "binar", coverage);
			// }
			//
			{
				long start = currentTimeMillis();
				Map<Integer, Integer> freqs = RankBinaryExtender.findMaximals(dataArr,
						theta);
				times.put("maxer", theta, currentTimeMillis() - start);

				List<Set<Integer>> clusters = new ArrayList<>();
				for (Entry<Integer, Integer> freq : freqs.entrySet())
				{
					Set<Integer> cluster = new HashSet<>();
					for (int i = freq.getValue(); i < freq.getKey(); i++)
					{
						cluster.add(i);
					}
					clusters.add(cluster);
				}
				double f1p = F1PrecissionMeasure.between(clusters, trueClusters);
				double f1r = F1PrecissionMeasure.between(trueClusters, clusters);
				// double coverage = cover.size() / ((double) numOfItems);
				double coverage = mergeAll(clusters).size() / ((double) numOfItems);
				System.out.printf("maxer" + "\t%d\t%.3f\t%.3f\t%.3f\n", theta, f1p,
						f1r, coverage);

				out.println("===== maxer " + " theta:" + theta);
				for (Set<Integer> cluster : clusters)
				{
					out.println(cluster);
				}
			}

			{
				long start = currentTimeMillis();
				List<Tile> tiles = tiler.runFor(theta, topK);
				times.put("tiler", theta, currentTimeMillis() - start);
				coverEvaluateAndPrint(tiles, trueClusters, theta, numOfItems, "tilCov");
				double coverage = flatten(tiles).size() / ((double) numOfItems);
				evaluatePrint(tiles, trueClusters, theta, "tiler", coverage);
			}

			{
				long start = currentTimeMillis();
				Collection<Tile> tiles = expander.runFor(theta, topK);
				times.put("expan", theta, currentTimeMillis() - start);

				coverEvaluateAndPrint(tiles, trueClusters, theta, numOfItems, "expCov");
				double coverage = flatten(tiles).size() / ((double) numOfItems);
				evaluatePrint(tiles, trueClusters, theta, "expan", coverage);
			}

			{
				long start = currentTimeMillis();
				List<Tile> tiles = pruner.runFor(theta, topK);
				times.put("prun", theta, currentTimeMillis() - start);

				coverEvaluateAndPrint(tiles, trueClusters, theta, numOfItems, "pruCov");
				double coverage = flatten(tiles).size() / ((double) numOfItems);
				evaluatePrint(tiles, trueClusters, theta, "prun", coverage);
			}
		}
		out.flush();
		out.close();

		for (Entry<String, Map<Integer, Long>> timeRow : times.rowMap().entrySet())
		{
			System.out.println(timeRow.getKey() + " : " + timeRow.getValue());
		}
	}

	private static Set<Integer> mergeAll(List<Set<Integer>> clusters)
	{
		Set<Integer> cover = new HashSet<>();
		for (Set<Integer> cl : clusters)
		{
			cover.addAll(cl);
		}
		return cover;
	}

	private static Set<Integer> flatten(Collection<Tile> tiles)
	{
		Set<Integer> cover = new HashSet<>();
		for (Tile tile : tiles)
		{
			for (int val = tile.sc; val < tile.ec; val++)
			{
				cover.add(val);
			}
		}
		return cover;
	}

	private static void coverEvaluateAndPrint(Collection<Tile> tiles,
			List<Set<Integer>> trueClusters, int theta, int numOfItems,
			final String name)
	{
		List<Tile> coverTiles = new ArrayList<>();
		Set<Integer> cover = RankTiler.findCoveringTiles(tiles, coverTiles);
		double coverage = cover.size() / ((double) numOfItems);

		evaluatePrint(coverTiles, trueClusters, theta, name, coverage);
	}

	private static void evaluatePrint(Collection<Tile> tiles,
			List<Set<Integer>> trueClusters, int theta, final String name,
			double coverage)
	{
		List<Set<Integer>> clusters = tiles2Clusters(tiles);
		double f1p = F1PrecissionMeasure.between(clusters, trueClusters);
		double f1r = F1PrecissionMeasure.between(trueClusters, clusters);
		System.out.printf(name + "\t%d\t%.3f\t%.3f\t%.3f\n", theta, f1p, f1r,
				coverage);

		out.println("====" + name + " theta:" + theta);
		for (Tile tile : tiles)
		{
			out.println(tile);
		}
	}

	private static List<Set<Integer>> tiles2Clusters(Collection<Tile> tiles)
	{
		List<Set<Integer>> clusters = new ArrayList<>(tiles.size());
		for (Tile tile : tiles)
		{
			Set<Integer> cl = new HashSet<>();
			for (int obj : tile.cols())
			{
				cl.add(obj);
			}
			clusters.add(cl);
		}
		return clusters;
	}

	private static List<Set<Integer>> create3EqualClusters(final int clSize)
	{
		List<Set<Integer>> trueClusters = new ArrayList<>();
		int objIx = 0;
		for (int clIx : Utils.range(3))
		{
			Set<Integer> cl = new HashSet<>();
			for (int i = 0; i < clSize; i++)
			{
				cl.add(objIx);
				objIx++;
			}
			trueClusters.add(cl);
		}
		return trueClusters;
	}

	private static List<Set<Integer>> createClusters(int... clSizes)
	{
		List<Set<Integer>> trueClusters = new ArrayList<>();
		int objIx = 0;
		for (int clSize : clSizes)
		{
			Set<Integer> cl = new HashSet<>();
			for (int i = 0; i < clSize; i++)
			{
				cl.add(objIx);
				objIx++;
			}
			trueClusters.add(cl);
		}
		return trueClusters;
	}
}
