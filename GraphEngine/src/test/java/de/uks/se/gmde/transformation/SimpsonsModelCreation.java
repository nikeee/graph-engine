package de.uks.se.gmde.transformation;

import de.uniks.networkparser.graph.Cardinality;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.DataType;
import de.uniks.networkparser.graph.Modifier;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sdmlib.models.classes.ClassModel;
import org.sdmlib.models.classes.Feature;

import java.io.File;
import java.io.IOException;

public class SimpsonsModelCreation
{
	private static final String TEST_PACKAGE_NAME = "de.uks.se.gmde.transformation.family";
	private static final String DEST_ROOT_DIR = "src/generated/java";
	private static final String PACKAGE_SUB_DIR = TEST_PACKAGE_NAME.replaceAll("\\.", "/");

	@Test
	public void createModel() throws IOException
	{
		ClassModel cm = new ClassModel(TEST_PACKAGE_NAME);
		Clazz family = cm.createClazz("Family")
			.withAttribute("name", DataType.STRING);

		Clazz familyMember = cm.createClazz("FamilyMember")
			.withAttribute("givenName", DataType.STRING);

		family.withUniDirectional(familyMember, "father", Cardinality.ONE)
			.withUniDirectional(familyMember, "mother", Cardinality.ONE)
			.withUniDirectional(familyMember, "sons", Cardinality.MANY)
			.withUniDirectional(familyMember, "daughters", Cardinality.MANY);

		Clazz personRegister = cm.createClazz("PersonRegister");
		Clazz person = cm.createClazz("Person")
			.withAttribute("fullName", DataType.STRING)
			.with(Modifier.ABSTRACT);
		Clazz man = cm.createClazz("Man").withSuperClazz(person);
		Clazz woman = cm.createClazz("Woman").withSuperClazz(person);

		personRegister.withUniDirectional(person, "persons", Cardinality.MANY);

		File destDir = new File(DEST_ROOT_DIR, PACKAGE_SUB_DIR);
		if (destDir.isDirectory())
			FileUtils.cleanDirectory(destDir);

		cm.withoutFeature(Feature.PATTERNOBJECT);
		cm.generate(DEST_ROOT_DIR);
	}
}
