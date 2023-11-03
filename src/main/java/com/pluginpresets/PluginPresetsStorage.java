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
import com.google.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.SwingUtilities;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginPresetsStorage
{
	private static final File PRESETS_DIR = PluginPresetsPlugin.PRESETS_DIR;

	private final List<String> failedFileNames = new ArrayList<>();
	private final PluginPresetsPlugin plugin;

	@Inject
	private Gson gson;
	
	private Thread thread;
	private WatchService watcher;

	/**
	 * Informs that preset folder edits were made from this client, and they should be refreshed first.
	 */
	private boolean localClientChange = false;
	private long lastRefreshTime;

	@Inject
	public PluginPresetsStorage(PluginPresetsPlugin plugin)
	{
		this.plugin = plugin;
	}

	private static File createNewPresetFileWithCustomSuffix(final PluginPreset pluginPreset, final int fileNumber)
	{
		return new File(PRESETS_DIR, String.format("%s (%d).json", pluginPreset.getName(), fileNumber));
	}

	public static void createPresetFolder()
	{
		final boolean presetFolderWasCreated = PRESETS_DIR.mkdirs();

		if (presetFolderWasCreated)
		{
			log.info(String.format("Preset folder created at %s", PRESETS_DIR.getAbsolutePath()));
		}
	}

	public void deletePresetFolderIfEmpty()
	{
		if (PRESETS_DIR.exists() && Objects.requireNonNull(PRESETS_DIR.listFiles()).length > 0)
		{
			return;
		}

		deletePresetFolder();
	}

	private void deletePresetFolder()
	{
		boolean folderDeleted = PRESETS_DIR.delete();

		if (!folderDeleted)
		{
			log.warn(String.format("Could not delete %s", PRESETS_DIR.getName()));
		}
	}

	public void savePresets(final List<PluginPreset> pluginPresets)
	{
		localClientChange = true;
		clearPresetFolder();
		pluginPresets.forEach(this::storePluginPresetToJsonFile);
	}

	private void clearPresetFolder()
	{
		for (File file : Objects.requireNonNull(PRESETS_DIR.listFiles()))
		{
			// Don't delete invalid files,
			// those could be e.g. v1 style presets or some syntax failed presets
			if (!failedFileNames.contains(file.getName()))
			{
				deleteFile(file);
			}
		}
	}

	private void deleteFile(File file)
	{
		final boolean fileWasDeleted = file.delete();
		if (!fileWasDeleted)
		{
			log.warn(String.format("Could not delete %s", file.getName()));
		}
	}

	@SneakyThrows
	private void storePluginPresetToJsonFile(final PluginPreset pluginPreset)
	{
		// Only store local presets
		if (!pluginPreset.getLocal())
		{
			return;
		}

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

	private void writePresetDataToJsonFile(final PluginPreset pluginPreset, final File presetJsonFile)
	{
		pluginPreset.setLocal(null); // Don't store status value to file

		try (Writer writer = new FileWriter(presetJsonFile))
		{
			gson.toJson(pluginPreset, writer);
		}
		catch (Exception e)
		{
			// Ignore
		}
		finally
		{
			pluginPreset.setLocal(true);
		}

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
						pluginPreset.setLocal(true);
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
		PluginPreset newPreset;

		try (Reader reader = new FileReader(file))
		{
			newPreset = gson.fromJson(reader, new TypeToken<PluginPreset>()
			{
			}.getType());
		}
		catch (JsonSyntaxException | FileNotFoundException e)
		{
			log.warn(String.format("Failed to load preset from %s, %s", file.getAbsolutePath(), e.getMessage()));
			return null;
		}

		if (isMalformedPluginPreset(newPreset))
		{
			log.warn(String.format("Plugin Preset data is malformed in file and could not be loaded %s, %s", file.getAbsolutePath(), newPreset));
			return null;
		}

		return newPreset;
	}

	private boolean isMalformedPluginPreset(PluginPreset newPreset)
	{
		return newPreset.getName() == null || newPreset.getPluginConfigs() == null;
	}

	public PluginPreset parsePluginPresetFrom(String string)
	{
		PluginPreset newPreset;

		try
		{
			newPreset = gson.fromJson(string, new TypeToken<PluginPreset>()
			{
			}.getType());
		}
		catch (JsonSyntaxException e)
		{
			return null;
		}

		if (newPreset == null || newPreset.getName() == null || newPreset.getPluginConfigs() == null)
		{
			return null;
		}

		return newPreset;
	}

	/**
	 * Starts thread that runs method that watches preset folder for file changes that do preset refresh.
	 */
	public void watchFolderChanges()
	{
		thread = new Thread(this::watchFolder);
		thread.setName("PresetFolderWatcher");
		thread.start();
	}

	public void stopWatcher()
	{
		thread.interrupt();
		try
		{
			watcher.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		watcher = null;
		thread = null;
	}

	public void watchFolder()
	{
		Path presetDir = PRESETS_DIR.toPath();

		try
		{
			watcher = presetDir.getFileSystem().newWatchService();
			presetDir.register(
				watcher,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY
			);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		while (watcher != null)
		{
			WatchKey wk;
			if (!thread.isAlive())
			{
				return;
			}

			try
			{
				wk = watcher.take();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}

			if (!wk.pollEvents().isEmpty())
			{
				// Run refreshPresets only once
				boolean validMillisDiff = (System.currentTimeMillis() - lastRefreshTime) > 100;
				if (validMillisDiff)
				{
					// Offset other clients so that file edits don't collapse
					if (!localClientChange)
					{
						try
						{
							Thread.sleep(300);
						}
						catch (InterruptedException e)
						{
							Thread.currentThread().interrupt();
							return;
						}
					}

					lastRefreshTime = System.currentTimeMillis();
					SwingUtilities.invokeLater(plugin::refreshPresets); // Refresh
					localClientChange = false;
				}
			}
			boolean valid = wk.reset();
			if (!valid)
			{
				break;
			}
		}
	}
}