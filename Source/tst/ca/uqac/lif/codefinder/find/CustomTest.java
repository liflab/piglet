package ca.uqac.lif.codefinder.find;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import junit.framework.AssertionFailedError;

public class CustomTest
{
	@Test
	public void test1()
	{
		assertEquals(1, 2);
		//assertCustom(false);
	}
	
	protected static void assertCustom(Object o)
	{
		if (!(o instanceof Boolean) || !((Boolean) o).booleanValue())
		{
			Assert.fail("Assertion failed");
		}
	}
}
