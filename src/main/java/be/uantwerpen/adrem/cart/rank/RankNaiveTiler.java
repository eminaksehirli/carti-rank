package be.uantwerpen.adrem.cart.rank;

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
						// int score = tileSum - (ec - sc + 1) * (er - sr + 1) * theta;
						int score = tileSum;
						addTile(score, sr, sc, er, ec);
					}
				}
			}
		}
	}
}