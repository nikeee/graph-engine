package de.uks.se.gmde.inference.naming;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public class SpecificToGeneric
{
	private static final Converter<String, String> UPPER_CAMEL_TO_KEBAB_CASE = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);
	private static final Converter<String, String> LOWER_CAMEL_TO_KEBAB_CASE = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);

	public static String inferAttributeName(String name)
	{
		return LOWER_CAMEL_TO_KEBAB_CASE.convert(name);
	}

	public static String edgeLabel(String name)
	{
		return LOWER_CAMEL_TO_KEBAB_CASE.convert(name);
	}

	public static String inferNodeLabel(String typeName)
	{
		return UPPER_CAMEL_TO_KEBAB_CASE.convert(typeName);
	}

	public static String getCreatorFullTypeName(Class<?> forType)
	{
		String packageName = forType.getPackage().getName();
		return String.join(".",
			packageName, "util", forType.getSimpleName() + "Creator"
		);
	}
}
