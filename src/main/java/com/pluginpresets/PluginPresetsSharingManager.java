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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.pluginpresets.ui.PluginPresetsPluginPanel;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.time.Instant;

public class PluginPresetsSharingManager
{
	private final PluginPresetsPlugin plugin;
	private final PluginPresetsPluginPanel pluginPanel;
	
	private Gson gson = new Gson();

	public PluginPresetsSharingManager(PluginPresetsPlugin plugin, final PluginPresetsPluginPanel pluginPanel)
	{
		this.plugin = plugin;
		this.pluginPanel = pluginPanel;
	}

	private String getClipboardText()
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
			pluginPanel.renderNotification("Unable to read system clipboard.");
			return null;
		}

		if (Strings.isNullOrEmpty(clipboardText))
		{
			pluginPanel.renderNotification("Your clipboard is empty.");
			return null;
		}

		return clipboardText;
	}

	public PluginPreset importPresetFromClipboard()
	{
		PluginPreset newPreset;

		String clipboardText = getClipboardText();
		if (clipboardText == null)
		{
			return null;
		}

		try
		{
			newPreset = gson.fromJson(clipboardText, new TypeToken<PluginPreset>()
			{
			}.getType());
		}
		catch (JsonSyntaxException e)
		{
			pluginPanel.renderNotification("You do not have any valid presets in your clipboard.");
			return null;
		}

		if (newPreset == null || newPreset.getName() == null || newPreset.getPluginConfigs() == null)
		{
			pluginPanel.renderNotification("You do not have any valid presets in your clipboard.");
			return null;
		}

		newPreset.setId(Instant.now().toEpochMilli());
		newPreset.setName(createNameWithSuffixIfNeeded(newPreset.getName()));

		return newPreset;
	}

	private String createNameWithSuffixIfNeeded(String name)
	{
		int duplicates = 0;
		for (PluginPreset preset : plugin.getPluginPresets()) {
			if (preset.getName().contains(name))
			{
				duplicates++;
			}
		}

		if (duplicates > 0)
		{
			boolean endWithSuffix = name.charAt((name.length() - 2)) == '(' && name.endsWith(")"); // cba with regex
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

	public void exportPresetToClipboard(final PluginPreset preset)
	{
		final String json = gson.toJson(preset);
		final StringSelection contents = new StringSelection(json);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, null);
	}
}
