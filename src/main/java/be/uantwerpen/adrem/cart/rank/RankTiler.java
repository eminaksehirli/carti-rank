package be.uantwerpen.adrem.cart.rank;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.model.OneDimDissimilarity;
import be.uantwerpen.adrem.cart.model.RankCartifier;

import com.google.common.io.ByteStreams;

/**
 * Creates the ranked matrices for individual dimensions and finds the best K
 * tiles in them. The best-tile finding algorithm is plugable.
 * 
 * @author Emin Aksehirli
 * 
 */
public class RankTiler
{
	public static RankTiler SquareExpander(InputFile input, int numOfItems,
			int dimIx)
	{
		return new RankTiler(input, numOfItems, dimIx, new RankSquareExpander());
	}

	protected PrintStream out;

	protected int numOfItems;
	protected InputFile input;
	protected SortedDb db;

	private RankMatTiler matTiler;

	private RankTiler(InputFile input, int numOfItems, int dimIx,
			RankMatTiler matTiler)
	{
		this.input = input;
		this.numOfItems = numOfItems;
		this.matTiler = matTiler;
		initializeLogOut();
		double[][] dataArr;
		try
		{
			dataArr = input.getData().toArray(new double[0][]);
		} catch (FileNotFoundException e)
		{
			throw new InvalidParameterException(e.getMessage());
		}

		db = SortedDb.from(dataArr, dimIx);
	}

	protected List<Tile> runFor(int theta, int topK)
	{
		out.println(theta);
		int[][] rankMat = rankMatOf(db, numOfItems);
		for (int rowIx = 0; rowIx < rankMat.length; rowIx++)
		{
			for (int cIx = 0; cIx < rankMat[rowIx].length; cIx++)
			{
				rankMat[rowIx][cIx] -= theta;
			}
		}
		List<Tile> allTiles = new TopKList<>(topK);

		for (int i = 0; i < topK; i++)
		{
			Tile tile = matTiler.findBestTile(rankMat);

			if (tile == null)
			{
				break;
			}
			for (int r = tile.sr; r <= tile.er; r++)
			{
				for (int c = tile.sc; c <= tile.ec; c++)
				{
					rankMat[r][c] = rankMat.length * 3;
				}
			}

			allTiles.add(tile);
		}
		out.flush();
		return allTiles;
	}

	public static int[][] rankMatOf(SortedDb db, int numOfCarts)
	{
		RankCartifier cartif = RankCartifier.newCartifier(db.db,
				new OneDimDissimilarity(db.dimIx));
		return cartif.getRankMat();
	}

	protected static Set<Integer> findCoveringTiles(Collection<Tile> tiles,
			List<Tile> coverTiles)
	{
		Set<Integer> cover = new HashSet<>();
		nextTile:
		for (Tile tile : tiles)
		{
			for (int col : tile.cols())
			{
				if (cover.contains(col))
				{
					continue nextTile;
				}
			}
			coverTiles.add(tile);
			for (int col : tile.cols())
			{
				cover.add(col);
			}
		}
		return cover;
	}

	private void initializeLogOut()
	{
		out = new PrintStream(ByteStreams.nullOutputStream());
	}
}