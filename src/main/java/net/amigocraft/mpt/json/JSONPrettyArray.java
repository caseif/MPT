/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014 Maxim Roncacé <https://github.com/mproncace>
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
package net.amigocraft.mpt.json;

import org.json.simple.JSONArray;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class JSONPrettyArray extends JSONArray {

	public static String toPrettyJSONString(List list) throws IOException {

		StringBuilder sb = new StringBuilder();

		if (list == null){
			sb.append("null");
			return sb.toString();
		}

		boolean first = true;
		Iterator iter = list.iterator();

		sb.append('[');
		while (iter.hasNext()){
			if (first)
				first = false;
			else
				sb.append(',');

			Object value = iter.next();
			if (value == null){
				sb.append("null");
				continue;
			}

			sb.append(JSONPrettyValue.toPrettyJSONString(value));
		}
		sb.append(']');
		return sb.toString();
	}
}