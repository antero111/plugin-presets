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

public class PluginPresetsSharingManager
{
	private final PluginPresetsPluginPanel pluginPanel;

	public PluginPresetsSharingManager(final PluginPresetsPluginPanel pluginPanel)
	{
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
			final Gson gson = new Gson();
			newPreset = gson.fromJson(clipboardText, new TypeToken<PluginPreset>()
			{
			}.getType());
			// TODO: validate parsed json as plugin preset 
		}
		catch (JsonSyntaxException e)
		{
			pluginPanel.renderNotification("You do not have any valid presets in your clipboard.");
			return null;
		}

		return newPreset;
	}

	public void exportPresetToClipboard(final PluginPreset preset)
	{
		final Gson gson = new Gson();
		final String json = gson.toJson(preset);
		final StringSelection contents = new StringSelection(json);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, null);
	}
}
