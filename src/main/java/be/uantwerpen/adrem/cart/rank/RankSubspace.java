package be.uantwerpen.adrem.cart.rank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import be.uantwerpen.adrem.cart.io.InputFile;

public class RankSubspace
{
	private static final int ClusterPerDim = 40;

	public static class SubCluster
	{
		public int[] ids;
		public int[] dims;

		public SubCluster(int[] ids, int[] dims)
		{
			this.ids = ids;
			this.dims = dims;
		}

		@Override
		public String toString()
		{
			return "[" + ids.length + "] " + Arrays.toString(dims) + " "
					+ Arrays.toString(ids);
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
	private int[][][] dimThetaMatrices;
	private int maxDims;

	public static void main(String[] args) throws IOException
	{
		InputFile input = InputFile.forMime("/home/memin/research/data/synth/6c10d/6c10d.mime");
		// InputFile input =
		// InputFile.forMime("/home/memin/research/data/synth/6c10d/p12.mime");
		int numOfClusters = 4;
		int theta = 205;
		// final int startDimIx = 0;

		RankSubspace miner = new RankSubspace(input);
		List<SubCluster> tiles = miner.runFor(70, theta);

		System.out.println("Herro!");
		for (SubCluster tile : tiles)
		{
			System.out.println(tile);
		}
	}

	public RankSubspace(InputFile input)
	{
		this.input = input;
	}

	public List<SubCluster> runFor(int minLength, int theta, int maxDims)
			throws IOException
	{
		this.maxDims = maxDims;
		return runFor(minLength, theta);
	}

	public List<SubCluster> runFor(int minLength, int theta) throws IOException
	{
		allTiles = new ArrayList<>();
		this.numOfItems = input.getData().size();
		this.numOfDims = input.getData().get(0).length;

		if (maxDims == 0)
		{
			this.maxDims = numOfDims + 1;
		}
		this.minLength = minLength;

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
		dimThetaMatrices = new int[numOfDims][dimMatrices[0].length][dimMatrices[0][0].length];

		for (int i = 0; i < dimMatrices.length; i++)
		{
			for (int j = 0; j < dimMatrices[i].length; j++)
			{
				for (int j2 = 0; j2 < dimMatrices[i][j].length; j2++)
				{
					dimThetaMatrices[i][j][j2] = dimMatrices[i][j][j2] - theta;
				}
			}
		}

		for (int startDimIx = 0; startDimIx < numOfDims; startDimIx++)
		{
			RankTiler tiler_s = RankTiler.SquareExpander(input, numOfItems,
					startDimIx);

			Collection<Tile> c0 = tiler_s.runFor(theta, 1000);

			List<int[]> tileIds = new ArrayList<>();
			for (Tile tile : c0)
			{
				if (tile.er - tile.sr < minLength)
				{
					continue;
				}
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

			int[] freqDims = new int[]
			{ startDimIx };

			for (int[] tile : tileIds)
			{
				addCluster(tile, freqDims);
				checkForMore(tile, freqDims);
			}
		}
		return allTiles;
	}

	private void checkForMore(int[] tile, int[] freqDims)
	{
		final int firstCandidateDim = freqDims[freqDims.length - 1] + 1;

		for (int dimIx = firstCandidateDim; dimIx < numOfDims; dimIx++)
		{
			List<int[]> nextTiles = refineTile(dimIx, tile);

			// System.out.println("For dim " + dimIx + " and "
			// + Arrays.toString(freqDims) + ":" + nextTiles.size());

			int[] tileDims = Arrays.copyOf(freqDims, freqDims.length + 1);
			tileDims[tileDims.length - 1] = dimIx;

			for (int[] nextTile : nextTiles)
			{
				if (nextTile.length > minLength)
				{
					addCluster(nextTile, tileDims);
					if (freqDims.length < maxDims)
					{
						checkForMore(nextTile, tileDims);
					}
				}
			}
		}
	}

	private void addCluster(int[] nextTile, int[] tileDims)
	{
		allTiles.add(new SubCluster(nextTile, tileDims));
	}

	private List<int[]> refineTile(int dimIx, int[] ids)
	{
		List<int[]> nextTiles = new ArrayList<>();

		int[] loc2Ids_n = dimLoc2Ids[dimIx];

		boolean[] toAdd = new boolean[numOfItems];
		for (int id : ids)
		{
			toAdd[id] = true;
		}

		int[] ixs = new int[ids.length];
		int j = 0;
		for (int i = 0; i < loc2Ids_n.length; i++)
		{
			if (toAdd[loc2Ids_n[i]])
			{
				ixs[j] = i;
				j++;
			}
		}

		List<Tile> tiles = new ArrayList<>();
		for (int i = 0; i < ClusterPerDim; i++)
		{
			RankMatTiler tiler = new RankSquareFastExpander(ixs);
			Tile tile = tiler.findBestTile(dimThetaMatrices[dimIx]);
			if (tile == null)
			{
				break;
			}
			tiles.add(tile);

			int[] cluster = new int[tile.er - tile.sr + 1];
			for (int ij = tile.sr; ij <= tile.er; ij++)
			{
				cluster[ij - tile.sr] = loc2Ids_n[ixs[ij]];
			}
			Arrays.sort(cluster);
			nextTiles.add(cluster);

			int newSize = ixs.length - tile.er + tile.sr - 1;
			int[] newIxs = new int[newSize];

			for (int ij = 0; ij < tile.sr; ij++)
			{
				newIxs[ij] = ixs[ij];
			}
			for (int ij = tile.er + 1; ij < ixs.length; ij++)
			{
				newIxs[ij - tile.er - 1] = ixs[ij];
			}
			ixs = newIxs;
			if (ixs.length < minLength)
			{
				break;
			}
		}

		return nextTiles;
	}
}
