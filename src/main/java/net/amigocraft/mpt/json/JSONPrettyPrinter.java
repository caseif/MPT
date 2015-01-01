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
package net.amigocraft.mpt.json;

import org.json.simple.JSONAware;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONPrettyPrinter extends HashMap {

	private static final long serialVersionUID = -9168577804652055206L;

	static int column = 0;

	static final int INDENT = 2;

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
			sb.append(escape(String.valueOf(entry.getKey())));
			sb.append('\"');
			sb.append(':');
			sb.append(' ');

			sb.append(entry.getValue() instanceof Map ?
					toJSONString((Map)entry.getValue()) :
					valueToJsonString(entry.getValue()));
		}

		sb.append(newLine);
		column--;

		for (int i = 0; i < column * INDENT; i++)
			sb.append(' ');

		sb.append('}');
		return sb.toString();
	}

	public static String listToJsonString(List list) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (list == null){
			sb.append("null");
			return sb.toString();
		}

		boolean first = true;
		Iterator iter = list.iterator();

		String newLine = System.getProperty("line.separator");

		sb.append('[');
		while (iter.hasNext()){
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append(newLine);
			for (int i = 0; i < column * INDENT + 4; i++)
				sb.append(' ');

			Object value = iter.next();
			if (value == null){
				sb.append("null");
				continue;
			}

			sb.append(valueToJsonString(value));
		}
		sb.append(newLine);
		for (int i = 0; i < column * INDENT; i++)
			sb.append(' ');
		sb.append(']');
		return sb.toString();
	}

	public static String valueToJsonString(Object value) throws IOException {
		if(value == null)
			return "null";

		if(value instanceof String)
			return "\"" + escape((String)value)+"\"";

		if(value instanceof Double){
			if(((Double)value).isInfinite() || ((Double)value).isNaN())
				return "null";
			else
				return value.toString();
		}

		if(value instanceof Float){
			if(((Float)value).isInfinite() || ((Float)value).isNaN())
				return "null";
			else
				return value.toString();
		}

		if (value instanceof Number)
			return value.toString();
		if (value instanceof Boolean)
			return value.toString();
		if (value instanceof Map)
			return JSONPrettyPrinter.toJSONString((Map)value);
		if (value instanceof List)
			return JSONPrettyPrinter.listToJsonString((List)value);
		if ((value instanceof JSONAware))
			return ((JSONAware)value).toJSONString();

		return value.toString();
	}

	public static String escape(String str){
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < str.length(); i++){
			char ch = str.charAt(i);
			switch (ch){
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					if ((ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')){
						String ss=Integer.toHexString(ch);
						sb.append("\\u");
						for (int k = 0; k < 4 - ss.length(); k++){
							sb.append('0');
						}
						sb.append(ss.toUpperCase());
					}
					else {
						sb.append(ch);
					}
			}
		}
		return sb.toString();
	}
}
