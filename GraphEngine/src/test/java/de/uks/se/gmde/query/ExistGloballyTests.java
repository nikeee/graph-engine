package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ExistGloballyTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullPredicate()
	{
		new ExistGlobally(null);
	}

	@Test
	public void existGloballyTrue()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistGlobally ag = new ExistGlobally(s -> true);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());

		LtsPath path = result.get();
		Assert.assertNotNull(path);
		Assert.assertTrue(path.size() > 0);

		Assert.assertEquals(host, path.getStart().get());
	}

	@Test
	public void existGloballyFalse()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistGlobally ag = new ExistGlobally(s -> false);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void existGloballyExactlyOneBoat()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistGlobally ag = new ExistGlobally(QueryTestUtil::exactlyOneBoatExists);
		Optional<LtsPath> result = ag.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());

		LtsPath path = result.get();
		Assert.assertNotNull(path);
		Assert.assertTrue(path.size() > 0);

		Assert.assertEquals(host, path.getStart().get());
	}
}
