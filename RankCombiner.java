package cart.kulua;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import tk.memin.dm.matrix.MatrixVisualiser;
import be.uantwerpen.adrem.cart.io.InputFile;

public class RankCombiner
{
	private static int theta;
	private static int numOfItems;

	public static void main(String[] args) throws IOException
	{
		// InputFile input =
		// InputFile.forMime("/home/memin/research/data/synth/6c10d/6c10d.mime");
		InputFile input = InputFile.forMime("/home/memin/research/data/synth/6c10d/p12.mime");
		numOfItems = 660;
		theta = 205;
		RankTiler tiler_0 = RankTiler.SquareExpander(input, numOfItems, 0);

		Collection<Tile> c0 = tiler_0.runFor((int) (theta), 5);
		// Collection<Tile> c1 = squareTiler_1.runFor(0, 0);

		int[][] mat_0 = RankTiler.rankMatOf(tiler_0.db, numOfItems);
		MatrixVisualiser.showFrame(mat_0);

		List<int[]> tileIds = new ArrayList<>();
		for (Tile tile : c0)
		{
			int[] ids = tiler_0.db.loc2Ids(tile.sr, tile.er);
			Arrays.sort(ids);
			tileIds.add(ids);
		}

		System.out.println("---- theta: " + theta);
		for (int[] tile : tileIds)
		{
			System.out.println(tile.length + ": " + Arrays.toString(tile));
		}
		// System.out.println("----");

		List<int[]> nextTiles = tileIds;
		for (int dimIx = 1; dimIx < 16; dimIx += 3)
		{
			theta = (int) (theta * .95);
			System.out.println("---- theta: " + theta);
			nextTiles = refineAndPrintDim(dimIx, input, nextTiles);

			int removed = 0;
			for (Iterator<int[]> it = nextTiles.iterator(); it.hasNext();)
			{
				int[] is = it.next();
				if (is.length < 30)
				{
					it.remove();
					removed++;
				}
			}
			System.in.read();
		}

		// nextTiles = refineAndPrintDim(7, input, nextTiles);
		// System.out.println("----");
		//
		// nextTiles = refineAndPrintDim(4, input, nextTiles);
		// System.out.println("----");
		//
		// nextTiles = refineAndPrintDim(6, input, nextTiles);
		// System.out.println("----");
		//
		// nextTiles = refineAndPrintDim(10, input, nextTiles);

		// System.out.println(c0);
		// System.out.println(c1);
	}

	private static List<int[]> refineAndPrintDim(final int dimIx,
			InputFile input, List<int[]> tileIds) throws IOException
	{
		RankTiler tiler = RankTiler.SquareExpander(input, numOfItems, dimIx);
		List<int[]> nextTiles = refineTiles(tiler.db, tileIds);
		for (int[] tile : nextTiles)
		{
			System.out.println(tile.length + ": " + Arrays.toString(tile));
		}
		return nextTiles;
	}

	private static List<int[]> refineTiles(SortedDb nextDb,
			Collection<int[]> tileIds) throws IOException
	{
		List<int[]> nextTiles = new ArrayList<>();
		int[][] mat_n = RankTiler.rankMatOf(nextDb, numOfItems);
		int[] loc2Ids_n = nextDb.loc2Ids(0, numOfItems - 1);
		for (int[] ids : tileIds)
		{
			// System.out.println(ids.length + ": " + Arrays.toString(ids));
			boolean[] toAdd = new boolean[numOfItems];
			for (int id : ids)
			{
				toAdd[id] = true;
			}
			int[][] newMat = new int[ids.length][ids.length];

			int[] loc2Ids_comb = new int[ids.length];

			int new_i = 0;
			for (int i = 0; i < numOfItems; i++)
			{
				if (toAdd[loc2Ids_n[i]])
				{
					loc2Ids_comb[new_i] = loc2Ids_n[i];
					int new_j = 0;
					for (int j = 0; j < numOfItems; j++)
					{
						if (toAdd[loc2Ids_n[j]])
						{
							newMat[new_i][new_j] = mat_n[i][j];
							new_j++;
						}
					}
					new_i++;
				}
			}

			for (int i = 0; i < newMat.length; i++)
			{
				for (int j = 0; j < newMat[i].length; j++)
				{
					newMat[i][j] -= theta;
				}
			}
			MatrixVisualiser.showFrame(newMat);

			RankSquareExpander tiler = new RankSquareExpander();

			List<Tile> tiles = new ArrayList<>();
			for (int i = 0; i < 2; i++)
			{
				Tile tile = tiler.findBestTile(newMat);
				if (tile == null)
				{
					break;
				}
				tiles.add(tile);
				for (int r = tile.sr; r <= tile.er; r++)
				{
					for (int c = tile.sc; c <= tile.ec; c++)
					{
						newMat[r][c] = newMat.length * 3;
					}
				}
			}

			for (Tile t : tiles)
			{
				// System.out.print((t.er - t.sr + 1) + ": ");
				int[] cluster = new int[t.er - t.sr + 1];
				for (int i = t.sr; i <= t.er; i++)
				{
					cluster[i - t.sr] = loc2Ids_comb[i];
					// System.out.print(loc2Ids_comb[i] + ",");
				}
				// System.out.println();
				Arrays.sort(cluster);
				nextTiles.add(cluster);
				// System.out.println(cluster);
			}
			// System.out.println();
		}
		return nextTiles;
	}
}
