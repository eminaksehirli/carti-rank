package cart.kulua;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import cart.io.InputFile;

public class RankNaiveTiler extends RankTiler
{

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
		RankTiler tiler = new RankNaiveTiler(InputFile.forMime(fileName),
				numOfItems);
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


	public RankNaiveTiler(InputFile input, int numOfItems)
	{
		super(input,numOfItems);
	}


	@Override
	protected void findTiles(int[][] rankMat, List<Tile> tiles, int psr, int per)
	{
		for (int sr = psr; sr < per; sr++)
		{
			for (int sc = 0; sc < rankMat[sr].length; sc++)
			{
				for (int er = sr; er < rankMat.length; er++)
				{
					int tileSum = 0;
					for (int ec = sc; ec < rankMat[er].length; ec++)
					{
						for (int r = sr; r <= er; r++)
						{
							tileSum += rankMat[r][ec];
						}
						// int score = tileSum - (ec - sc + 1) * (er - sr + 1) * theta;
						int score = tileSum;
						Tile tile = newTile(score, sr, sc, er, ec);

						tiles.add(tile);
					}
				}
			}
		}
	}
}