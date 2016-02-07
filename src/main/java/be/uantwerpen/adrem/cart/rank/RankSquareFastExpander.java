package be.uantwerpen.adrem.cart.rank;

/**
 * Similar to {@link RankSquareExpander}, this tiler only considers square tiles
 * on the diagonal. However, instead of the whole matrix, it only uses ranks of
 * some of the objects indexes of which are given in the constructor.
 * 
 * See the paper for a more detailed explanation.
 * 
 * @author Emin Aksehirli
 * 
 */
public class RankSquareFastExpander extends RankMatTiler
{
	private int[] ixs;

	/**
	 * Similar to {@link RankSquareExpander}, this tiler only considers square
	 * tiles on the diagonal. However, instead of the whole matrix, it only uses
	 * ranks of some of the objects indexes of which are given in the constructor.
	 * 
	 * See the paper for a more detailed explanation.
	 * 
	 * @param ixs
	 *          Indices in the matrix to consider.
	 */
	public RankSquareFastExpander(int[] ixs)
	{
		super();
		this.ixs = ixs;
	}

	@Override
	protected void findTiles(int psr, int per)
	{
		startRow:
		for (int start = psr; start < ixs.length; start++)
		{
			int tileSum = rankMat[ixs[start]][ixs[start]];
			for (int end = start + 1; end < ixs.length; end++)
			{
				int newSum = rankMat[ixs[end]][ixs[end]];
				for (int col = start; col < end; col++)
				{
					newSum += rankMat[ixs[end]][ixs[col]];
				}
				for (int row = start; row < end; row++)
				{
					newSum += rankMat[ixs[row]][ixs[end]];
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