package be.uantwerpen.adrem.cart.rank;

public class RankExpander extends RankMatTiler
{
	@Override
	protected void findTiles(int psr, int per)
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

						addTile(tileSum, sr, sc, er, ec);
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
						addTile(tileSum, sr, sc, er, ec);
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