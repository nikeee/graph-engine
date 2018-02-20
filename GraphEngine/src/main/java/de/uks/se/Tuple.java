package de.uks.se;

import java.util.Objects;

public class Tuple<T, U>
{
	public final T item0;
	public final U item1;

	public Tuple(T item0, U item1)
	{
		this.item0 = item0;
		this.item1 = item1;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Tuple<?, ?> tuple = (Tuple<?, ?>) o;
		return Objects.equals(item0, tuple.item0) &&
			Objects.equals(item1, tuple.item1);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(item0, item1);
	}

	@Override
	public String toString()
	{
		return "Tuple(" +
			"item0=" + StringEx.escapeNullString(item0) + ", " +
			"item1=" + StringEx.escapeNullString(item1) +
			")";
	}
}
