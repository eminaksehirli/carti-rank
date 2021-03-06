package be.uantwerpen.adrem.cart.rank;

import static java.lang.Integer.parseInt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.rank.RankSubspace.SubCluster;

/**
 * The runner class for the algorithm. It takes the input as parameters and run
 * the main algorithm.
 * 
 * @author Emin Aksehirli
 * 
 */
public class Runner
{
	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.err.println("Usage: java Runner datafile.mime theta minLen [maxDims]");
			System.err.println("Output: (cluster size) [dimensions] -- [objects]");
			System.err.println("For more information http://adrem.uantwerpen.be/cartirank");

			return;
		}
		String fileName = args[0];
		int theta = parseInt(args[1]);
		int minLen = parseInt(args[2]);
		int maxDims = 10;
		if (args.length == 4)
		{
			maxDims = parseInt(args[3]);
		}

		RankSubspace miner = new RankSubspace(InputFile.forMime(fileName));

		List<SubCluster> tiles;
		try
		{
			tiles = miner.runFor(minLen, theta, maxDims);
		} catch (IOException e)
		{
			if (e instanceof FileNotFoundException)
			{
				System.err.println("File cannot be found: " + fileName);
			}
			e.printStackTrace();
			return;
		}

		for (SubCluster cluster : tiles)
		{
			System.out.print(cluster.ids.length + " - ");
			for (int dim : cluster.dims)
			{
				System.out.printf("%d ", dim);
			}
			System.out.printf("- ");

			for (int obj : cluster.ids)
			{
				System.out.printf("%d ", obj);
			}
			System.out.println();
		}
	}
}
