package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class AlwaysGloballyTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullPredicate()
	{
		new AlwaysGlobally(null);
	}

	@Test
	public void alwaysGloballyTrue()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysGlobally ag = new AlwaysGlobally(s -> true);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void alwaysGloballySuccessful1()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysGlobally ag = new AlwaysGlobally(QueryTestUtil::exactlyOneBoatExists);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void alwaysGloballyUnsuccessful()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysGlobally ag = new AlwaysGlobally(QueryTestUtil::goatAtLeftSide);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());

		LtsPath path = result.get();
		Assert.assertNotNull(path);
		Assert.assertTrue(path.size() > 0);


		// Query asks if the goat is always at the left side
		// The last element of the path should not satisfy predicate p
		// The first element should be the start graph
		Assert.assertEquals(host, path.getStart().get());
		Assert.assertFalse(QueryTestUtil.goatAtLeftSide(path.getEnd().get()));
	}
}
