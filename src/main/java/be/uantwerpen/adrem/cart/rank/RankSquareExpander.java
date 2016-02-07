package be.uantwerpen.adrem.cart.rank;

/**
 * This tiler considers only the square tiles on the diagonal. See the original
 * paper for the explanation.
 * 
 * @author Emin Aksehirli
 * 
 */
public class RankSquareExpander extends RankMatTiler
{
	@Override
	protected void findTiles(int psr, int per)
	{
		startRow:
		for (int start = psr; start < per; start++)
		{
			int tileSum = rankMat[start][start];
			for (int end = start + 1; end < rankMat.length; end++)
			{
				int newSum = rankMat[end][end];
				for (int col = start; col < end; col++)
				{
					newSum += rankMat[end][col];
				}
				for (int row = start; row < end; row++)
				{
					newSum += rankMat[row][end];
				}

				if (newSum > 0)
				{
					continue startRow;
				}
				tileSum += newSum;

				addTile(tileSum, start, start, end, end);
			}
		}
	}
}