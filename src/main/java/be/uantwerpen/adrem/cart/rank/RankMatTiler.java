package be.uantwerpen.adrem.cart.rank;

import java.util.List;

/**
 * Find tiles in rank matrices.
 * 
 * @author Emin Aksehirli
 * 
 */
public abstract class RankMatTiler
{
	protected int[][] rankMat;
	protected List<Tile> tiles;

	/**
	 * Finds the best tile in {@code rankMat} by using the class specific
	 * impelemtation.
	 * 
	 * @param rankMat
	 *          Rank-Matrix
	 * @return The best {@link Tile} in the rank matrix.
	 */
	public Tile findBestTile(int[][] rankMat)
	{
		tiles = new TopKList<>(1);
		this.rankMat = rankMat;
		findTiles(0, rankMat.length);
		if (tiles.isEmpty())
		{
			return null;
		}
		return tiles.get(0);
	}

	/**
	 * This is the main search implementation. {@code psr} and {@code per} are for
	 * embarrassingly parallel implementations which we do not use here.
	 * 
	 * @param psr
	 *          Partition Start Row
	 * @param per
	 *          Partition End Row
	 */
	protected abstract void findTiles(int psr, int per);

	protected void addTile(int score, int sr, int sc, int er, int ec)
	{
		Tile tile = new Tile(score, sr, sc, er, ec);
		tiles.add(tile);
	}
}
