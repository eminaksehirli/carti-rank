package cart.kulua;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import be.uantwerpen.adrem.cart.io.InputFile;
import be.uantwerpen.adrem.cart.maximizer.CartiMaximizer;
import be.uantwerpen.adrem.cart.maximizer.OneDCartifier;

public class OneDimRunner
{
	public static void main(String[] args) throws IOException
	{
		final String fileName = "/home/memin/Private/workspace/o/go/src/gitlab.com/eminaksehirli/kulua/2c2d_12.mime";
		InputFile input = InputFile.forMime(fileName);

		ArrayList<double[]> data = input.getData();
		double[][] dims = OneDCartifier.transpose(data);

		for (int minLen = 2; minLen < 11; minLen++)
		{
			System.out.println("minLen: " + minLen);
			CartiMaximizer cm = new CartiMaximizer();
			Map<Integer, Integer> freqs = cm.mineOneDim(dims[0], minLen);

			for (Entry<Integer, Integer> freq : freqs.entrySet())
			{
				System.out.println(freq.getValue() + " => " + freq.getKey());
			}
		}
	}
}
