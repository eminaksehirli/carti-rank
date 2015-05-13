package cart.kulua;

import static be.uantwerpen.adrem.cart.maximizer.MaximalMinerCombiner.getId2Ord;
import static be.uantwerpen.adrem.cart.maximizer.MaximalMinerCombiner.getOrd2Id;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.maximizer.OneDCartifier;
import be.uantwerpen.adrem.cart.model.Pair;

public class BinaryTiler
{
	private double[][] dims;
	private Pair[][] origData;
	private int numOfDims;
	private Pair[][] orderedDims;
	private int[][] ids2Orders;
	private Item[][] allItems;

	public BinaryTiler(InputFile inputFile)
	{
		try
		{
			ArrayList<double[]> data = inputFile.getData();
			dims = OneDCartifier.transpose(data);
			// System.out.println("Dims data read and transposed");
			origData = OneDCartifier.toPairs(data);
			// System.out.println("Data pairs are created.");
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}

		numOfDims = dims.length;

		orderedDims = new Pair[dims.length][];
		ids2Orders = new int[dims.length][];
		for (int dimIx = 0; dimIx < numOfDims; dimIx++)
		{
			orderedDims[dimIx] = getOrd2Id(origData, dimIx);
			ids2Orders[dimIx] = getId2Ord(orderedDims[dimIx]);
		}
	}

	public List<Tile> runFor(int dimIx, int k, int topK)
	{
		convertToItems(k);
		Item[] items = new Item[allItems[dimIx].length];

		for (int i = 0; i < allItems[dimIx].length; i++)
		{
			final Item item = allItems[dimIx][i];
			items[ids2Orders[dimIx][item.id]] = item;
		}

		List<Tile> tiles = new ArrayList<>(topK);
		for (int tileIx = 0; tileIx < topK; tileIx++)
		{
			Tile tile = findMaxTile(items);
			int sc = ids2Orders[dimIx][items[tile.sc].id];
			int ec = ids2Orders[dimIx][items[tile.ec].id];
			Tile fixedTile = new Tile(tile.score, tile.sr, sc, tile.er, ec);

			tiles.add(fixedTile);

			final int ofset = tile.ec - tile.sc + 1;

			if (ofset >= items.length)
			{
				break;
			}
			Item[] remainingItems = new Item[items.length - ofset];
			for (int i = 0; i < tile.sc; i++)
			{
				remainingItems[i] = items[i];
			}
			for (int i = tile.ec + 1; i < items.length; i++)
			{
				remainingItems[i - ofset] = items[i];
			}
			items = remainingItems;
		}
		return tiles;
	}

	protected Tile findMaxTile(Item[] items)
	{
		// int minSup = minLen;
		// Map<Integer, Integer> maxes = new HashMap<>();

		int[] maxTile = new int[2];
		int maxScore = 0;

		for (int start = 0; start < items.length - 1; start++)
		{
			int end = start + 1;

			while (end < items.length - 1 && items[end].txS == items[end + 1].txS)
			{
				end++;
			}

			for (; end < items.length && items[end].txS <= items[start].txE; end++)
			{
				int score = (end - start) * (items[start].txE - items[end].txS);
				if (score > maxScore)
				{
					maxScore = score;
					maxTile[0] = start;
					maxTile[1] = end;
				}
			}
		}
		int start = maxTile[0];
		int end = maxTile[1];
		Tile tile = new Tile(maxScore, items[end].txS, start, items[start].txE, end);
		return tile;
	}

	private void convertToItems(int k)
	{
		Item[][] nAllItems = new Item[numOfDims][];
		for (int dimIx = 0; dimIx < numOfDims; dimIx++)
		{
			Item[] dimItems = new Item[dims[dimIx].length];
			for (int i = 0; i < dimItems.length; i++)
			{
				dimItems[i] = new Item(i);
			}
			int[] cartStarts = OneDCartifier.findCartStarts(dims[dimIx], k, false);
			for (int order = 0; order < cartStarts.length; order++)
			{
				for (int itemIx = cartStarts[order]; itemIx < cartStarts[order] + k; itemIx++)
				{
					dimItems[orderedDims[dimIx][itemIx].ix].addTid(order);
				}
			}
			nAllItems[dimIx] = dimItems;
		}
		allItems = nAllItems;
	}

	protected static class Item
	{
		int id;
		int txS = Integer.MAX_VALUE, txE;

		public Item(int id)
		{
			this.id = id;
		}

		public void addTid(int tid)
		{
			if (tid < txS)
			{
				txS = tid;
			}
			if (tid > txE)
			{
				txE = tid;
			}
		}

		@Override
		public String toString()
		{
			return "[" + id + ", " + txS + "=>" + txE + "]";
		}
	}
}
