package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ExistNextTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullPredicate()
	{
		new ExistNext(null);
	}

	@Test
	public void existNextTrue()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistNext ag = new ExistNext(s -> true);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());

		LtsPath path = result.get();
		Assert.assertNotNull(path);
		Assert.assertTrue(path.size() > 0);

		Assert.assertEquals(host, path.getStart().get());
	}

	@Test
	public void existNextFalse()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistNext ag = new ExistNext(s -> false);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void existNextExactlyOneBoat()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistNext ag = new ExistNext(QueryTestUtil::exactlyOneBoatExists);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());

		LtsPath path = result.get();
		Assert.assertNotNull(path);
		Assert.assertTrue(path.size() > 0);

		Assert.assertEquals(host, path.getStart().get());
	}
}
