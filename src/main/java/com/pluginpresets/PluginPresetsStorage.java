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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class PluginPresetsStorage
{
	private static final File PRESETS_DIR = PluginPresetsPlugin.getPRESETS_DIR();

	static void savePresets(List<PluginPreset> presets) throws IOException
	{
		for (File file : PRESETS_DIR.listFiles())
		{
			file.delete();
		}

		for (PluginPreset pluginPreset : presets)
		{
			File file = new File(PRESETS_DIR, pluginPreset.getName() + ".json");
			Gson gson = new Gson();
			Writer writer = new FileWriter(file);
			gson.toJson(pluginPreset, writer);
			writer.flush();
			writer.close();
		}
	}

	static List<PluginPreset> loadPresets() throws IOException
	{
		List<PluginPreset> pluginPresetsFromFolder = new ArrayList<>();
		for (File file : PRESETS_DIR.listFiles())
		{
			Gson gson = new Gson();
			PluginPreset presets = gson.fromJson(new FileReader(file), new TypeToken<PluginPreset>()
			{
			}.getType());
			pluginPresetsFromFolder.add(presets);
		}
		return pluginPresetsFromFolder;
	}
}
