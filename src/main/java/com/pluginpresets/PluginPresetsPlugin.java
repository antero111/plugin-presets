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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

@Slf4j
@PluginDescriptor(
	name = "Plugin Presets",
	description = "Create presets of your plugin configurations.",
	tags = {"preset", "setups", "plugins"}
)
public class PluginPresetsPlugin extends Plugin
{
	public static final File PRESETS_DIR = new File(RUNELITE_DIR, "presets");
	private static final List<String> IGNORED_PLUGINS = Stream.of("Plugin Presets", "Configuration", "Xtea", "Twitch", "Notes", "Discord").collect(Collectors.toList());
	private static final String PLUGIN_NAME = "Plugin Presets";
	private static final String ICON_FILE = "panel_icon.png";
	public final String HELP_LINK = "https://github.com/antero111/plugin-presets#using-plugin-presets";
	public final String DEFAULT_PRESET_NAME = "Preset";

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

	private PluginPresetsSharingManager sharingManager;

	private boolean configChangedFromLoadPreset = false;

	@Override
	protected void startUp()
	{
		createPresetFolder();

		loadPresets();

		pluginPanel = new PluginPresetsPluginPanel(this);
		pluginPanel.rebuild();

		sharingManager = new PluginPresetsSharingManager(pluginPanel);

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
	protected void shutDown()
	{
		pluginPresets.clear();
		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		sharingManager = null;
		preset = null;
		navigationButton = null;
	}

	@Subscribe
	public void onExternalPluginsChanged(ExternalPluginsChanged externalPluginsChanged)
	{
		SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!(configChangedFromLoadPreset))
		{
			// Check if manually created preset matches existing one
			PluginPreset matchingPreset = getPresetThatMatchesCurrentConfigurations();
			if (matchingPreset != null)
			{
				SwingUtilities.invokeLater(() -> setAsSelected(matchingPreset, true));
			}
			else
			{
				// No existing preset matches current configurations
				warnFromUnsavedPluginConfigurations();
			}
		}
	}

	private PluginPreset getPresetThatMatchesCurrentConfigurations()
	{
		HashMap<String, Boolean> enabledPlugins = getEnabledPlugins();

		for (PluginPreset preset : pluginPresets)
		{
			if (preset.getEnabledPlugins().equals(enabledPlugins))
			{
				return preset;
			}
		}

		return null;
	}

	private void warnFromUnsavedPluginConfigurations()
	{
		PluginPreset selectedPreset = getSelectedPreset();
		if (selectedPreset != null)
		{
			SwingUtilities.invokeLater(() -> displayUnsavedPluginConfigurationsWarning(selectedPreset));
		}
	}

	private PluginPreset getSelectedPreset()
	{
		for (PluginPreset preset : pluginPresets)
		{
			if (preset.getSelected() != null && preset.getSelected())
			{
				return preset;
			}
		}
		return null;
	}

	private void displayUnsavedPluginConfigurationsWarning(PluginPreset selectedPreset)
	{
		setAsSelected(selectedPreset, null);
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

			// Check if plugin has configurable settings to be saved
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

		setAsSelected(preset, true);

		refreshPresets();
		pluginPanel.rebuild();
	}

	public void deletePreset(final PluginPreset preset)
	{
		pluginPresets.remove(preset);
		savePresets();
		pluginPanel.rebuild();
	}

	public void updatePreset(final PluginPreset preset)
	{
		preset.setEnabledPlugins(getEnabledPlugins());
		preset.setPluginSettings(getPluginSettings());
		savePresets();
		setAsSelected(preset, true);
		pluginPanel.rebuild();
	}

	@SneakyThrows
	public void loadPreset(final PluginPreset preset)
	{
		configChangedFromLoadPreset = true;

		// Load plugin settings
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

		// Start/stop plugins
		HashMap<String, Boolean> enabledPlugins = preset.getEnabledPlugins();
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (IGNORED_PLUGINS.contains(plugin.getName()))
			{
				continue;
			}

			// External Plugin Hub plugins that are not yet saved to the preset one is trying to load raises a null exception
			// External plugins will stay as "ignored" (they wont go on/off when presets are loaded) until saved to a plugin preset
			try
			{
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
			catch (NullPointerException ignore)
			{
			}
		}

		configChangedFromLoadPreset = false;
	}

	@SneakyThrows
	public void savePresets()
	{
		PluginPresetsStorage.savePresets(pluginPresets);
	}

	@SneakyThrows
	public void loadPresets()
	{
		pluginPresets.addAll(PluginPresetsStorage.loadPresets());
	}

	public void refreshPresets()
	{
		pluginPresets.clear();
		loadPresets();
		savePresets();
	}

	public void importPresetFromClipboard()
	{
		PluginPreset newPreset = sharingManager.importPresetFromClipboard();
		if (newPreset == null)
		{
			return;
		}
		newPreset.setId(Instant.now().toEpochMilli());
		newPreset.setSelected(false);

		pluginPresets.add(newPreset);
		savePresets();
		refreshPresets();
		pluginPanel.rebuild();
	}

	public void exportPresetToClipboard(final PluginPreset preset)
	{
		sharingManager.exportPresetToClipboard(preset);
	}

	public void setAsSelected(PluginPreset selectedPreset, Boolean select)
	{
		pluginPresets.forEach(preset -> preset.setSelected(false));
		if (selectedPreset != null)
		{
			selectedPreset.setSelected(select);
		}
		savePresets();
		pluginPanel.rebuild();
	}

	public List<String> getUnsavedExternalPlugins(PluginPreset preset)
	{
		List<String> newPlugins = new ArrayList<>();
		List<String> plugins = pluginManager.getPlugins().stream().map(Plugin::getName)
			.collect(Collectors.toList());
		List<String> pluginsInPreset = new ArrayList<>(preset.getEnabledPlugins().keySet());

		plugins.forEach(plugin -> {
			if (!(IGNORED_PLUGINS.contains(plugin)))
			{
				if (!(pluginsInPreset.contains(plugin)))
				{
					newPlugins.add(plugin);
				}
			}
		});

		return newPlugins;
	}

	public List<String> getMissingExternalPlugins(PluginPreset preset)
	{

		List<String> missingPlugins = new ArrayList<>();
		List<String> plugins = pluginManager.getPlugins().stream().map(Plugin::getName)
			.collect(Collectors.toList());
		List<String> pluginsInPreset = new ArrayList<>(preset.getEnabledPlugins().keySet());

		pluginsInPreset.forEach(plugin -> {
			if (!(IGNORED_PLUGINS.contains(plugin)))
			{
				if (!(plugins.contains(plugin)))
				{
					missingPlugins.add(plugin);
				}
			}
		});

		return missingPlugins;
	}

	private void createPresetFolder()
	{
		final boolean presetFolderWasCreated = PRESETS_DIR.mkdirs();

		if (!presetFolderWasCreated)
		{
			log.warn(String.format("Could not create %s", PRESETS_DIR.getAbsolutePath()));
		}
	}

	public boolean stringContainsInvalidCharacters(String string)
	{
		return !(Pattern.compile("^[ A-Za-z0-9]+$").matcher(string).matches());
	}
}
