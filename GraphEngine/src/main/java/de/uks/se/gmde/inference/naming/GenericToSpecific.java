package de.uks.se.gmde.inference.naming;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import de.uks.se.ArgumentNullException;
import de.uks.se.StringEx;
import de.uks.se.gmde.Node;

import java.util.Optional;

public class GenericToSpecific
{
	private static final Converter<String, String> KEBAB_TO_UPPER_CAMEL_CASE = CaseFormat.LOWER_HYPHEN.converterTo(CaseFormat.UPPER_CAMEL);
	private static final Converter<String, String> KEBAB_TO_LOWER_CAMEL_CASE = CaseFormat.LOWER_HYPHEN.converterTo(CaseFormat.LOWER_CAMEL);

	public static String inferAttributeName(String name)
	{
		return KEBAB_TO_LOWER_CAMEL_CASE.convert(name);
	}

	public static String inferPrivateFieldName(String name)
	{
		return inferAttributeName(name);
	}

	public static String inferAssocName(String name)
	{
		return KEBAB_TO_LOWER_CAMEL_CASE.convert(name);
	}

	public static String inferTypeName(Node node)
	{
		if (node == null)
			throw new ArgumentNullException("node");
		Optional<Object> nodeLabel = node.getAttribute(Node.LABEL_ATTRIBUTE_NAME);
		assert nodeLabel.isPresent();
		assert nodeLabel.get() instanceof String;
		String rawName = (String) nodeLabel.get();
		return KEBAB_TO_UPPER_CAMEL_CASE.convert(rawName);
	}

	public static String getFullQualifiedTypeName(Node node, String packageName)
	{
		if (StringEx.isNullOrWhiteSpace(packageName))
			return getFullQualifiedTypeName(node, StringEx.EMPTY);

		return packageName.length() == 0
			? inferTypeName(node)
			: String.join(".",
			packageName, inferTypeName(node));
	}

	public static String getCreatorTypeName(Node node, String packageName)
	{
		if (StringEx.isNullOrWhiteSpace(packageName))
			return getCreatorTypeName(node, StringEx.EMPTY);

		String creatorName = inferTypeName(node) + "Creator";
		return packageName.length() == 0
			? creatorName
			: String.join(".",
			packageName, "util", creatorName);
	}
}
