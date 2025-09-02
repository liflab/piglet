package foo;

import java.util.HashSet;

class FooTest {
	@Test
	public void test1() {
		HashSet<String> set = new HashSet<String>();
		assertEquals(set, set);
	}
}
