package de.uks.se.gmde.transformation;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.rendering.GraphDumper;
import de.uks.se.gmde.transformation.family.Family;
import de.uks.se.gmde.transformation.family.PersonRegister;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.uks.se.gmde.inference.ClassModelCreator.genericToSpecific;
import static de.uks.se.gmde.inference.ClassModelCreator.specificToGeneric;

public class TransformPersonRegisterToFamilyTest
{
	@Test
	public void transformation() throws IOException
	{
		PersonRegister register = SimpsonsUtil.createPersonRegister();

		Graph generic = specificToGeneric(register);
		GraphDumper.dumpGraph(generic, "dumps/person-register-to-family/generic-original.svg");

		Graph transformed = personRegisterToFamiliesTransformation(generic);
		GraphDumper.dumpGraph(transformed, "dumps/person-register-to-family/generic-transformed.svg");

		Map<Node, Object> nodeObjectMap = genericToSpecific(transformed, Family.class.getPackage().getName());
		Assert.assertNotNull(nodeObjectMap);
		Assert.assertEquals(6, nodeObjectMap.size()); // Marge, Lisa, Maggie, Homer, Bart + Family
	}

	public static Graph personRegisterToFamiliesTransformation(final Graph genericIn)
	{
		if (genericIn == null)
			throw new ArgumentNullException("genericIn");

		// Get the "root" node PersonRegister (do not loop through all nodes in the graph, since this is wrong)
		Node personRegister = genericIn.getNodes().stream()
			.filter(n -> "person-register".equals(n.getAttribute(Node.LABEL_ATTRIBUTE_NAME).orElse(null)))
			.findFirst()
			.get();

		Set<String> allFullNames = getFullNames(personRegister, "persons");
		Set<String> lastNames = getLastNames(allFullNames);

		Graph genericOut = new Graph();

		// Create a node for each last name
		Map<String, Node> familyOutNodes = lastNames.stream().collect(Collectors.toMap(
			lastName -> lastName,
			lastName -> genericOut.createNode().withLabel("Family").withAttr("name", lastName)
		));

		Set<Node> menIn = getAllNodesOfLabel(genericIn, "man");
		Set<Node> womenIn = getAllNodesOfLabel(genericIn, "woman");

		Map<String, List<Node>> familyToMenMap = createFamilyToPersonMap(menIn);
		Map<String, List<Node>> familyToWomenMap = createFamilyToPersonMap(womenIn);

		final String IN_FULL_NAME_ATTR = "full-name";
		final String OUT_FAMILY_MEMBER_LABEL = "family-member";
		final String OUT_GIVEN_NAME_ATTR = "given-name";

		familyOutNodes.forEach((familyName, familyOutNode) -> {
			List<Node> menInFamily = familyToMenMap.get(familyName);
			if (menInFamily != null)
			{
				int menCounter = 0;
				for (Node man : menInFamily)
				{
					assert man != null;

					String firstName = getFirstName((String) man.getAttribute(IN_FULL_NAME_ATTR).get());
					Node manOut = genericOut.createNode().withLabel(OUT_FAMILY_MEMBER_LABEL)
						.withAttr(OUT_GIVEN_NAME_ATTR, firstName);

					String assoc = menCounter == 0
						? "father"
						: "sons";
					familyOutNode.createEdge(assoc, manOut);
					++menCounter;
				}
			}

			List<Node> womenInFamily = familyToWomenMap.get(familyName);
			if (womenInFamily != null)
			{
				int womenCounter = 0;
				for (Node woman : womenInFamily)
				{
					assert woman != null;

					String firstName = getFirstName((String) woman.getAttribute(IN_FULL_NAME_ATTR).get());
					Node manOut = genericOut.createNode().withLabel(OUT_FAMILY_MEMBER_LABEL)
						.withAttr(OUT_GIVEN_NAME_ATTR, firstName);

					String assoc = womenCounter == 0
						? "mother"
						: "daughters";
					familyOutNode.createEdge(assoc, manOut);
					++womenCounter;
				}
			}
		});

		return genericOut;
	}

	private static Set<String> getFullNames(Node personRegister, String edgeLabel)
	{
		assert personRegister != null;
		assert edgeLabel != null;

		return personRegister.getEdges(edgeLabel).stream()
			.map(n -> n.getAttribute("full-name"))
			.filter(Optional::isPresent)
			.map(n -> n.get())
			.filter(n -> n instanceof String)
			.map(n -> (String) n)
			.collect(Collectors.toSet());
	}

	private static Set<String> getLastNames(Set<String> fullNames)
	{
		assert fullNames != null;
		return fullNames.stream()
			.filter(Objects::nonNull)
			.map(TransformPersonRegisterToFamilyTest::getLastName)
			.collect(Collectors.toSet());
	}

	private static String getLastName(String fullName)
	{
		assert fullName != null;

		String lastName = null;
		for (String namePart : fullName.split(" "))
			if (namePart.trim().length() > 0)
				lastName = namePart.trim();
		return lastName;
	}

	private static String getFirstName(String fullName)
	{
		assert fullName != null;

		for (String namePart : fullName.split(" "))
			if (namePart.trim().length() > 0)
				return namePart.trim();
		return null;
	}

	private static Set<Node> getAllNodesOfLabel(Graph in, Object labelValue)
	{
		assert in != null;
		assert labelValue != null;

		return in.getNodes().stream()
			.filter(n -> labelValue.equals(n.getAttribute(Node.LABEL_ATTRIBUTE_NAME).orElse(null)))
			.collect(Collectors.toSet());
	}

	private static Map<String, List<Node>> createFamilyToPersonMap(Set<Node> persons)
	{
		return persons.stream().collect(Collectors.groupingBy(
			m -> getLastName((String) m.getAttribute("full-name").get())
		));
	}
}
