package be.uantwerpen.adrem.cart.rank;

public class RankSquareFastExpander extends RankMatTiler
{
	private int[] ixs;

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