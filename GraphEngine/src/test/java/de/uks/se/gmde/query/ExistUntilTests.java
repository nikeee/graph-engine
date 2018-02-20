package de.uks.se.gmde.query;

import de.uks.se.ArgumentNullException;
import de.uks.se.gmde.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ExistUntilTests
{
	@Test(expected = ArgumentNullException.class)
	public void nullPredicate0()
	{
		new ExistUntil(null, s -> true);
	}

	@Test(expected = ArgumentNullException.class)
	public void nullPredicate1()
	{
		new ExistUntil(s -> true, null);
	}

	@Test(expected = ArgumentNullException.class)
	public void nullPredicate2()
	{
		new ExistUntil(null, null);
	}

	@Test
	public void existUntil()
	{
		Graph host = QueryTestUtil.createHostInLts();

		ExistUntil eu = new ExistUntil(QueryTestUtil::goatAtLeftSide, QueryTestUtil::boatIsFull);
		Optional<LtsPath> result = eu.test(host);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isPresent()); // There is a path where the goat is the last item put on the boat

		LtsPath path = result.get();
		Assert.assertNotNull(path);

		Assert.assertEquals(host, path.getStart().get());
		Assert.assertTrue(QueryTestUtil.boatIsFull(path.getEnd().get()));
	}
}
