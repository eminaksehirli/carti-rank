package cart.kulua;

import java.util.List;

public abstract class RankMatTiler
{
	protected int[][] rankMat;
	protected List<Tile> tiles;

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

	protected abstract void findTiles(int psr, int per);

	protected void addTile(int score, int sr, int sc, int er, int ec)
	{
		Tile tile = new Tile(score, sr, sc, er, ec);
		tiles.add(tile);
	}
}
