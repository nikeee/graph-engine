package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;

public class Destination
{
	public final String via;
	public final Graph destination;

	Destination(String via, Graph destination)
	{
		if (destination == null)
			throw new ArgumentNullException("destination");

		this.via = via;
		this.destination = destination;
	}
}
