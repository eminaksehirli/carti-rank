package be.uantwerpen.adrem.cart.rank;

/**
 * This is the most naive tiler. It computes the tile score for every possible
 * combination.
 * 
 * @author Emin Aksehirli
 * 
 */
public class RankNaiveTiler extends RankMatTiler
{
	@Override
	protected void findTiles(int psr, int per)
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
						int score = tileSum;
						addTile(score, sr, sc, er, ec);
					}
				}
			}
		}
	}
}