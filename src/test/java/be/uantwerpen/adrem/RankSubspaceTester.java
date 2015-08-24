package cart.kulua;

import i9.subspace.base.Cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;
import weka.subspaceClusterer.SubspaceClusterTools;
import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.maximizer.Freq;
import cart.PaperEvaluator;
import cart.PaperEvaluator.TestData;
import cart.kulua.RankSubspace.SubCluster;
import dm.cartification.kfinder.Tester;

public class RankSubspaceTester
{
	public static void main(String[] args) throws Exception
	{
		PrintWriter wr = new PrintWriter(new File("/a/rank-tester-log"));
		final String dir = "/home/memin/research/data/synth";
		// TestData[] testDatas = PaperEvaluator.aachenSizeDatasets();
		// TestData[] testDatas = Tester.dimScaleDataSets();
		// TestData[] testDatas = Tester.varDenseClusterDataSets();
		// TestData[] testDatas = rankSeparatedScale();
		TestData[] testDatas = rankVerySeparatedScale();
		// TestData[] testDatas = rankNotSeparatedScale();
		// TestData[] testDatas =
		// {//
		// new
		// TestData("/home/memin/research/data/bio/nutt-2003-v1/nutt-2003-v1_database.arff",
		// 100,
		// 14,
		// false), //
		// new TestData("/home/memin/research/data/synth/rank-vs4/4vs4.arff",
		// 4,
		// 50,
		// false), //
		// new TestData("/a/rank-0.1p5cs.arff", 4, 50, false), //
		// new
		// TestData("/home/memin/research/data/Databases/real world data/shape/shape.arff",
		// 15,
		// 20,
		// false), //
		// new TestData("/home/memin/research/data/mfeat/mfeat-pix-half.arff",
		// 120,
		// 200,
		// false),//
		// new
		// TestData("/home/memin/research/data/bio/ramaswamy-2001/ramaswamy-2001_database.arff",
		// 100,
		// 10,
		// false), //
		// new TestData("/home/memin/research/data/Alon/I2000T.arff", 2, 30,
		// false),//
		// };
		for (TestData dataset : testDatas)
		{
			RankSubspace miner = new RankSubspace(InputFile.forMime(dataset.mimeFile));
			final int maxDims = (int) (dataset.expectedNumOfDims * 1.2);

			int es = dataset.expectedClusterSize;
			// int numOfClusters = 10;

//			for (int theta = (int) (es * .5); theta < es * 4.1; theta += es * .1)
			for (int theta = (int) (es * 3); theta < es * 4.1; theta += es * .1)
			// for (int theta = (int) (es * .5); theta < es * 3.1; theta += es / 5)
			// for (int theta = (int) (es * 3); theta < es * 4.1; theta += es /5)
			// for (int theta = (int) (es * .8); theta < es * 1.2; theta += es * .1)
			// for (int theta = 10; theta < 20; theta += 2)
			{
				for (int minLength = theta; minLength > (2 * theta) / 3; minLength -= theta / 10)
				{
					List<SubCluster> tiles = miner.runFor(minLength, theta, maxDims);
					// List<SubCluster> tiles = miner.runFor(minLength, theta);
					final String name = new File(dataset.mimeFile).getName();

					if (tiles.size() > 250_000)
					{
						System.out.println(name + "_" + theta + "_" + minLength + "_"
								+ tiles.size() + "\tn/a");
						break;
					}

					evaluate(dataset, tiles, name + "_" + theta + "_" + minLength + "_"
							+ tiles.size());

					wr.println("======== data= " + dataset.arffFile + " theta=" + theta
							+ ", minLen=" + minLength + " =========");
					for (SubCluster cluster : tiles)
					{
						wr.println(cluster);
					}
					wr.flush();

				}
			}
		}
	}

	private static void evaluate(TestData dataset, List<SubCluster> tiles,
			String exp) throws FileNotFoundException, Exception
	{
		Instances data = PaperEvaluator.prepareWekaInstances(dataset.arffFile);
		final int numOfDims = data.numAttributes();
		// final int numOfDims = data.numAttributes() - 1;
		ArrayList<Cluster> trueClusters = SubspaceClusterTools.getClusterList(
				new File(dataset.trueClustersFileName), numOfDims);

		List<Cluster> theirClusters = new ArrayList<>(tiles.size());
		for (SubCluster tile : tiles)
		{
			boolean[] dims = new boolean[numOfDims];
			for (int i : tile.dims)
			{
				dims[i] = true;
			}
			ArrayList<Integer> objects = new ArrayList<>(tile.ids.length);
			for (int object : tile.ids)
			{
				objects.add(object);
			}
			theirClusters.add(new Cluster(dims, objects));
		}

		PrintWriter log = new PrintWriter(new File("/a/out.log"));
		PaperEvaluator.calculateAndPrintOutMeasures(theirClusters, data, "Rank_"
				+ exp, trueClusters, log);
	}

	public static TestData[] rankVerySeparatedScale()
	{
		final String dir = "/home/memin/research/data/synth/rank-scale/";
		return new TestData[]
		{ //
		new TestData(dir + "rank-0.1p5cs.arff", 4, 50, false), //
				new TestData(dir + "rank-0.2p5cs.arff", 4, 50, false), //
//				new TestData(dir + "rank-0.5p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-1p5cs.arff", 4, 60, false), // it was 50
		// once
		// new TestData(dir + "rank-2p5cs.arff", 4, 70, false), //
		// new TestData(dir + "rank-3p5cs.arff", 4, 80, false), //
		};
	}

	public static TestData[] rankSeparatedScale()
	{
		final String dir = "/home/memin/research/data/synth/rank-scale/s/";
		return new TestData[]
		{ //
		// new TestData(dir + "rank-s-0.1p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-s-0.2p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-s-0.5p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-s-1p5cs.arff", 4, 60, false), //
		// new TestData(dir + "rank-s-2p5cs.arff", 4, 70, false), //
		new TestData(dir + "rank-s-3p5cs.arff", 4, 80, false), //
		};
	}

	public static TestData[] rankNotSeparatedScale()
	{
		final String dir = "/home/memin/research/data/synth/rank-scale/ns/";
		return new TestData[]
		{ //
		// new TestData(dir + "rank-s-0.1p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-s-0.2p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-s-0.5p5cs.arff", 4, 50, false), //
		// new TestData(dir + "rank-s-1p5cs.arff", 4, 60, false), //
		// new TestData(dir + "rank-s-2p5cs.arff", 4, 70, false), //
		// new TestData(dir + "rank-s-3p5cs.arff", 4, 80, false), //
		new TestData(dir + "rank-ns-5p5cs.arff", 4, 120, false), //
		};
	}
}
