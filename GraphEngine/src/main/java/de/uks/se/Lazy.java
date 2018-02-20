package de.uks.se;

import java.util.function.Supplier;

/**
 * THIS IS NOT THREAD SAFE
 *
 * @param <T>
 */
public class Lazy<T>
{
	private T instance = null;
	private final Supplier<T> supplier;

	public Lazy(final Supplier<T> supplier)
	{
		if (supplier == null)
			throw new ArgumentNullException("supplier");

		this.supplier = supplier;
	}

	public T get()
	{
		if (instance == null)
			instance = supplier.get();

		assert instance != null; // Check for thready-sanity
		return instance;
	}
}
