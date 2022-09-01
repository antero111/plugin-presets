/*
 * Copyright (c) 2021, antero111 <https://github.com/antero111>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.pluginpresets;

import com.google.common.base.Strings;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.text.WordUtils;

public class PluginPresetsUtils
{
	/**
	 * Checks whether given string is valid for a preset filename.
	 */
	public static boolean stringContainsInvalidCharacters(final String string)
	{
		return !(Pattern.compile("(?i)^[ a-ö0-9-_.,;=()+!]+$").matcher(string).matches());
	}

	/**
	 * Split from uppercase letters and capitalize
	 */
	public static String splitAndCapitalize(final String string)
	{
		return WordUtils.capitalize(string.replaceAll("\\W", "").replaceAll("(.)([A-Z])", "$1 $2"));
	}

	/**
	 * Add commas to list of strings.
	 *
	 * @param plugins list of strings
	 * @return list of string append with commas
	 */
	public static String pluginListToString(List<String> plugins)
	{
		StringBuilder message = new StringBuilder(plugins.get(0));
		for (int i = 1; i < plugins.size(); i++)
		{
			if (i == plugins.size() - 1)
			{
				message.append(" and ").append(plugins.get(i));
				break;
			}

			message.append(", ");

			if (i % 4 == 0)
			{
				message.append("\n");
			}

			message.append(plugins.get(i));
		}

		if (plugins.size() == 1)
		{
			return message + " plugin.";
		}
		else
		{
			return message + " plugins.";
		}
	}

	public static String getClipboardText()
	{
		final String clipboardText;
		try
		{
			clipboardText = Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.getData(DataFlavor.stringFlavor)
				.toString();
		}
		catch (IOException | UnsupportedFlavorException ignore)
		{
			return null;
		}

		if (Strings.isNullOrEmpty(clipboardText))
		{
			return null;
		}

		return clipboardText;
	}

	public static String createNameWithSuffixIfNeeded(String name, List<PluginPreset> pluginPresets)
	{
		int duplicates = 0;
		for (PluginPreset preset : pluginPresets)
		{
			if (preset.getName().contains(name))
			{
				duplicates++;
			}
		}

		if (duplicates > 0)
		{
			boolean endWithSuffix = name.charAt((name.length() - 2)) == '(' && name.endsWith(")");
			if (endWithSuffix)
			{
				return String.format("%s (%d)", name.substring(0, name.length() - 3), duplicates);
			}
			else
			{
				return String.format("%s (%d)", name, duplicates);
			}
		}
		else
		{
			return name;
		}
	}
}
