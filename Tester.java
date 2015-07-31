package cart.kulua;

import static java.lang.System.currentTimeMillis;
import static tk.memin.dm.cluster.evaluator.AdjustedRandIndex.addComplement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import mime.tool.Utils;
import tk.memin.dm.cluster.evaluator.AdjustedRandIndex;
import weka.clusterquality.F1PrecissionMeasure;
import be.uantwerpen.adrem.cart.io.InputFile;

public class Tester
{
	private static final String Cartification = "Cartification";
	// private static final String Probable = "Probabilistic";
	private static final String BinaryRows = "Binary-Rows";
	private static final String BinaryCols = "Binary-Cols";
	private static final String BinarySq = "Binary-Squares";
	private static final String RankTiling = "Rank-Tiling";
	private static final String NaiveTiling = "Naive-Tiling";
	private static final String RankSquare = "Rank-Square";
	private static PrintStream logOut;
	private static String dataSetName;
	private static int numOfItems;
	private static PrintStream out;
	private static Map<Integer, Map<String, Double>> resultsPre;
	private static Map<Integer, Map<String, Double>> resultsRec;
	private static Map<Integer, Map<String, Double>> resultsCov;
	private static Map<Integer, Map<String, Long>> resultsTime;
	private static Map<Integer, Map<String, Integer>> resultsSize;

	public static void main(String[] args) throws IOException
	{
		final String dir = "/home/memin/research/data/synth/1d-simple/";
		// final String fileName =
		// "/home/memin/research/data/synth/1d-simple/ns50.mime";
		// int numOfItems = 150;
		// int clusterSize = 50;
		// InputFile input = InputFile.forMime(fileName);
		// int clusterSize = 25;

		// final String fileName =
		// "/home/memin/research/data/synth/1d-simple/s3.mime";
		// int numOfItems = 100;
		// int clusterSize = 30;
		// InputFile input = InputFile.forMime(fileName);
		// List<Set<Integer>> trueClusters = createClusters(25, 50, 25);

		List<TestSet> tests = Arrays.asList(
				new TestSet(dir + "ns25.mime", 75, 9, 50, create3EqualClusters(25)),//
				new TestSet(dir + "ns50.mime", 150, 9, 75, create3EqualClusters(50)),//
				new TestSet(dir + "s3.mime", 100, 9, 60, createClusters(25, 50, 25)),//
				new TestSet(dir + "vs4.mime", 190, 15, 100, createClusters(25, 40, 55,
						70)),//
				new TestSet(dir + "vs5.mime", 200, 15, 100, createClusters(50, 25, 50,
						25, 50)),//
				new TestSet(dir + "vs6.mime", 180, 10, 100, createClusters(15, 30, 45,
						45, 30, 15))//
		);

		// System.out.println("Data_Method\tTopK\tTheta\tF1-Precision\tF1-Recall\tCoverage");
		for (TestSet testSet : tests)
		{
			final String fileName = testSet.fileName;
			dataSetName = new File(fileName).getName();
			// int numOfItems = testSet.numOfItems;
			// int clusterSize = 30;
			List<Set<Integer>> trueClusters = testSet.trueClusters;
			numOfItems = testSet.numOfItems;
			resultsPre = new TreeMap<>();
			resultsRec = new TreeMap<>();
			resultsCov = new TreeMap<>();
			resultsTime = new TreeMap<>();
			resultsSize = new TreeMap<>();

			InputFile input = InputFile.forMime(fileName);

			logOut = new PrintStream(File.createTempFile("TilerLog-" + dataSetName,
					".txt"));
			int topK = trueClusters.size() + 1;

			// Table<String, Integer, Long> times = HashBasedTable.create();
			// Map<String, Long> times = new HashMap<>();

			// RankBinaryExtender binary = new RankBinaryExtender(input, numOfItems);
			RankTiler tiler = RankTiler.Naive(input, testSet.numOfItems, 0);
			BinaryTiler binaryC = new BinaryTiler(input);
			BinarySquareTiler binaryS = new BinarySquareTiler(input);
			RankTiler expander = RankTiler.Expander(input, numOfItems, 0);
			RankTiler squareTiler = RankTiler.SquareExpander(input, numOfItems, 0);
			RankTiler pruner = RankTiler.Pruner(input, numOfItems, 0);
			// ProbCartRunner probCart = new ProbCartRunner(input);
			double[][] dataArr = input.getData().toArray(new double[0][]);
			// for (int theta = (int) (clusterSize * 0.25); theta < clusterSize * 2;
			// theta += 1)
			// for (int theta = 15; theta < 40; theta += 1)
			// for (int theta = 25; theta < 30; theta += 5)

			for (int theta = testSet.ts; theta < testSet.te; theta++)
			{
				resultsPre.put(theta, new HashMap<String, Double>());
				resultsRec.put(theta, new HashMap<String, Double>());
				resultsCov.put(theta, new HashMap<String, Double>());
				resultsTime.put(theta, new HashMap<String, Long>());
				resultsSize.put(theta, new HashMap<String, Integer>());
				// int theta = 31;
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
					Map<Integer, Integer> freqs = RankBinaryExtender.findMaximals(
							dataArr, theta);
					final String name = Cartification;
					// times.put(name, theta, currentTimeMillis() - start);
					resultsTime.get(theta).put(name, currentTimeMillis() - start);
					evaluateFreqs(name, freqs, trueClusters, testSet.trueClusters.size(),
							theta, testSet);
				}

				// {
				// long start = currentTimeMillis();
				// List<int[]> freqs = probCart.runFor(0, theta, theta / 2);
				// final String name = Probable;
				// times.put(name, theta, currentTimeMillis() - start);
				//
				// List<Set<Integer>> clusters = new ArrayList<>();
				// for (int[] freq : freqs)
				// {
				// Set<Integer> cluster = new HashSet<>();
				// for (int i : freq)
				// {
				// cluster.add(i);
				// }
				// clusters.add(cluster);
				// }
				// double coverage = mergeAll(clusters).size() / ((double)
				// testSet.numOfItems);
				// printQuality(clusters, trueClusters, theta, name, coverage);
				//
				// logOut.println("===== " + name + " " + " theta:" + theta);
				// for (Set<Integer> cluster : clusters)
				// {
				// logOut.println(cluster);
				// }
				// }

//				{
//					long start = currentTimeMillis();
//					Collection<Tile> tiles = tiler.runFor(theta, topK);
//					// times.put("tiler", theta, currentTimeMillis() - start);
//
//					resultsTime.get(theta).put(NaiveTiling, currentTimeMillis() - start);
//					// coverEvaluateAndPrint(tiles, trueClusters, theta,
//					// testSet.numOfItems, "tilCov");
//					double coverage = flatten(tiles).size()
//							/ ((double) testSet.numOfItems);
//					evaluatePrint(tiles, trueClusters, theta, NaiveTiling, coverage);
//				}

				{
					long start = currentTimeMillis();
					Collection<Tile> tiles = binaryC.runFor(0, theta, topK);
					// times.put(BinaryCols, theta, currentTimeMillis() - start);
					resultsTime.get(theta).put(BinaryCols, currentTimeMillis() - start);
					resultsTime.get(theta).put(BinaryRows, currentTimeMillis() - start);

					// coverEvaluateAndPrint(tiles, trueClusters, theta,
					// testSet.numOfItems, "expCov");
					double coverage = flatten(tiles).size()
							/ ((double) testSet.numOfItems);
					evaluatePrint(tiles, trueClusters, theta, BinaryCols, coverage);

					List<Tile> transTiles = new ArrayList<>(tiles.size());
					for (Tile t : tiles)
					{
						transTiles.add(new Tile(t.score, t.sc, t.sr, t.ec, t.er));
					}
					double tCoverage = flatten(transTiles).size()
							/ ((double) testSet.numOfItems);
					evaluatePrint(transTiles, trueClusters, theta, BinaryRows, tCoverage);
				}

				// {
				// long start = currentTimeMillis();
				// Collection<Tile> tiles = binaryS.runFor(0, theta, topK);
				// // times.put(BinarySq, theta, currentTimeMillis() - start);
				// resultsTime.get(theta).put(BinarySq, currentTimeMillis() - start);
				// // coverEvaluateAndPrint(tiles, trueClusters, theta,
				// // testSet.numOfItems, "expCov");
				// double coverage = flatten(tiles).size()
				// / ((double) testSet.numOfItems);
				// evaluatePrint(tiles, trueClusters, theta, BinarySq, coverage);
				// }

				// {
				// long start = currentTimeMillis();
				// Collection<Tile> tiles = expander.runFor(theta, topK);
				// // times.put(RankTiling, theta, currentTimeMillis() - start);
				// resultsTime.get(theta).put(RankTiling, currentTimeMillis() - start);
				//
				// // coverEvaluateAndPrint(tiles, trueClusters, theta,
				// // testSet.numOfItems, "expCov");
				// double coverage = flatten(tiles).size()
				// / ((double) testSet.numOfItems);
				// evaluatePrint(tiles, trueClusters, theta, RankTiling, coverage);
				// }

				{
					long start = currentTimeMillis();
					Collection<Tile> tiles = squareTiler.runFor(theta, topK);
					// times.put(RankSquare, theta, currentTimeMillis() - start);
					resultsTime.get(theta).put(RankSquare, currentTimeMillis() - start);

					// coverEvaluateAndPrint(tiles, trueClusters, theta,
					// testSet.numOfItems, "expCov");
					double coverage = flatten(tiles).size()
							/ ((double) testSet.numOfItems);
					evaluatePrint(tiles, trueClusters, theta, RankSquare, coverage);
				}

				// {
				// long start = currentTimeMillis();
				// Collection<Tile> tiles = pruner.runFor(0, theta, topK);
				// times.put("prun", theta, currentTimeMillis() - start);
				// // coverEvaluateAndPrint(tiles, trueClusters, theta,
				// testSet.numOfItems, "pruCov");
				// double coverage = flatten(tiles).size() / ((double)
				// testSet.numOfItems);
				// evaluatePrint(tiles, trueClusters, theta, "prun", coverage);
				// }
			}
			logOut.flush();
			logOut.close();

			// for (Entry<String, Map<Integer, Long>> timeRow : times.rowMap()
			// .entrySet())
			// {
			// System.out.println(timeRow.getKey() + " : " + timeRow.getValue());
			// }
			resultsPre.remove(testSet.ts);
			resultsRec.remove(testSet.ts);
			resultsCov.remove(testSet.ts);
			resultsTime.remove(testSet.ts);
			resultsSize.remove(testSet.ts);
			printResults(resultsPre, testSet.fileName + "_P.dat");
			printResults(resultsRec, testSet.fileName + "_R.dat");
			printResults(resultsCov, testSet.fileName + "_C.dat");
			printResults(resultsTime, testSet.fileName + "_time.dat");
			printResults(resultsSize, testSet.fileName + "_size.dat");
		}
	}

	private static <N extends Number> void printResults(
			Map<Integer, Map<String, N>> results, final String name)
			throws FileNotFoundException
	{
		PrintStream outQ = new PrintStream(new File("/a/"
				+ new File(name).getName()));

		// Iterator<Entry<Integer, Map<String, Double>>> it = results.entrySet()
		// .iterator();
		// outQ.print("Theta");
		// if (it.hasNext())
		// {
		// Entry<Integer, Map<String, Double>> result = it.next();
		// for (String method : result.getValue().keySet())
		// {
		// outQ.print("\t" + method);
		// }
		// outQ.println();
		// }

		String[] methods = new String[]
		{ RankTiling, RankSquare, BinaryCols, BinaryRows, BinarySq, Cartification,
				NaiveTiling };
		outQ.print("Theta");
		for (String method : methods)
		{
			outQ.print("\t" + method);
		}
		outQ.println();

		for (Entry<Integer, Map<String, N>> theta : results.entrySet())
		{
			outQ.print(theta.getKey());
			for (String method : methods)
			{
				final N val = theta.getValue().get(method);
				if (val instanceof Double)
				{
					outQ.printf("\t%.4f", val);
				} else
				{
					outQ.printf("\t%d", val);
				}
			}

			// for (Entry<String, Double> res : theta.getValue().entrySet())
			// {
			// outQ.print("\t" + res.getValue());
			// }
			outQ.println();
		}
		outQ.close();
	}

	private static List<Set<Integer>> freqs2Clusters(Map<Integer, Integer> freqs)
	{
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
		return clusters;
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
			for (int val = tile.sc; val <= tile.ec; val++)
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
		int maxK = Math.min(trueClusters.size() + 1, tiles.size());
		int minK = Math.max(trueClusters.size() - 1, 1);
		// for (int topK = minK; topK <= maxK; topK++)
		{
			int topK = trueClusters.size();
			List<Set<Integer>> clusters = tiles2Clusters(tiles, topK);
			printQuality(clusters, trueClusters, theta, name, coverage);
		}
		logOut.println("=====" + name + " theta:" + theta);
		for (Tile tile : tiles)
		{
			logOut.println(tile);
		}
	}

	private static void printQuality(List<Set<Integer>> clusters,
			List<Set<Integer>> trueClusters, int theta, final String name,
			double coverage)
	{
		double f1p = F1PrecissionMeasure.between(clusters, trueClusters);
		double f1r = F1PrecissionMeasure.between(trueClusters, clusters);
		int size = clusters.size();
		// double ari = ariBetween(trueClusters, clusters);

		// out.printf(dataSetName + "_" + name + "\t%d\t%d\t%.3f\t%.3f\t%.3f\n",
		// topK, theta, f1p, f1r, coverage);
		resultsPre.get(theta).put(name, f1p);
		resultsRec.get(theta).put(name, f1r);
		resultsCov.get(theta).put(name, coverage);
		resultsSize.get(theta).put(name, size);
	}

	private static void evaluateFreqs(final String name,
			Map<Integer, Integer> freqs, List<Set<Integer>> trueClusters, int topK,
			int theta, TestSet testSet)
	{
		List<Set<Integer>> clusters = freqs2Clusters(freqs);
		double coverage = mergeAll(clusters).size() / ((double) testSet.numOfItems);
		printQuality(clusters, trueClusters, theta, name, coverage);

		logOut.println("===== " + name + " " + " theta:" + theta);
		for (Set<Integer> cluster : clusters)
		{
			logOut.println(cluster);
		}
	}

	private static double ariBetween(List<Set<Integer>> trueClusters,
			List<Set<Integer>> clusters)
	{
		BitSet[] clusterBS = addComplement(clus2BS(clusters), numOfItems);
		BitSet[] trueClusterBS = clus2BS(trueClusters);

		try
		{
			double ari = AdjustedRandIndex.between(clusterBS, trueClusterBS);
			return ari;
		} catch (IllegalArgumentException e)
		{
			return 0;
		}
	}

	private static BitSet[] clus2BS(List<Set<Integer>> clusters)
	{
		BitSet[] clusterBS = new BitSet[clusters.size()];
		int counter = 0;
		for (Set<Integer> cluster : clusters)
		{
			BitSet clBs = new BitSet();
			for (int i : cluster)
			{
				clBs.set(i);
			}
			clusterBS[counter++] = clBs;
		}
		return clusterBS;
	}

	private static List<Set<Integer>> tiles2Clusters(Collection<Tile> tiles,
			int topK)
	{
		List<Set<Integer>> clusters = new ArrayList<>(topK);
		int counter = 1;
		for (Tile tile : tiles)
		{
			if (counter > topK)
			{
				break;
			}
			Set<Integer> cl = new HashSet<>();
			for (int obj : tile.cols())
			{
				cl.add(obj);
			}
			clusters.add(cl);

			counter++;
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

	private static class TestSet
	{
		private final String fileName;
		private final int numOfItems;
		private final int ts;
		private final int te;
		private final List<Set<Integer>> trueClusters;

		public TestSet(String fileName, int numOfItems, int ts, int te,
				List<Set<Integer>> trueClusters)
		{
			this.fileName = fileName;
			this.numOfItems = numOfItems;
			this.ts = ts;
			this.te = te;
			this.trueClusters = trueClusters;
		}
	}
}
