//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.opdi.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhat.opdi.protocol.ProtocolException;


public class Strings {
	
	public final static char NO_SEPARATOR = '\0';

	private static String removeLeadingBlank(StringBuilder sb) {
		if (sb.length() > 0)
			if (sb.charAt(0) == ' ')
				sb.deleteCharAt(0);
		return sb.toString();
	}
	
	/** Splits the string at the specified separator character into parts.
	 * If the separator character appears twice in a row it is replaced by one character and appended to the previous part.
	 * Ideally splits strings that have been assembled using the join() method.
	 * This method guarantees that there is at least one part even if the string is empty or null.
	 * @param str
	 * @param c
	 * @return
	 */
	public static String[] split(String str, char c) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> list = new ArrayList<String>();
		if (str != null)
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i); 
				char nextCh = (char)(ch + 1);	// make sure the characters are not equal
				// preview of next character
				if (i < str.length() - 1) nextCh = str.charAt(i + 1);
				// two subsequent separator characters?
				if ((ch == c) && (ch == nextCh)) {
					sb.append(ch);
					// jump over the next one
					i++;
				} else if (ch == c) {
					// separator character
					list.add(removeLeadingBlank(sb));
					sb = new StringBuilder();
				} else
					// other character
					sb.append(ch);
			}
		list.add(removeLeadingBlank(sb));
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/** Joins the strings separated by the character c.
	 * If the character c appears in one of the strings it is replaced by twice c in order to
	 * allow splitting of the string using the split() method.
	 * The strings may be of any type. This method uses the toString() method. If an object is null
	 * it will be represented in the output by an empty part.
	 * @param c
	 * @param strings
	 * @return
	 */
	public static String join(char c, Object... strings) {
		StringBuilder sb = new StringBuilder();
		for (Object obj: strings) {
			String str = (obj == null ? "" : obj.toString());
			// do not send empty strings because it messes up the separator character escape sequence
			if (str.isEmpty()) str = " ";
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i); 
				// duplicate first character in case it's a blank
				// this preserves sending leading blanks
				if (i == 0 && ch == ' ') {
					sb.append(' ');
				}
				if (ch == c) {
					// append separator character twice
					sb.append(ch);
				}
				// all characters
				sb.append(ch);
			}
			// append separator
			sb.append(c);
		}
		return (sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1));	// remove last separator 
	}

	/** Joins the strings separated by the character c, skipping the first skip objects.
	 * If the character c appears in one of the strings it is replaced by twice c in order to
	 * allow splitting of the string using the split() method.
	 * If the separator character is '\0' (constant NO_SEPARATOR), no separator will be appended.
	 * The strings may be of any type. This method uses the toString() method. If an object is null
	 * it will be represented in the output by an empty part.
	 * @param c
	 * @param strings
	 * @return
	 */
	public static String join(int skip, char c, Object... strings) {
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for (Object obj: strings) {
			if (counter++ < skip)
				continue;
			String str = (obj == null ? "" : obj.toString());
			// do not send empty strings because it messes up the separator character escape sequence
			if (str.isEmpty()) str = " ";
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i); 
				if (ch == c) {
					// append separator character twice
					sb.append(ch);
				}
				// all characters
				sb.append(ch);
			}
			if (c != NO_SEPARATOR)
				// append separator
				sb.append(c);
		}
		return ((sb.length() == 0) || (c == NO_SEPARATOR) ? sb.toString() : sb.substring(0, sb.length() - 1));	// remove last separator 
	}
	
	/** Parses a parameter as integer and throws exceptions if error conditions are met.
	 * 
	 * @param s
	 * @param paramName
	 * @param min
	 * @param max
	 * @return
	 * @throws ProtocolException
	 */
	public static int parseInt(String s, String paramName, int min, int max) {
		try {
			int value = Integer.parseInt(s);
			if (value < min)
				throw new IllegalArgumentException("Parameter " + paramName + ": value too small: " + value);
			if (value > max)
				throw new IllegalArgumentException("Parameter " + paramName + ": value too large: " + value);
			return value;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Parameter " + paramName + ": number expected instead of '" + s + "'");
		}
	}

	/** Parses a parameter as long and throws exceptions if error conditions are met.
	 * 
	 * @param s
	 * @param paramName
	 * @param min
	 * @param max
	 * @return
	 * @throws ProtocolException
	 */
	public static long parseLong(String s, String paramName, long min, long max) {
		try {
			long value = Long.parseLong(s);
			if (value < min)
				throw new IllegalArgumentException("Parameter " + paramName + ": value too small: " + value);
			if (value > max)
				throw new IllegalArgumentException("Parameter " + paramName + ": value too large: " + value);
			return value;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Parameter " + paramName + ": number expected instead of '" + s + "'");
		}
	}

	/** Parses a parameter as double and throws exceptions if error conditions are met.
	 * 
	 * @param s
	 * @param paramName
	 * @param min
	 * @param max
	 * @return
	 * @throws ProtocolException
	 */
	public static double parseDouble(String s, String paramName, double min, double max) throws ProtocolException {
		try {
			double value = Double.parseDouble(s);
			if (value < min)
				throw new ProtocolException("Parameter " + paramName + ": value too small: " + value);
			if (value > max)
				throw new ProtocolException("Parameter " + paramName + ": value too large: " + value);
			return value;
		} catch (NumberFormatException e) {
			throw new ProtocolException("Parameter " + paramName + ": number expected instead of '" + s + "'");
		}
	}
	
	public static String escape(String sStrP)
	{
		// replace delimiter characters, tabs and newlines
		return sStrP.replace("\\", "\\\\").replace("=", "\\=").replace(";", "\\;").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
	}

	public static HashMap<String, String> getProperties(String sConfigP)
	{
		// loop over the string
		int pos = 0;		// position
		char ch = '\0';	// current character
		char ch1 = '\0';	// lookahead character
		String part = null;
		String key = null;
		HashMap<String, String> result = new HashMap<String, String>();
		
		while (pos < sConfigP.length())
		{
			ch = sConfigP.charAt(pos);
			if (pos < sConfigP.length() - 1)
			{
				ch1 = sConfigP.charAt(pos + 1);
			}
			else
			{
				ch1 = '\0';
			}
			// in part?
			if (part != null)
			{
				// end of key?
				if (ch == '=')
				{
					key = part.trim();
					part = null;
				} 
				// end of value?
				else if (ch == ';')
				{
					if (key != null)
					{
						// store key => value pair
						result.put(key, part);
					}
					else
					{
						// store key => empty string
						result.put(key, "");
					}
					key = null;
					part = null;
				}
				// escape char?
				else if (ch == '\\')
				{
					// evaluate next character
					if (ch1 == '\\')
					{
						part += '\\';
					} 
					else
					if (ch1 == '=')
					{
						part += '=';
					} 
					else
					if (ch1 == ';')
					{
						part += ';';
					} 
					else
					if (ch1 == 't')
					{
						part += '\t';
					} 
					else
					if (ch1 == 'n')
					{
						part += '\n';
					} 
					else
					if (ch1 == 'r')
					{
						part += '\r';
					} 
					else
					// ! EOS?
					if (ch1 != 0)
					{
						part += ch1;
					}
					// skip next character
					pos++;
				}
				// normal character
				else
					part += ch;
			}
			else
			// part starting
			{
				part = "";
				// end of key?
				if (ch == '=')
				{
					// syntax error, ignore
				} 
				// end of value?
				else if (ch == ';')
				{
					// syntax error, ignore
				}
				// escape char?
				else if (ch == '\\')
				{
					// evaluate next character
					if (ch1 == '\\')
					{
						part += '\\';
					} 
					else
					if (ch1 == '=')
					{
						part += '=';
					} 
					else
					if (ch1 == ';')
					{
						part += ';';
					} 
					else
					if (ch1 == 't')
					{
						part += '\t';
					} 
					else
					if (ch1 == 'n')
					{
						part += '\n';
					} 
					else
					if (ch1 == 'r')
					{
						part += '\r';
					} 
					else
					// ! EOS?
					if (ch1 != 0)
					{
						part += ch1;
					}
					// skip next character
					pos++;
				}
				// normal character
				else
					part += ch;			
			}
			pos++;
		}
		
		// last property unterminated?
		if (key != null) 
		{
			result.put(key, (part == null ? "" : part));
		}
		// key not followed by =
		else if (part != null)
		{
			result.put(key, "");
		}
		
		return result;
	}

}
