/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2015 Maxim Roncacé <mproncace@lapis.blue>
 *
 * The MIT License (MIT)
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all
 *     copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *     SOFTWARE.
 */
package net.amigocraft.mpt.util;

import static net.amigocraft.mpt.util.MiscUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class VersionComparisonTest {

	@Test
	public void testVersionEquality(){
		assertEquals(compareVersions("1", "1"), 0);
		assertEquals(compareVersions("1", "1.0"), 0);
		assertEquals(compareVersions("1", "1.0.0"), 0);
		assertEquals(compareVersions("1.0", "1"), 0);
		assertEquals(compareVersions("1.0", "1.0"), 0);
		assertEquals(compareVersions("1.0", "1.0.0"), 0);
		assertEquals(compareVersions("1.0.0", "1"), 0);
		assertEquals(compareVersions("1.0.0", "1.0"), 0);
		assertEquals(compareVersions("1.0.0", "1.0.0"), 0);
		assertEquals(compareVersions("1-alpha", "1-alpha"), 0);
	}

	@Test
	public void testMajorVersionGreater(){
		assertEquals(compareVersions("1", "2"), 1);
		assertEquals(compareVersions("1", "2.0"), 1);
		assertEquals(compareVersions("1", "2.0.0"), 1);
		assertEquals(compareVersions("1.0", "2"), 1);
		assertEquals(compareVersions("1.0", "2.0"), 1);
		assertEquals(compareVersions("1.0", "2.0.0"), 1);
		assertEquals(compareVersions("1.0.0", "2"), 1);
		assertEquals(compareVersions("1.0.0", "2.0"), 1);
		assertEquals(compareVersions("1.0.0", "2.0.0"), 1);
	}

	@Test
	public void testMinorVersionGreater(){
		assertEquals(compareVersions("1", "1.1"), 1);
		assertEquals(compareVersions("1", "1.1.0"), 1);
		assertEquals(compareVersions("1.0", "1.1"), 1);
		assertEquals(compareVersions("1.0", "1.1.0"), 1);
		assertEquals(compareVersions("1.0.0", "1.1"), 1);
		assertEquals(compareVersions("1.0.0", "1.1.0"), 1);
	}

	@Test
	public void testIncrementalVersionGreater(){
		assertEquals(compareVersions("1", "1.0.1"), 1);
		assertEquals(compareVersions("1.0", "1.0.1"), 1);
		assertEquals(compareVersions("1.0.0", "1.0.1"), 1);
	}

	@Test
	public void testQualifierGreater(){
		assertEquals(compareVersions("1-alpha", "1-beta"), 1);
		assertEquals(compareVersions("1-alpha", "1.0-beta"), 1);
		assertEquals(compareVersions("1-alpha", "1.0.0-beta"), 1);
		assertEquals(compareVersions("1.0-alpha", "1-beta"), 1);
		assertEquals(compareVersions("1.0-alpha", "1.0-beta"), 1);
		assertEquals(compareVersions("1.0-alpha", "1.0.0-beta"), 1);
		assertEquals(compareVersions("1.0.0-alpha", "1-beta"), 1);
		assertEquals(compareVersions("1.0.0-alpha", "1.0-beta"), 1);
		assertEquals(compareVersions("1.0.0-alpha", "1.0.0-beta"), 1);
	}

	@Test
	public void testMajorVersionLesser(){
		assertEquals(compareVersions("2", "1"), -1);
		assertEquals(compareVersions("2", "1.0"), -1);
		assertEquals(compareVersions("2", "1.0.0"), -1);
		assertEquals(compareVersions("2.0", "1"), -1);
		assertEquals(compareVersions("2.0", "1.0"), -1);
		assertEquals(compareVersions("2.0", "1.0.0"), -1);
		assertEquals(compareVersions("2.0.0", "1"), -1);
		assertEquals(compareVersions("2.0.0", "1.0"), -1);
		assertEquals(compareVersions("2.0.0", "1.0.0"), -1);
	}

	@Test
	public void testMinorVersionLesser(){
		assertEquals(compareVersions("1.1", "1"), -1);
		assertEquals(compareVersions("1.1", "1.0"), -1);
		assertEquals(compareVersions("1.1", "1.0.0"), -1);
		assertEquals(compareVersions("1.1.0", "1"), -1);
		assertEquals(compareVersions("1.1.0", "1.0"), -1);
		assertEquals(compareVersions("1.1.0", "1.0.0"), -1);
	}

	@Test
	public void testIncrementalVersionLesser(){
		assertEquals(compareVersions("1.0.1", "1"), -1);
		assertEquals(compareVersions("1.0.1", "1.0"), -1);
		assertEquals(compareVersions("1.0.1", "1.0.0"), -1);
	}

	@Test
	public void testQualifierLesser(){
		assertEquals(compareVersions("1-beta", "1-alpha"), -1);
		assertEquals(compareVersions("1-beta", "1.0-alpha"), -1);
		assertEquals(compareVersions("1-beta", "1.0.0-alpha"), -1);
		assertEquals(compareVersions("1.0-beta", "1-alpha"), -1);
		assertEquals(compareVersions("1.0-beta", "1.0-alpha"), -1);
		assertEquals(compareVersions("1.0-beta", "1.0.0-alpha"), -1);
		assertEquals(compareVersions("1.0.0-beta", "1-alpha"), -1);
		assertEquals(compareVersions("1.0.0-beta", "1.0-alpha"), -1);
		assertEquals(compareVersions("1.0.0-beta", "1.0.0-alpha"), -1);
	}

}
