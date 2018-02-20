package de.uks.se.gmde.problem.cannibal;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.inference.ClassModelCreator;
import de.uks.se.gmde.inferred.Bank;
import de.uks.se.gmde.inferred.Boat;
import de.uks.se.gmde.inferred.Human;
import de.uks.se.gmde.rendering.GraphDumper;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sdmlib.models.classes.ClassModel;
import org.sdmlib.models.classes.Feature;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CannibalInference
{
	private static final String TEST_PACKAGE_NAME = "de.uks.se.gmde.inferred";
	private static final String DEST_ROOT_DIR = "src/generated/java";
	private static final String PACKAGE_SUB_DIR = TEST_PACKAGE_NAME.replaceAll("\\.", "/");

	@Test
	public void classModelInference() throws IOException
	{
		Graph start = CannibalTest.createClassModelHost();
		ClassModel cm = ClassModelCreator.inferClassModel(start, TEST_PACKAGE_NAME);

		File destDir = new File(DEST_ROOT_DIR, PACKAGE_SUB_DIR);
		if (destDir.isDirectory())
			FileUtils.cleanDirectory(destDir);

		cm.withoutFeature(Feature.PATTERNOBJECT);
		cm.generate(DEST_ROOT_DIR);

		GraphDumper.dumpGraph(start, "dumps/cannibal-class-instances.svg");
	}

	@Test
	public void genericToSpecific()
	{
		Graph start = CannibalTest.createClassModelHost();
		Map<Node, Object> nodeObjectMap = ClassModelCreator.genericToSpecific(start, TEST_PACKAGE_NAME);
		Assert.assertNotNull(nodeObjectMap);
		Assert.assertEquals(13, nodeObjectMap.size());
	}

	@Test
	public void specificToGeneric() throws IOException
	{
		Boat boat = new Boat().withLabel("boat");
		Bank leftBank = new Bank().withLabel("bank")
			.withLocation("left");
		Bank rightBank = new Bank().withLabel("bank")
			.withLocation("right")
			.withNotEqual(leftBank);

		leftBank.withNotEqual(rightBank);

		boat.withAt(leftBank);

		Human missionary0 = new Human()
			.withLabel("missionary")
			.withCanDrive(true)
			.withAt(leftBank);
		Human missionary1 = new Human()
			.withLabel("missionary")
			.withCanDrive(true)
			.withAt(leftBank);
		Human missionary2 = new Human()
			.withLabel("missionary")
			.withCanDrive(true)
			.withAt(leftBank);
		Human cannibal0 = new Human()
			.withLabel("cannibal")
			.withCanDrive(true)
			.withAt(leftBank);
		Human cannibal1 = new Human()
			.withLabel("cannibal")
			.withCanDrive(false)
			.withAt(leftBank);
		Human cannibal2 = new Human()
			.withLabel("cannibal")
			.withCanDrive(false)
			.withAt(leftBank);

		Graph g = ClassModelCreator.specificToGeneric(
			boat,
			missionary0,
			missionary1,
			missionary2,
			cannibal0,
			cannibal1,
			cannibal2
		);
		Assert.assertNotNull(g);
		Assert.assertEquals(9, g.getNodes().size());
		GraphDumper.dumpGraph(g, "dumps/cannibal-specific-to-generic.svg");
	}
}
