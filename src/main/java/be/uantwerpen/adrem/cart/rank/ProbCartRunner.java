package be.uantwerpen.adrem.cart.rank;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.uantwerpen.adrem.fim.model.FrequentItemSet;
import be.uantwerpen.adrem.fim.model.PlainItemDB;
import be.uantwerpen.adrem.fim.model.PlainTransactionDB;
import mime.workers.assoc.BorgeltAdapter.TargetType;
import mime.workers.assoc.EclatBorgeltAdapter;
import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.model.CartifyProbDb;
import be.uantwerpen.adrem.cart.model.OneDimDissimilarity;
import be.uantwerpen.adrem.fim.model.PlainItem;

public class ProbCartRunner
{
	private int numOfDims;
	private CartifyProbDb cartDb;
	private InputFile inputFile;

	public ProbCartRunner(InputFile inputFile)
	{
		this.inputFile = inputFile;
		try
		{
			ArrayList<double[]> data = inputFile.getData();
			// dims = OneDCartifier.transpose(data);
			// // System.out.println("Dims data read and transposed");
			// origData = OneDCartifier.toPairs(data);
			// // System.out.println("Data pairs are created.");
			numOfDims = data.get(0).length;
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}

	}

	public List<int[]> runFor(int dimIx, int minSup, int minSize)
	{
		// List<Dissimilarity> measures = OneDimDissimilarity.forEach(numOfDims);
		List<OneDimDissimilarity> measures = Arrays.asList(
				new OneDimDissimilarity(dimIx), new OneDimDissimilarity(dimIx));
		cartDb = new CartifyProbDb(inputFile, measures);

		cartDb.cartify();
		PlainItemDB itemDb = cartDb.getBigTDb();
		PlainTransactionDB txDb = new PlainTransactionDB(itemDb);

		EclatBorgeltAdapter eclat = new EclatBorgeltAdapter(minSup,
				minSize,
				0,
				TargetType.Maximal_Item_Sets,
				txDb);
		List<FrequentItemSet> freqs = eclat.run();
		List<int[]> freqArrays = new ArrayList<>();
		for (FrequentItemSet freq : freqs)
		{
			int[] freqArr = new int[freq.size()];
			int counter = 0;
			for (PlainItem item : freq)
			{
				freqArr[counter++] = item.getId();
			}
			freqArrays.add(freqArr);
		}
		return freqArrays;
	}
}
