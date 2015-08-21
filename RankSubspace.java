package cart.kulua;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tk.memin.dm.matrix.MatrixVisualiser;
import be.uantwerpen.adrem.cart.io.InputFile;

public class RankSubspace
{
	public static class SubCluster
	{
		public int[] ids;
		public Set<Integer> dims;

		public SubCluster(int[] ids, Set<Integer> dims)
		{
			this.ids = ids;
			this.dims = dims;
		}

		@Override
		public String toString()
		{
			return "[" + ids.length + "] " + dims + " " + Arrays.toString(ids);
		}
	}

	private int numOfItems;
	private boolean debug = false;
	private int numOfDims;
	private InputFile input;
	private List<SubCluster> allTiles;
	private int minLength;
	private int[][][] dimMatrices;
	private int[][] dimLoc2Ids;

	public static void main(String[] args) throws IOException
	{
		InputFile input = InputFile.forMime("/home/memin/research/data/synth/6c10d/6c10d.mime");
		// InputFile input =
		// InputFile.forMime("/home/memin/research/data/synth/6c10d/p12.mime");
		int numOfClusters = 4;
		int theta = 205;
		// final int startDimIx = 0;

		RankSubspace miner = new RankSubspace(input);
		List<SubCluster> tiles = miner.runFor(numOfClusters, theta);

		for (SubCluster tile : tiles)
		{
			System.out.println(tile);
		}
	}

	public RankSubspace(InputFile input)
	{
		this.input = input;
	}

	public List<SubCluster> runFor(int numOfClusters, int theta)
			throws IOException
	{
		allTiles = new ArrayList<>();
		this.numOfItems = input.getData().size();
		this.numOfDims = input.getData().get(0).length;

//		minLength = theta / 3;
		minLength = 50;

		dimMatrices = new int[numOfDims][][];
		dimLoc2Ids = new int[numOfDims][];
		for (int dimIx = 0; dimIx < numOfDims; dimIx++)
		{
			RankTiler tiler = RankTiler.SquareExpander(input, numOfItems, dimIx);
			dimMatrices[dimIx] = RankTiler.rankMatOf(tiler.db, numOfItems);
			dimLoc2Ids[dimIx] = tiler.db.loc2Ids(0, numOfItems - 1);
			// System.out.println("Matrix for dim " + dimIx + "/" + numOfDims
			// + " is created.");
		}

		for (int startDimIx = 0; startDimIx < numOfDims; startDimIx++)
		{
			RankTiler tiler_s = RankTiler.SquareExpander(input, numOfItems,
					startDimIx);

			Collection<Tile> c0 = tiler_s.runFor(theta, numOfClusters);
			// Collection<Tile> c1 = squareTiler_1.runFor(0, 0);

			// int[][] mat_0 = RankTiler.rankMatOf(tiler_s.db, numOfItems);
			// if (debug)
			// {
			// MatrixVisualiser.showFrame(mat_0);
			// }
			List<int[]> tileIds = new ArrayList<>();
			for (Tile tile : c0)
			{
				int[] ids = tiler_s.db.loc2Ids(tile.sr, tile.er);
				Arrays.sort(ids);
				tileIds.add(ids);
			}

			// System.out.println("---- theta: " + theta);
			// for (int[] tile : tileIds)
			// {
			// System.out.println(tile.length + ": " + Arrays.toString(tile));
			// }
			// System.out.println("----");

			List<Integer> allDims = new ArrayList<>();
			for (int i = 0; i < numOfDims; i++)
			{
				allDims.add(i);
			}

			Set<Integer> dimCandidates = new HashSet<>(allDims);
			dimCandidates.remove(startDimIx);
			Set<Integer> freqDims = new HashSet<>();
			freqDims.add(startDimIx);

			for (int[] tile : tileIds)
			{
				addCluster(tile, freqDims);
				checkForMore(tile, dimCandidates, freqDims, (int) (theta * .95));
			}
		}
		return allTiles;
	}

	private void checkForMore(int[] tile, Set<Integer> dimCandidates,
			Set<Integer> freqDims, int theta) throws IOException
	{
		final int newTheta = (int) (theta * .95);

		for (int dimIx : dimCandidates)
		{
			// RankTiler tiler = RankTiler.SquareExpander(input, numOfItems, dimIx);
			List<int[]> nextTiles = refineTile(dimIx, tile, theta);

//			System.out.println("For dim " + dimIx + " and " + freqDims + ":"
//					+ nextTiles.size());

			Set<Integer> tileDims = new HashSet<>(freqDims);
			tileDims.add(dimIx);
			Set<Integer> tileDimCandidates = new HashSet<>(dimCandidates);
			tileDimCandidates.remove(dimIx);

			for (int[] nextTile : nextTiles)
			{
				if (nextTile.length > minLength)
				{
					addCluster(nextTile, tileDims);
					checkForMore(nextTile, tileDimCandidates, tileDims, newTheta);
				}
			}
		}
	}

	private void addCluster(int[] nextTile, Set<Integer> tileDims)
	{
		allTiles.add(new SubCluster(nextTile, tileDims));
	}

	// private static List<int[]> refineAndPrintDim(final int dimIx,
	// InputFile input, List<int[]> tileIds) throws IOException
	// {
	// RankTiler tiler = RankTiler.SquareExpander(input, numOfItems, dimIx);
	// List<int[]> nextTiles = refineTiles(tiler.db, tileIds);
	// for (int[] tile : nextTiles)
	// {
	// System.out.println(tile.length + ": " + Arrays.toString(tile));
	// }
	// return nextTiles;
	// }

	private List<int[]> refineTile(int dimIx, int[] ids, int theta)
			throws IOException
	{
		List<int[]> nextTiles = new ArrayList<>();
		// // matrix of the next dimension
		// int[][] mat_n = RankTiler.rankMatOf(nextDb, numOfItems);
		// // loc2Ids of the next dimension matrix
		// int[] loc2Ids_n = nextDb.loc2Ids(0, numOfItems - 1);

		// loc2Ids of the conditional matrix
		int[] loc2Ids_cond = new int[ids.length];

		int[][] condMat = createCondMatrix(dimIx, ids, theta, loc2Ids_cond);

		RankSquareExpander tiler = new RankSquareExpander();

		List<Tile> tiles = new ArrayList<>();
		for (int i = 0; i < 2; i++)
		{
			Tile tile = tiler.findBestTile(condMat);
			if (tile == null)
			{
				break;
			}
			tiles.add(tile);
			for (int r = tile.sr; r <= tile.er; r++)
			{
				for (int c = tile.sc; c <= tile.ec; c++)
				{
					condMat[r][c] = condMat.length * 3;
				}
			}
		}

		for (Tile t : tiles)
		{
			// System.out.print((t.er - t.sr + 1) + ": ");
			int[] cluster = new int[t.er - t.sr + 1];
			for (int i = t.sr; i <= t.er; i++)
			{
				cluster[i - t.sr] = loc2Ids_cond[i];
				// System.out.print(loc2Ids_comb[i] + ",");
			}
			// System.out.println();
			Arrays.sort(cluster);
			nextTiles.add(cluster);
			// System.out.println(cluster);
		}
		// System.out.println();
		return nextTiles;
	}

	private int[][] createCondMatrix(int dimIx, int[] ids, int theta,
			int[] loc2Ids_cond)
	{
		// matrix of the next dimension
		int[][] mat_n = dimMatrices[dimIx];
		// loc2Ids of the next dimension matrix
		int[] loc2Ids_n = dimLoc2Ids[dimIx];
		// System.out.println(ids.length + ": " + Arrays.toString(ids));
		boolean[] toAdd = new boolean[numOfItems];
		for (int id : ids)
		{
			toAdd[id] = true;
		}
		// conditional matrix
		int[][] condMat = new int[ids.length][ids.length];

		int new_i = 0;
		for (int i = 0; i < numOfItems; i++)
		{
			if (toAdd[loc2Ids_n[i]])
			{
				loc2Ids_cond[new_i] = loc2Ids_n[i];
				int new_j = 0;
				for (int j = 0; j < numOfItems; j++)
				{
					if (toAdd[loc2Ids_n[j]])
					{
						condMat[new_i][new_j] = mat_n[i][j];
						new_j++;
					}
				}
				new_i++;
			}
		}

		for (int i = 0; i < condMat.length; i++)
		{
			for (int j = 0; j < condMat[i].length; j++)
			{
				condMat[i][j] -= theta;
			}
		}
		if (debug)
		{
			MatrixVisualiser.showFrame(condMat);
		}
		return condMat;
	}
}
