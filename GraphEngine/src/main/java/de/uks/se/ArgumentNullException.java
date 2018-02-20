package de.uks.se;

public class ArgumentNullException extends IllegalArgumentException
{
	public ArgumentNullException(String argumentName)
	{
		super("Argument " + argumentName + " is null.");
	}
}
