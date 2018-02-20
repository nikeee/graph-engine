package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class AlwaysNextTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullPredicate()
	{
		new AlwaysNext(null);
	}

	@Test
	public void alwaysNextTrue()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysNext ag = new AlwaysNext(s -> true);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void alwaysNextExactlyOneBoat()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysNext ag = new AlwaysNext(QueryTestUtil::exactlyOneBoatExists);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void alwaysNextUnsuccessful()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysNext ag = new AlwaysNext(QueryTestUtil::goatAtLeftSide);
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
