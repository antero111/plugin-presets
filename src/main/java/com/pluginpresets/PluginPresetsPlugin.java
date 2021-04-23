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

import com.google.common.collect.ImmutableMap;
import com.pluginpresets.ui.PluginPresetsPluginPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.plugins.PluginManager;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

@PluginDescriptor(
	name = "Plugin Presets",
	description = "Create presets of your plugin configurations.",
	tags = {"preset", "setups", "plugins"}
)
public class PluginPresetsPlugin extends Plugin
{
	private static final String PLUGIN_NAME = "Plugin Presets";
	private static final String ICON_FILE = "panel_icon.png";
	private static final List<String> IGNORED_PLUGINS = Stream.of("Twitch", "Login Screen", "Notes", "Discord").collect(Collectors.toList());

	@Getter
	private static final File PRESETS_DIR = new File(RUNELITE_DIR, "presets");

	@Getter
	private static final String DEFAULT_PRESET_NAME = "Preset";

	@Getter
	private final List<PluginPreset> pluginPresets = new ArrayList<>();

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	private NavigationButton navigationButton;

	@Getter(AccessLevel.PACKAGE)
	private PluginPreset preset;

	private PluginPresetsPluginPanel pluginPanel;

	@Override
	protected void startUp() throws Exception
	{
		PRESETS_DIR.mkdirs();

		loadPresets();

		pluginPanel = new PluginPresetsPluginPanel(this);
		pluginPanel.rebuild();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.priority(8)
			.icon(icon)
			.panel(pluginPanel)
			.popup(ImmutableMap
				.<String, Runnable>builder()
				.put("Open preset folder...", () ->
					LinkBrowser.open(PRESETS_DIR.toString()))
				.build())
			.build();
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginPresets.clear();
		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		preset = null;
		navigationButton = null;
	}

	private HashMap<String, Boolean> getEnabledPlugins()
	{
		HashMap<String, Boolean> enabledPlugins = new HashMap<>();

		pluginManager.getPlugins().forEach(plugin -> {
			if (!(IGNORED_PLUGINS.contains(plugin.getName())))
			{
				enabledPlugins.put(plugin.getName(), pluginManager.isPluginEnabled(plugin));
			}
		});

		return enabledPlugins;
	}

	private HashMap<String, HashMap<String, String>> getPluginSettings()
	{
		HashMap<String, HashMap<String, String>> pluginSettings = new HashMap<>();

		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (IGNORED_PLUGINS.contains(plugin.getName()))
			{
				continue;
			}

			HashMap<String, String> pluginSettingKeyValue = new HashMap<>();

			// Check if plugin has configurable settings to be saved.
			ConfigDescriptor pluginConfigProxy;
			try
			{
				pluginConfigProxy = configManager.getConfigDescriptor(pluginManager.getPluginConfigProxy(plugin));
			}
			catch (NullPointerException ignore)
			{
				continue;
			}

			String groupName = pluginConfigProxy.getGroup().value();
			pluginConfigProxy.getItems().forEach(configItemDescriptor -> {
				String key = configItemDescriptor.getItem().keyName();
				pluginSettingKeyValue.put(key, configManager.getConfiguration(groupName, key));
			});

			pluginSettings.put(groupName, pluginSettingKeyValue);
		}

		return pluginSettings;
	}

	public void createPreset(String presetName)
	{
		if (presetName.equals(""))
		{
			presetName = DEFAULT_PRESET_NAME + " " + (pluginPresets.size() + 1);
		}

		preset = new PluginPreset(
			Instant.now().toEpochMilli(),
			presetName,
			false,
			getEnabledPlugins(),
			getPluginSettings()
		);
		pluginPresets.add(preset);

		setAsSelected(preset);
	}

	public void deletePreset(final PluginPreset preset)
	{
		pluginPresets.remove(preset);
		savePresets();
		pluginPanel.rebuild();
	}

	public void overwritePreset(final PluginPreset preset)
	{
		preset.setEnabledPlugins(getEnabledPlugins());
		preset.setPluginSettings(getPluginSettings());
		savePresets();
		pluginPanel.rebuild();
	}

	@SneakyThrows
	public void loadPreset(final PluginPreset preset)
	{
		// Load plugin settings.
		for (HashMap.Entry<String, HashMap<String, String>> groupNames : preset.getPluginSettings().entrySet())
		{
			for (HashMap.Entry<String, String> keys : preset.getPluginSettings().get(groupNames.getKey()).entrySet())
			{
				if (keys.getValue() == null)
				{
					continue;
				}

				configManager.setConfiguration(groupNames.getKey(), keys.getKey(), keys.getValue());
			}
		}

		// Start/stop plugins.
		HashMap<String, Boolean> enabledPlugins = preset.getEnabledPlugins();
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (IGNORED_PLUGINS.contains(plugin.getName()))
			{
				continue;
			}

			pluginManager.setPluginEnabled(plugin, enabledPlugins.get(plugin.getName()));

			if (enabledPlugins.get(plugin.getName()))
			{
				pluginManager.startPlugin(plugin);
			}
			else
			{
				pluginManager.stopPlugin(plugin);
			}
		}
	}

	public void savePresets()
	{
		try
		{
			PluginPresetsStorage.savePresets(pluginPresets);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void loadPresets()
	{
		try
		{
			pluginPresets.addAll(PluginPresetsStorage.loadPresets());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void refreshPresets()
	{
		pluginPresets.clear();
		loadPresets();
		savePresets();
		pluginPanel.rebuild();
	}

	public void setAsSelected(PluginPreset selectedPreset)
	{
		for (PluginPreset preset : pluginPresets)
		{
			preset.setSelected(false);
		}
		selectedPreset.setSelected(true);
		savePresets();
		pluginPanel.rebuild();
	}

	public boolean stringContainsInvalidCharacters(String string)
	{
		return !(Pattern.compile("^[ A-Za-z0-9]+$").matcher(string).matches());
	}
}
