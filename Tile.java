package cart.kulua;

class Tile implements Comparable<Tile>
{
	double score;
	// int[] rows, cols;
	private IdMapper mapper;
	final int sr;
	final int sc;
	final int er;
	final int ec;

	Tile(double score, int sr, int sc, int er, int ec)
	{
		this.score = score;
		this.sr = sr;
		this.sc = sc;
		this.er = er;
		this.ec = ec;
		this.mapper = new DirectMapper();
	}

	// Tile(double score, int[] rows, int[] cols)
	// {
	// this.score = score;
	// this.rows = rows;
	// this.cols = cols;
	// this.mapper = new DirectMapper();
	// }

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
		return String.format("%f, [%d-%d]~[%d-%d]", score, sr, er, sc, ec);
	}

	// @Override
	// public String toString()
	// {
	// return score + ", " + Arrays.toString(mapper.map(rows)) + "~"
	// + Arrays.toString(mapper.map(cols));
	// }

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

	public int[] cols()
	{
		int[] cols = new int[ec - sc + 1];
		for (int c = sc; c <= ec; c++)
		{
			cols[c - sc] = c;
		}
		return mapper.map(cols);
	}
}