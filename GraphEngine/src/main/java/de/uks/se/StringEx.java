package de.uks.se;

public class StringEx
{
	public static final String EMPTY = "";
	private static final String NULL_STRING = "<null>";

	public static String escapeNullString(final Object value)
	{
		return value == null ? NULL_STRING : value.toString();
	}

	public static String escapeNullString(final String value)
	{
		return value == null ? NULL_STRING : value;
	}

	public static boolean isNullOrWhiteSpace(final String value)
	{
		return value == null || value.trim().length() == 0;
	}
}
