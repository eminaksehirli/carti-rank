package cart.kulua;

class Pair implements Comparable<Pair>
{
	int ix;
	double v;

	public Pair(int ix, double v)
	{
		this.ix = ix;
		this.v = v;
	}

	@Override
	public int compareTo(Pair o)
	{
		return Double.compare(this.v, o.v);
	}

	@Override
	public String toString()
	{
		return "{" + this.ix + ", " + this.v + "}";
	}
}