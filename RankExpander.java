package cart.kulua;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import cart.io.InputFile;

public class RankExpander extends RankTiler
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
		Collection<Tile> coverTiles = tiler.runFor(theta, 4);
		System.out.println("========================");
		for (Tile tile : coverTiles)
		{
			System.out.println(tile);
		}
	}

	public RankExpander(InputFile input, int numOfItems)
	{
		super(input, numOfItems);
	}

	@Override
	protected void findTiles(int[][] rankMat, List<Tile> tiles, int psr, int per)
	{
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
						tiles.add(RankNaiveTiler.newTile(tileSum, sr, sc, er, ec));
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
	}
}