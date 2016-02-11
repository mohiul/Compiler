package lex;

public class Pair<L, R> {
	private L l;
	private R r;

	public Pair(L l, R r) {
		this.l = l;
		this.r = r;
	}

	public L getL() {
		return l;
	}

	public R getR() {
		return r;
	}

	public void setL(L l) {
		this.l = l;
	}

	public void setR(R r) {
		this.r = r;
	}
	
	@Override
	public boolean equals(Object obj) {
		Pair<L, R> pair = (Pair<L, R>)obj;
		boolean equal = false;
		if (pair.getL() == l && pair.getR() == r)
			equal = true;
		return equal;
	}

	@Override
	public String toString() {
		return "State: " + l + " Token: " + r;
	}
}