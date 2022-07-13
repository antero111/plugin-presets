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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginPresetsStorage
{
	private static final File PRESETS_DIR = PluginPresetsPlugin.PRESETS_DIR;

	private List<String> failedFileNames = new ArrayList<>();

	private Gson gson = new Gson();

	public void savePresets(final List<PluginPreset> pluginPresets)
	{
		clearPresetFolder();
		pluginPresets.forEach(this::storePluginPresetToJsonFile);
	}

	private void clearPresetFolder()
	{
		for (File file : Objects.requireNonNull(PRESETS_DIR.listFiles()))
		{
			// Dont delete invalid files,
			// those could be e.g v1 style presets or some syntax failed presets
			if (!failedFileNames.contains(file.getName()))
			{
				final boolean fileWasDeleted = file.delete();
				if (!fileWasDeleted)
				{
					log.warn(String.format("Could not delete %s", file.getName()));
				}
			}
		}
	}

	@SneakyThrows
	private void storePluginPresetToJsonFile(final PluginPreset pluginPreset)
	{
		File presetJsonFile = getPresetJsonFileFrom(pluginPreset);

		if (presetJsonFile.exists())
		{
			presetJsonFile = giveJsonFileCustomSuffixNumber(pluginPreset, presetJsonFile);
		}

		writePresetDataToJsonFile(pluginPreset, presetJsonFile);
	}

	private File getPresetJsonFileFrom(final PluginPreset pluginPreset)
	{
		return new File(PRESETS_DIR, String.format("%s.json", pluginPreset.getName()));
	}

	private File giveJsonFileCustomSuffixNumber(final PluginPreset pluginPreset, File presetJsonFile)
	{
		int fileNumber = 1;
		while (presetJsonFile.exists())
		{
			presetJsonFile = createNewPresetFileWithCustomSuffix(pluginPreset, fileNumber);
			fileNumber++;
		}
		return presetJsonFile;
	}

	private static File createNewPresetFileWithCustomSuffix(final PluginPreset pluginPreset, final int fileNumber)
	{
		return new File(PRESETS_DIR, String.format("%s (%d).json", pluginPreset.getName(), fileNumber));
	}

	private void writePresetDataToJsonFile(final PluginPreset pluginPreset, final File presetJsonFile) throws IOException
	{
		Writer writer = new FileWriter(presetJsonFile);
		gson.toJson(pluginPreset, writer);
		writer.close();
	}

	public List<PluginPreset> loadPresets() throws IOException
	{
		failedFileNames.clear();

		ArrayList<Long> loadedIds = new ArrayList<>();
		List<PluginPreset> pluginPresetsFromFolder = new ArrayList<>();

		for (File file : Objects.requireNonNull(PRESETS_DIR.listFiles()))
		{
			if (file.isFile())
			{
				PluginPreset pluginPreset = parsePluginPresetFrom(file);

				if (pluginPreset != null)
				{
					long id = pluginPreset.getId();
					if (!(loadedIds.contains(id)))
					{
						pluginPresetsFromFolder.add(pluginPreset);
						loadedIds.add(id);
					}
				}
				else
				{
					failedFileNames.add(file.getName());
				}
			}
		}

		return pluginPresetsFromFolder;
	}

	private PluginPreset parsePluginPresetFrom(final File file) throws IOException
	{
		PluginPreset pluginPreset;
		
		try (Reader reader = new FileReader(file))
		{
			pluginPreset = gson.fromJson(reader, new TypeToken<PluginPreset>()
			{
			}.getType());
		}
		catch (JsonSyntaxException e)
		{
			log.warn(String.format("Failed to load preset from %s, %s", file.getAbsolutePath(), e.getMessage()));
			return null;
		}

		if (pluginPreset.getName() == null || pluginPreset.getPluginConfigs() == null)
		{
			log.warn(String.format("Plugin Preset data is malformed in file and could not be loaded %s, %s", file.getAbsolutePath(), pluginPreset));
			return null;
		}

		return pluginPreset;
	}

	public static void createPresetFolder()
	{
		final boolean presetFolderWasCreated = PRESETS_DIR.mkdirs();

		if (presetFolderWasCreated)
		{
			log.info(String.format("Preset folder created at %s", PRESETS_DIR.getAbsolutePath()));
		}
	}
}
