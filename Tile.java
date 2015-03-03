package cart.kulua;

import cern.colt.Arrays;

class Tile implements Comparable<Tile>
{
	double score;
	int[] rows, cols;
	private IdMapper mapper;

	Tile(double score, int[] rows, int[] cols)
	{
		this.score = score;
		this.rows = rows;
		this.cols = cols;
		this.mapper = new DirectMapper();
	}

	void setMapper(IdMapper mapper)
	{
		this.mapper = mapper;
	}

	@Override
	public int compareTo(Tile o)
	{
		return Double.compare(score, o.score);
	}

	@Override
	public String toString()
	{
		return score + ", " + Arrays.toString(mapper.map(rows)) + "~"
				+ Arrays.toString(mapper.map(cols));
	}

	static interface IdMapper
	{
		int[] map(int[] ids);
	}

	private static class DirectMapper implements IdMapper
	{
		@Override
		public int[] map(int[] ids)
		{
			return ids;
		}

	}
}