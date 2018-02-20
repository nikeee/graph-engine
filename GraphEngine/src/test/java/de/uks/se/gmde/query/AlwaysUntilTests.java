package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class AlwaysUntilTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullPredicate0()
	{
		new AlwaysUntil(null, s -> true);
	}

	@Test(expected = ArgumentNullException.class)
	public void nullPredicate1()
	{
		new AlwaysUntil(s -> true, null);
	}

	@Test(expected = ArgumentNullException.class)
	public void nullPredicate2()
	{
		new AlwaysUntil(null, null);
	}

	@Test
	public void alwaysUntilSuccessful()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysUntil au = new AlwaysUntil(s -> !QueryTestUtil.leftSideIsEmpty(s), QueryTestUtil::boatIsFull);
		Optional<LtsPath> result = au.test(host);
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void alwaysUntilUnsuccessful()
	{
		Graph host = QueryTestUtil.createHostInLts();

		AlwaysUntil au = new AlwaysUntil(QueryTestUtil::goatAtLeftSide, QueryTestUtil::boatIsFull);
		Optional<LtsPath> result = au.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent());

		LtsPath path = result.get();
		Assert.assertNotNull(path);
		Assert.assertTrue(path.size() > 0);

		Assert.assertEquals(host, path.getStart().get());
		Assert.assertFalse(QueryTestUtil.boatIsFull(path.getEnd().get()));
	}
}
