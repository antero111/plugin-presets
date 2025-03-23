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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static java.nio.file.StandardOpenOption.*;

@Slf4j
public class PluginPresetsStorage
{
	private static final File PRESETS_DIR = PluginPresetsPlugin.PRESETS_DIR;
	private static final File LOCK_FILE = new File(PRESETS_DIR, "lock");

	private final List<String> failedFileNames = new ArrayList<>();
	private final PluginPresetsPlugin plugin;

	@Inject
	private Gson gson;
	
	private Thread thread;
	private WatchService watcher;

	@Inject
	public PluginPresetsStorage(PluginPresetsPlugin plugin)
	{
		this.plugin = plugin;
	}

	private FileLock lock() {
		try {
			// wait until the lock file is free
			while (LOCK_FILE.exists()) {
				// writing the plugin preset files should only take a few ms
				// if the lock file is more than 5sec old then we assume it's an artifact from a previous run
				// this can happen in the event runelite crashes or is killed vai external process
				if(Instant.now().toEpochMilli() - LOCK_FILE.lastModified() > 5000) {
					LOCK_FILE.delete();
				} else {
					TimeUnit.MILLISECONDS.sleep(50);
				}
			}

			FileChannel channel = FileChannel.open(LOCK_FILE.toPath(), CREATE_NEW, SYNC, WRITE);
			return channel.lock();
		} catch (IOException | InterruptedException e) {
			log.warn("Failed to lock file");
		}
		return null;
	}

	private void unlock(FileLock lock) {
		if(lock == null) {
			log.warn("lock is null"); // probably caused by ending runelite via task manager
			LOCK_FILE.delete();
			return;
		}

		try {
			lock.release();
			LOCK_FILE.delete();
		} catch (IOException e) {
			log.warn("Failed to release lock file");
		}
	}

	private boolean isLockFile(File file) {
		try {
			return LOCK_FILE.exists() && Files.isSameFile(file.toPath(), LOCK_FILE.toPath());
		} catch (IOException e) {
			log.warn(String.format("Failed compare file to lock file, %s", e.toString()));
		}
		return false;
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
		synchronized (LOCK_FILE) {
			FileLock lock = lock();
			clearPresetFolder();
			pluginPresets.forEach(this::storePluginPresetToJsonFile);
			unlock(lock);
		}
	}

	private void clearPresetFolder()
	{
		for (File file : Objects.requireNonNull(PRESETS_DIR.listFiles()))
		{
			// Don't delete invalid files,
			// those could be e.g. v1 style presets or some syntax failed presets
			if (!failedFileNames.contains(file.getName()) && !isLockFile(file))
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
		ArrayList<Long> loadedIds = new ArrayList<>();
		List<PluginPreset> pluginPresetsFromFolder = new ArrayList<>();
		synchronized (LOCK_FILE) {
			FileLock lock = lock();

			failedFileNames.clear();
			for (File file : Objects.requireNonNull(PRESETS_DIR.listFiles()))
			{
				if (file.isFile() && !isLockFile(file))
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

			unlock(lock);
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

			// test to see if any files other than the lock file were modified
			boolean change = false;
			for(WatchEvent<?> event : wk.pollEvents()) {
				WatchEvent<Path> path = (WatchEvent<Path>) event;
				change = !path.context().toString().equals(LOCK_FILE.getName());
				if(change){
					break;
				}
			}

			// if any plugin preset files were modified then run the update
			// we lock here to ensure that all updates happen before we reload
			if(change) {
				synchronized (LOCK_FILE) {
					FileLock lock = lock();
					wk.pollEvents(); // clear all events
					SwingUtilities.invokeLater(plugin::refreshPresets); // Refresh
					unlock(lock);
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