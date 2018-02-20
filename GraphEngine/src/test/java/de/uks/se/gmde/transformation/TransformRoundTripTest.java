package de.uks.se.gmde.transformation;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.rendering.GraphDumper;
import de.uks.se.gmde.transformation.family.Family;
import de.uks.se.gmde.transformation.family.PersonRegister;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static de.uks.se.gmde.inference.ClassModelCreator.genericToSpecific;
import static de.uks.se.gmde.inference.ClassModelCreator.specificToGeneric;

public class TransformRoundTripTest
{
	@Test
	public void transformation() throws IOException
	{
		Family simpsons = SimpsonsUtil.createFamily();

		Graph generic = specificToGeneric(simpsons);
		GraphDumper.dumpGraph(generic, "dumps/round-trip/0-family-generic-original.svg");

		Graph genericTransformed = TransformFamilyToPersonRegisterTest.familyToPersonRegisterTransformation(generic);
		GraphDumper.dumpGraph(genericTransformed, "dumps/round-trip/1-family-generic-transformed.svg");

		Map<Node, Object> specificTransformed = genericToSpecific(genericTransformed, PersonRegister.class.getPackage().getName());
		Assert.assertNotNull(specificTransformed);
		Assert.assertEquals(6, specificTransformed.size()); // Marge, Lisa, Maggie, Homer, Bart + PersonRegister

		PersonRegister personRegister = specificTransformed.values().stream()
			.filter(o -> o instanceof PersonRegister)
			.map(o -> (PersonRegister) o)
			.findFirst()
			.get();

		Graph genericReverse = specificToGeneric(personRegister);
		GraphDumper.dumpGraph(genericReverse, "dumps/round-trip/2-person-register-generic-original.svg");

		Graph genericReverseTransformed = TransformPersonRegisterToFamilyTest.personRegisterToFamiliesTransformation(genericReverse);
		GraphDumper.dumpGraph(genericReverseTransformed, "dumps/round-trip/3-person-register-generic-transformed.svg");

		Map<Node, Object> nodeObjectMap = genericToSpecific(genericReverseTransformed, Family.class.getPackage().getName());
		Assert.assertNotNull(nodeObjectMap);
		Assert.assertEquals(6, nodeObjectMap.size()); // Marge, Lisa, Maggie, Homer, Bart + Family
	}
}
