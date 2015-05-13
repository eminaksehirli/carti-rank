package cart.kulua;

public class RankPruner extends RankMatTiler
{
	@Override
	protected void findTiles(int psr, int per)
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

						addTile(tileSum, sr, sc, er, ec);
					}
				}
			}
		}
	}
}
