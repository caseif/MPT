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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONPrettyPrinter extends HashMap {

	private static final long serialVersionUID = -9168577804652055206L;

	private static int column = 0;

	private static final int INDENT = 2;

	public static String toJSONString(Map map) throws IOException {

		StringBuilder sb = new StringBuilder();

		if (map == null){
			sb.append("null");
			return sb.toString();
		}

		boolean first = true;
		String newLine = System.getProperty("line.separator");

		Iterator iter = map.entrySet().iterator();

		sb.append('{');
		column++;

		sb.append(newLine);

		while (iter.hasNext()){
			if (first)
				first = false;
			else {
				sb.append(',');
				sb.append(newLine);
			}

			for (int i = 0; i < column * INDENT; i++)
				sb.append(' ');

			Map.Entry entry = (Map.Entry)iter.next();
			sb.append('\"');
			sb.append(JSONPrettyValue.escape(String.valueOf(entry.getKey())));
			sb.append('\"');
			sb.append(':');
			sb.append(' ');

			sb.append(entry.getValue() instanceof Map ?
					toJSONString((Map)entry.getValue()) :
					JSONPrettyValue.toPrettyJSONString(entry.getValue()));
		}

		sb.append(newLine);
		column--;

		for (int i = 0; i < column * INDENT; i++)
			sb.append(' ');

		sb.append('}');
		return sb.toString();
	}
}
