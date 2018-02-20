package de.uks.se.gmde.inference;

import de.uks.se.ArgumentNullException;
import de.uniks.networkparser.graph.DataType;

public class TypeSystem
{
	static DataType inferDataType(Object value)
	{
		if (value instanceof Integer)
			return DataType.INT;
		if (value instanceof String)
			return DataType.STRING;
		if (value instanceof Boolean)
			return DataType.BOOLEAN;
		if (value instanceof Character)
			return DataType.CHAR;
		if (value instanceof Double)
			return DataType.DOUBLE;
		if (value instanceof Float)
			return DataType.FLOAT;
		if (value instanceof Long)
			return DataType.LONG;
		if (value instanceof Byte)
			return DataType.BYTE;
		throw new IllegalArgumentException("value has no supported type");
	}

	/**
	 * Takes two DataTypes and returns a type that can contain instances of both types.
	 * E. g. (INT, LONG) -> LONG
	 */
	static DataType widenType(DataType reference, DataType toFit)
	{
		if (reference == null)
			throw new ArgumentNullException("reference");
		if (toFit == null)
			throw new ArgumentNullException("reference");

		if (reference.equals(toFit))
			return reference;

		if (DataType.BOOLEAN.equals(reference))
		{
			return DataType.OBJECT;
		}
		else if (DataType.CHAR.equals(reference))
		{
			if (DataType.STRING.equals(toFit))
				return DataType.STRING;
			return DataType.OBJECT;
		}
		else if (DataType.DOUBLE.equals(reference))
		{
			if (DataType.FLOAT.equals(toFit)
				|| DataType.LONG.equals(toFit)
				|| DataType.INT.equals(toFit)
				|| DataType.BYTE.equals(toFit))
				return DataType.DOUBLE;
			return DataType.OBJECT;
		}
		else if (DataType.FLOAT.equals(reference))
		{
			if (DataType.DOUBLE.equals(toFit))
				return DataType.DOUBLE;
			if (DataType.LONG.equals(toFit)
				|| DataType.INT.equals(toFit)
				|| DataType.BYTE.equals(toFit))
				return DataType.FLOAT;
			return DataType.OBJECT;
		}
		else if (DataType.LONG.equals(reference))
		{
			if (DataType.INT.equals(toFit)
				|| DataType.BYTE.equals(toFit))
				return DataType.LONG;
			if (DataType.DOUBLE.equals(toFit))
				return DataType.DOUBLE;
			if (DataType.FLOAT.equals(toFit))
				return DataType.FLOAT;
			return DataType.OBJECT;
		}
		else if (DataType.INT.equals(reference))
		{
			if (DataType.LONG.equals(toFit))
				return DataType.LONG;
			if (DataType.BYTE.equals(toFit))
				return DataType.INT;
			if (DataType.DOUBLE.equals(toFit))
				return DataType.DOUBLE;
			if (DataType.FLOAT.equals(toFit))
				return DataType.FLOAT;
			return DataType.OBJECT;
		}
		else if (DataType.STRING.equals(reference))
		{
			if (DataType.CHAR.equals(toFit))
				return DataType.STRING;
			return DataType.OBJECT;
		}
		else if (DataType.BYTE.equals(reference))
		{
			if (DataType.INT.equals(toFit))
				return DataType.INT;
			if (DataType.LONG.equals(toFit))
				return DataType.LONG;
			if (DataType.DOUBLE.equals(toFit))
				return DataType.DOUBLE;
			if (DataType.FLOAT.equals(toFit))
				return DataType.FLOAT;
			return DataType.OBJECT;
		}
		else if (DataType.OBJECT.equals(reference))
		{
			return DataType.OBJECT;
		}
		throw new IllegalArgumentException("value has no supported type");
	}
}
