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
import com.google.inject.Provides;
import com.pluginpresets.ui.PluginPresetsPluginPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
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
	public static final String HELP_LINK = "https://github.com/antero111/plugin-presets#using-plugin-presets";
	public static final String DEFAULT_PRESET_NAME = "Preset";
	private static final List<String> DEFAULT_IGNORED_PLUGINS = Stream.of("Plugin Presets", "Configuration", "Xtea").collect(Collectors.toList());
	private static final List<String> DEFAULT_IGNORED_PLUGIN_KEYS = Stream.of("channel", "oauth", "username", "notesData").collect(Collectors.toList());
	private static final String PLUGIN_NAME = "Plugin Presets";
	private static final String PLUGIN_CONFIG_NAME = "pluginpresets";
	private static final String ICON_FILE = "panel_icon.png";

	@Getter
	private final List<PluginPreset> pluginPresets = new ArrayList<>();

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private PluginPresetsConfig pluginConfig;

	@Inject
	private ConfigManager configManager;

	@Getter
	@Inject
	private ChangeInspectorManager changeInspectorManager;

	private NavigationButton navigationButton;

	private PluginPresetsPluginPanel pluginPanel;

	private PluginPresetsSharingManager sharingManager;

	// Prevents ConfigChanged event from running when programmatically turning plugins on/off
	private boolean configChangedFromLoadPreset = false;

	@Provides
	PluginPresetsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PluginPresetsConfig.class);
	}

	@Override
	protected void startUp()
	{
		createPresetFolder();

		loadPresets();

		pluginPanel = new PluginPresetsPluginPanel(this);
		rebuildPluginUi();

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
		navigationButton = null;
	}

	@Subscribe
	public void onExternalPluginsChanged(ExternalPluginsChanged externalPluginsChanged)
	{
		SwingUtilities.invokeLater(this::rebuildPluginUi);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (validConfigChange(configChanged))
		{
			handleValidConfigChange();
		}
	}

	private boolean validConfigChange(ConfigChanged configChanged)
	{
		return !configChangedFromLoadPreset
			&& !configChanged.getGroup().equals("pluginpresets")
			&& !configChanged.getKey().equals("pluginpresetsplugin");
	}

	private void handleValidConfigChange()
	{
		PluginPreset matchingPreset = getPresetMatchingCurrentConfigurations();
		if (presetIsValid(matchingPreset))
		{
			setMatchingPresetAsSelected(matchingPreset);
		}
		else
		{
			warnFromUnsavedPluginConfigurations();
		}
	}

	private Boolean presetIsValid(final PluginPreset preset)
	{
		return preset != null;
	}

	private PluginPreset getPresetMatchingCurrentConfigurations()
	{
		for (PluginPreset preset : pluginPresets)
		{
			if (presetMatchesCurrentConfigurations(preset))
			{
				return preset;
			}
		}
		return null;
	}

	public Map<String, ArrayList<String[]>> getChanges(PluginPreset preset)
	{
		List<String> userIgnoredPlugins = getUserIgnoredPlugins();
		Set<Entry<String, Boolean>> enabledPlugins = getEnabledPlugins().entrySet();
		HashMap<String, Boolean> presetEnabledPlugins = preset.getEnabledPlugins();

		HashMap<String, ArrayList<String[]>> changes = new HashMap<>();

		// Compare current plugin on/off configurations to preset 
		for (Entry<String, Boolean> currentPluginSetting : enabledPlugins)
		{
			String pluginName = currentPluginSetting.getKey();
			Boolean presetPluginValue = presetEnabledPlugins.get(pluginName);
			Boolean pluginValue = currentPluginSetting.getValue();

			if (presetPluginValue != null
				&& !presetPluginValue.equals(pluginValue)
				&& !userIgnoredPlugins.contains(pluginName))
			{
				ArrayList<String[]> pluginChange = new ArrayList<>();
				String presetValueString = presetPluginValue.toString() == "true" ? "On" : "Off";
				String pluginValueString = pluginValue.toString() == "true" ? "On" : "Off";
				pluginChange.add(new String[]{"Plugin", presetValueString, pluginValueString});
				changes.put(pluginName, pluginChange);
			}
		}

		List<String> userIgnoredPluginSettings = getUserIgnoredPluginSettings();
		List<String> userIgnoredPluginConfigNames = getUserIgnoredPluginConfigNames();

		// Compare current plugin configurations to preset
		HashMap<String, HashMap<String, String>> currentPluginSettings = getPluginSettings();
		for (Entry<String, HashMap<String, String>> pluginSettingsFromPreset : preset.getPluginSettings().entrySet())
		{
			HashMap<String, String> currentSettingsForPlugin = currentPluginSettings.get(pluginSettingsFromPreset.getKey());
			for (Entry<String, String> settingKeyValuePair : pluginSettingsFromPreset.getValue().entrySet())
			{
				String settingKey = settingKeyValuePair.getKey();
				String presetSettingValue = settingKeyValuePair.getValue();
				String currentSettingValue = currentSettingsForPlugin.get(settingKey);
				String pluginConfigName = pluginSettingsFromPreset.getKey();

				if (presetSettingValue != null
					&& currentSettingValue != null
					&& !presetSettingValue.equals(currentSettingValue)
					&& !userIgnoredPluginSettings.contains(settingKey)
					&& !userIgnoredPluginConfigNames.contains(pluginConfigName))
				{
					String pluginName = pluginConfigNameToPluginName(pluginConfigName);
					if (pluginName == null)
					{
						pluginName = "RuneLite";
					}
					
					if (changes.get(pluginName) == null)
					{
						ArrayList<String[]> settingChange = new ArrayList<>();
						settingChange.add(new String[]{settingKey, presetSettingValue, currentSettingValue});
						changes.put(pluginName, settingChange);
					}
					else
					{
						ArrayList<String[]> settingChange = changes.get(pluginName);
						settingChange.add(new String[]{settingKey, presetSettingValue, currentSettingValue});
						changes.put(pluginName, settingChange);
					}
				}
			}
		}
		return changes;
	}

	private String pluginConfigNameToPluginName(String configName)
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			try
			{
				String cname = configManager.getConfigDescriptor(pluginManager.getPluginConfigProxy(plugin)).getGroup().value();
				if (cname.equals(configName))
				{
					return plugin.getName();
				}
			}
			catch (NullPointerException ignored)
			{
			}
		}
		return null;
	}

	private Boolean presetMatchesCurrentConfigurations(PluginPreset preset)
	{
		Map<String, ArrayList<String[]>> changes = getChanges(preset);
		return changes.size() == 0;
	}

	public void ignorePlugin(String pluginName)
	{
		ArrayList<String> userIgnoredPlugins = new ArrayList<>(getUserIgnoredPlugins());
		if (!userIgnoredPlugins.contains(pluginName))
		{
			userIgnoredPlugins.add(pluginName);
			String ignores = String.join(",", userIgnoredPlugins);
			configManager.setConfiguration(PLUGIN_CONFIG_NAME, "userIgnoredPlugins", ignores);
		}
	}

	public void ignorePluginSetting(String pluginSetting)
	{
		ArrayList<String> userIgnoredPluginSettings = new ArrayList<>(getUserIgnoredPluginSettings());
		if (!userIgnoredPluginSettings.contains(pluginSetting))
		{
			userIgnoredPluginSettings.add(pluginSetting);
			String ignores = String.join(",", userIgnoredPluginSettings);
			configManager.setConfiguration(PLUGIN_CONFIG_NAME, "userIgnoredPluginSettings", ignores);
		}
	}

	private List<String> getUserIgnoredPluginSettings()
	{
		return Arrays.asList(pluginConfig.getUserIgnoredPluginsSettings().split(","));
	}

	private List<String> getUserIgnoredPlugins()
	{
		return Arrays.asList(pluginConfig.getUserIgnoredPlugins().split(","));
	}

	// Convert ignored plugin names to their config names
	private List<String> getUserIgnoredPluginConfigNames()
	{
		List<String> userIgnoredPlugins = getUserIgnoredPlugins();
		List<String> userIgnoredPluginConfigNames = new ArrayList<>();
		getNotIgnoredPlugins().forEach(plugin ->
		{
			if (userIgnoredPlugins.contains(plugin.getName()) && pluginHasConfigurableSettingsToBeSaved(plugin))
			{
				userIgnoredPluginConfigNames.add(getConfigProxy(plugin).getGroup().value());
			}
		});
		return userIgnoredPluginConfigNames;
	}

	private void setMatchingPresetAsSelected(PluginPreset matchingPreset)
	{
		SwingUtilities.invokeLater(() -> setPresetAsSelected(matchingPreset));
	}

	private void warnFromUnsavedPluginConfigurations()
	{
		final PluginPreset selectedPreset = getSelectedPreset();
		SwingUtilities.invokeLater(() -> displayUnsavedPluginConfigurationsWarning(selectedPreset));
	}

	private PluginPreset getSelectedPreset()
	{
		for (PluginPreset preset : pluginPresets)
		{
			// If getSelected in preset is null, that means it is selected but has unsaved configurations
			if (preset.getSelected() == null || preset.getSelected())
			{
				return preset;
			}
		}
		return null;
	}

	private void displayUnsavedPluginConfigurationsWarning(final PluginPreset selectedPreset)
	{
		setSelectedPresetAsNull(selectedPreset);
	}

	public void createPreset(String presetName)
	{
		presetName = createDefaultPlaceholderNameIfNoNameSet(presetName);

		PluginPreset preset = new PluginPreset(
			Instant.now().toEpochMilli(),
			presetName,
			false,
			getEnabledPlugins(),
			getPluginSettings()
		);
		pluginPresets.add(preset);

		setPresetAsSelected(preset);

		refreshPresets();
		rebuildPluginUi();
	}

	private String createDefaultPlaceholderNameIfNoNameSet(String presetName)
	{
		if (presetName.equals("") || stringContainsInvalidCharacters(presetName))
		{
			presetName = DEFAULT_PRESET_NAME + " " + (pluginPresets.size() + 1);
		}
		return presetName;
	}

	private HashMap<String, Boolean> getEnabledPlugins()
	{
		HashMap<String, Boolean> enabledPlugins = new HashMap<>();
		getNotIgnoredPlugins().forEach(plugin -> enabledPlugins.put(plugin.getName(), pluginManager.isPluginEnabled(plugin)));
		return enabledPlugins;
	}

	private Stream<Plugin> getNotIgnoredPlugins()
	{
		// Return all plugins that all not marked as ignored by default, or by user.
		return pluginManager.getPlugins()
			.stream()
			.filter(plugin ->
				!DEFAULT_IGNORED_PLUGINS.contains(plugin.getName())
			);
	}

	private boolean pluginIsNotIgnored(String pluginName)
	{
		return !DEFAULT_IGNORED_PLUGINS.contains(pluginName);
	}

	private HashMap<String, HashMap<String, String>> getPluginSettings()
	{
		HashMap<String, HashMap<String, String>> pluginSettings = new HashMap<>();

		getNotIgnoredPlugins().forEach(plugin ->
		{
			if (pluginIsNotIgnored(plugin.getName()) && pluginHasConfigurableSettingsToBeSaved(plugin))
			{
				HashMap<String, String> pluginSettingKeyValue = new HashMap<>();

				ConfigDescriptor pluginConfigProxy = getConfigProxy(plugin);
				String groupName = pluginConfigProxy.getGroup().value();

				pluginConfigProxy.getItems().forEach(configItemDescriptor ->
				{
					String key = configItemDescriptor.getItem().keyName();
					if (!keyIsIgnored(key))
					{
						pluginSettingKeyValue.put(key, configManager.getConfiguration(groupName, key));
					}
				});

				pluginSettings.put(groupName, pluginSettingKeyValue);
			}
		});

		HashMap<String, String> runeliteConfigSettingKeyValues = new HashMap<>();

		configManager.getConfigDescriptor(runeLiteConfig).getItems().forEach(configItemDescriptor ->
		{
			String keyName = configItemDescriptor.getItem().keyName();
			String configuration = configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, keyName);
			runeliteConfigSettingKeyValues.put(keyName, configuration);
		});

		pluginSettings.put(RuneLiteConfig.GROUP_NAME, runeliteConfigSettingKeyValues);

		return pluginSettings;
	}

	private boolean keyIsIgnored(String key)
	{
		return DEFAULT_IGNORED_PLUGIN_KEYS.contains(key);
	}

	private Boolean pluginHasConfigurableSettingsToBeSaved(Plugin plugin)
	{
		try
		{
			getConfigProxy(plugin);
		}
		catch (NullPointerException ignore)
		{
			return false;
		}
		return true;
	}

	private ConfigDescriptor getConfigProxy(Plugin plugin)
	{
		return configManager.getConfigDescriptor(pluginManager.getPluginConfigProxy(plugin));
	}

	public void setSelectedPresetAsNull(final PluginPreset selectedPreset)
	{
		pluginPresets.forEach(preset -> preset.setSelected(false));
		if (presetIsValid(selectedPreset))
		{
			selectedPreset.setSelected(null);
		}
		savePresets();
		rebuildPluginUi();
	}

	public void setPresetAsSelected(final PluginPreset selectedPreset)
	{
		pluginPresets.forEach(preset -> preset.setSelected(false));
		if (presetIsValid(selectedPreset))
		{
			selectedPreset.setSelected(true);
		}
		savePresets();
		rebuildPluginUi();
	}

	@SneakyThrows
	public void savePresets()
	{
		PluginPresetsStorage.savePresets(pluginPresets);
	}

	public void refreshPresets()
	{
		pluginPresets.clear();
		loadPresets();
		savePresets();
	}

	public void loadPreset(final PluginPreset preset)
	{
		configChangedFromLoadPreset = true;
		setPresetPluginConfigurations(preset);
		startStopPlugins(preset);
		configChangedFromLoadPreset = false;
	}

	private void setPresetPluginConfigurations(final PluginPreset preset)
	{
		List<String> userIgnoredPluginSettings = getUserIgnoredPluginSettings();
		List<String> userIgnoredPluginConfigNames = getUserIgnoredPluginConfigNames();

		HashMap<String, HashMap<String, String>> pluginSettings = preset.getPluginSettings();
		for (Entry<String, HashMap<String, String>> pluginSetting : pluginSettings.entrySet())
		{
			String pluginConfigName = pluginSetting.getKey();
			if (!userIgnoredPluginConfigNames.contains(pluginConfigName))
			{
				HashMap<String, String> configurations = pluginSettings.get(pluginConfigName);
				for (Entry<String, String> configuration : configurations.entrySet())
				{
					if (!userIgnoredPluginSettings.contains(configuration.getKey()))
					{
						String value = configuration.getValue();
						if (value != null)
						{
							configManager.setConfiguration(pluginConfigName, configuration.getKey(), value);
						}
					}
				}
			}
		}
	}

	private void startStopPlugins(final PluginPreset preset)
	{
		List<String> userIgnoredPlugins = getUserIgnoredPlugins();
		List<String> unsavedExternalPlugins = getUnsavedExternalPlugins(preset);
		HashMap<String, Boolean> enabledPlugins = preset.getEnabledPlugins();

		getNotIgnoredPlugins().forEach(plugin ->
		{
			String pluginName = plugin.getName();
			if (pluginIsNotIgnored(pluginName) && !unsavedExternalPlugins.contains(pluginName) && !userIgnoredPlugins.contains(pluginName))
			{
				Boolean enabled = enabledPlugins.get(pluginName);
				setPluginEnabledAndStartPlugin(plugin, enabled);
			}
		});
	}

	private void setPluginEnabledAndStartPlugin(Plugin plugin, Boolean enabled)
	{
		setPluginEnabled(plugin, enabled);
		startOrStopPlugin(plugin, enabled);
	}

	private void setPluginEnabled(Plugin plugin, Boolean enabled)
	{
		// Turns the RuneLite settings switch on/off
		pluginManager.setPluginEnabled(plugin, enabled);
	}

	@SneakyThrows
	private void startOrStopPlugin(Plugin plugin, Boolean enabled)
	{
		if (enabled)
		{
			pluginManager.startPlugin(plugin);
		}
		else
		{
			pluginManager.stopPlugin(plugin);
		}
	}

	public void deletePreset(final PluginPreset preset)
	{
		pluginPresets.remove(preset);
		savePresets();
		rebuildPluginUi();
	}

	public void updatePreset(final PluginPreset preset)
	{
		preset.setEnabledPlugins(getEnabledPlugins());
		preset.setPluginSettings(getPluginSettings());
		savePresets();
		setPresetAsSelected(preset);
		rebuildPluginUi();
	}

	@SneakyThrows
	public void loadPresets()
	{
		pluginPresets.addAll(PluginPresetsStorage.loadPresets());
	}

	public void importPresetFromClipboard()
	{
		PluginPreset newPreset = sharingManager.importPresetFromClipboard();
		if (!presetIsValid(newPreset))
		{
			return;
		}
		newPreset.setId(Instant.now().toEpochMilli());
		newPreset.setSelected(false);

		pluginPresets.add(newPreset);
		savePresets();
		refreshPresets();
		rebuildPluginUi();
	}

	public void exportPresetToClipboard(final PluginPreset preset)
	{
		sharingManager.exportPresetToClipboard(preset);
	}

	public List<String> getUnsavedExternalPlugins(final PluginPreset preset)
	{
		List<String> newPlugins = new ArrayList<>();
		List<String> pluginsInPreset = new ArrayList<>(preset.getEnabledPlugins().keySet());

		getNotIgnoredPlugins().forEach(plugin -> {
			String pluginName = plugin.getName();
			if (!pluginsInPreset.contains(pluginName))
			{
				newPlugins.add(pluginName);
			}
		});

		return newPlugins;
	}

	public List<String> getMissingExternalPlugins(final PluginPreset preset)
	{
		List<String> missingPlugins = new ArrayList<>();
		List<String> notIgnoredPluginNames = getNotIgnoredPlugins().map(Plugin::getName).collect(Collectors.toList());
		List<String> pluginsInPreset = new ArrayList<>(preset.getEnabledPlugins().keySet());

		pluginsInPreset.forEach(pluginName ->
		{
			if (pluginIsNotIgnored(pluginName) && !notIgnoredPluginNames.contains(pluginName))
			{
				missingPlugins.add(pluginName);
			}
		});

		return missingPlugins;
	}

	private void createPresetFolder()
	{
		final boolean presetFolderWasCreated = PRESETS_DIR.mkdirs();

		if (!presetFolderWasCreated)
		{
			log.info(String.format("Folder already exists %s", PRESETS_DIR.getAbsolutePath()));
		}
	}

	public void rebuildPluginUi()
	{
		pluginPanel.rebuild();
	}

	public boolean stringContainsInvalidCharacters(final String string)
	{
		return !Pattern.compile("(?i)^[ a-ö0-9-_.,;=()+!]+$").matcher(string).matches();
	}
}
