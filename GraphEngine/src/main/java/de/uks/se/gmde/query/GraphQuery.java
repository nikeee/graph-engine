package de.uks.se.gmde.query;

import de.uks.se.gmde.Graph;

import java.util.Optional;

public interface GraphQuery<TResult>
{
	Optional<TResult> test(Graph state);
}
