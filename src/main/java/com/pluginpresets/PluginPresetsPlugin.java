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
import java.util.Map.Entry;
import java.util.Set;
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
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
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
	private static final List<String> IGNORED_PLUGINS = Stream.of("Plugin Presets", "Configuration", "Xtea").collect(Collectors.toList());
	private static final List<String> IGNORED_KEYS = Stream.of("channel", "oauth", "username", "notesData").collect(Collectors.toList());
	private static final String PLUGIN_NAME = "Plugin Presets";
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
		preset = null;
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
			if (currentConfigurationsMatchSomePreset())
			{
				setMatchingPresetAsSelected();
			}
			else
			{
				warnFromUnsavedPluginConfigurations();
			}
		}
	}

	private boolean validConfigChange(ConfigChanged configChanged)
	{
		return !(configChangedFromLoadPreset) && !(configChanged.getKey().equals("pluginpresetsplugin"));
	}

	private Boolean currentConfigurationsMatchSomePreset()
	{
		PluginPreset matchingPreset = getPresetThatMatchesCurrentConfigurations();
		if (matchingPreset != null)
		{
			return presetMatchesCurrentConfigurations(matchingPreset);
		}
		return false;
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

	private Boolean presetMatchesCurrentConfigurations(PluginPreset preset)
	{
		HashMap<String, HashMap<String, String>> currentPluginSettings = getPluginSettings();
		// For every plugin
		for (Entry<String, HashMap<String, String>> pluginSettingsFromPreset : preset.getPluginSettings().entrySet())
		{
			// For every plugin setting
			HashMap<String, String> currentSettingsForPlugin = currentPluginSettings.get(pluginSettingsFromPreset.getKey());
			for (Entry<String, String> settingKeyValuePair : pluginSettingsFromPreset.getValue().entrySet())
			{
				String presetSettingValue = settingKeyValuePair.getValue();
				String currentSettingValue = currentSettingsForPlugin.get(settingKeyValuePair.getKey());
				if (presetSettingValue != null && currentSettingValue != null)
				{
					// If values don't match then given preset does not match current configurations
					if (!presetSettingValue.equals(currentSettingValue))
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	private void setMatchingPresetAsSelected()
	{
		PluginPreset matchingPreset = getPresetThatMatchesCurrentConfigurations();
		SwingUtilities.invokeLater(() -> setAsSelected(matchingPreset, true));
	}

	private void warnFromUnsavedPluginConfigurations()
	{
		final PluginPreset selectedPreset = getSelectedPreset();
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

	private void displayUnsavedPluginConfigurationsWarning(final PluginPreset selectedPreset)
	{
		setAsSelected(selectedPreset, null);
	}

	public void createPreset(String presetName)
	{
		presetName = createDefaultPlaceholderNameIfNoNameSet(presetName);

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
		rebuildPluginUi();
	}

	private String createDefaultPlaceholderNameIfNoNameSet(String presetName)
	{
		if (presetName.equals(""))
		{
			presetName = DEFAULT_PRESET_NAME + " " + (pluginPresets.size() + 1);
		}
		return presetName;
	}

	private HashMap<String, Boolean> getEnabledPlugins()
	{
		HashMap<String, Boolean> enabledPlugins = new HashMap<>();

		pluginManager.getPlugins().forEach(plugin ->
		{
			String pluginName = plugin.getName();
			if (pluginIsNotIgnored(pluginName))
			{
				enabledPlugins.put(pluginName, pluginManager.isPluginEnabled(plugin));
			}
		});

		return enabledPlugins;
	}

	private boolean pluginIsNotIgnored(String pluginName)
	{
		return !IGNORED_PLUGINS.contains(pluginName);
	}

	private HashMap<String, HashMap<String, String>> getPluginSettings()
	{
		HashMap<String, HashMap<String, String>> pluginSettings = new HashMap<>();

		pluginManager.getPlugins().forEach(plugin ->
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
		return IGNORED_KEYS.contains(key);
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

	public void setAsSelected(final PluginPreset selectedPreset, final Boolean select)
	{
		pluginPresets.forEach(preset -> preset.setSelected(false));
		if (presetIsValid(selectedPreset))
		{
			selectedPreset.setSelected(select);
		}
		savePresets();
		rebuildPluginUi();
	}

	private Boolean presetIsValid(final PluginPreset selectedPreset)
	{
		return selectedPreset != null;
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

	@SneakyThrows
	public void loadPreset(final PluginPreset preset)
	{
		// Prevents ConfigChanged event from running when programmatically turning plugins on/off
		configChangedFromLoadPreset = true;
		loadPluginSettings(preset);
		startStopPlugins(preset);
		configChangedFromLoadPreset = false;
	}

	private void loadPluginSettings(final PluginPreset preset)
	{
		Set<Entry<String, HashMap<String, String>>> groupNames = preset.getPluginSettings().entrySet();
		setConfigurationsForEveryGroupName(groupNames, preset);
	}

	private void setConfigurationsForEveryGroupName(Set<Entry<String, HashMap<String, String>>> groupNames, PluginPreset preset)
	{
		for (Entry<String, HashMap<String, String>> groupName : groupNames)
		{
			String groupNameKey = groupName.getKey();
			Set<Entry<String, String>> keys = preset.getPluginSettings().get(groupNameKey).entrySet();
			setConfigurationsForEveryKey(keys, groupNameKey);
		}
	}

	private void setConfigurationsForEveryKey(Set<Entry<String, String>> keys, String groupNameKey)
	{
		for (Entry<String, String> key : keys)
		{
			String keyValue = key.getValue();
			if (keyValue != null)
			{
				configManager.setConfiguration(groupNameKey, key.getKey(), keyValue);
			}
		}
	}

	private void startStopPlugins(final PluginPreset preset) throws PluginInstantiationException
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (pluginIsNotIgnored(plugin.getName()))
			{
				Boolean enabledOrDisabled = getPluginState(preset.getEnabledPlugins(), plugin);
				enablePreset(plugin, enabledOrDisabled);
			}
		}
	}

	private Boolean getPluginState(HashMap<String, Boolean> enabledPluginsInPreset, Plugin plugin)
	{
		return enabledPluginsInPreset.get(plugin.getName());
	}

	private void enablePreset(Plugin plugin, Boolean enabledOrDisabled) throws PluginInstantiationException
	{
		// External Plugin Hub plugins that are not yet saved to a preset one is trying to load raises a null exception
		// External plugins will stay as "ignored" (they wont go on/off when presets are loaded) until saved to a plugin preset
		try
		{
			setPluginEnabledAndStartPlugin(plugin, enabledOrDisabled);
		}
		catch (NullPointerException ignore)
		{
		}
	}

	private void setPluginEnabledAndStartPlugin(Plugin plugin, Boolean enabledOrDisabled) throws PluginInstantiationException
	{
		setPluginEnabled(plugin, enabledOrDisabled);
		startOrStopPlugin(plugin, enabledOrDisabled);
	}

	private void setPluginEnabled(Plugin plugin, Boolean enabledOrDisabled)
	{
		// Turns the RuneLite settings switch on/off
		pluginManager.setPluginEnabled(plugin, enabledOrDisabled);
	}

	private void startOrStopPlugin(Plugin plugin, Boolean enabledOrDisabled) throws PluginInstantiationException
	{
		if (enabledOrDisabled)
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
		setAsSelected(preset, true);
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
		List<String> pluginNames = getAllPluginNames();
		List<String> pluginsInPreset = new ArrayList<>(preset.getEnabledPlugins().keySet());

		pluginNames.forEach(pluginName ->
		{
			if (pluginIsNotIgnored(pluginName) && !(pluginsInPreset.contains(pluginName)))
			{
				newPlugins.add(pluginName);
			}
		});

		return newPlugins;
	}

	public List<String> getMissingExternalPlugins(final PluginPreset preset)
	{
		List<String> missingPlugins = new ArrayList<>();
		List<String> pluginNames = getAllPluginNames();
		List<String> pluginsInPreset = new ArrayList<>(preset.getEnabledPlugins().keySet());

		pluginsInPreset.forEach(pluginName ->
		{
			if (pluginIsNotIgnored(pluginName) && !(pluginNames.contains(pluginName)))
			{
				missingPlugins.add(pluginName);
			}
		});

		return missingPlugins;
	}

	private List<String> getAllPluginNames()
	{
		return pluginManager.getPlugins().stream().map(Plugin::getName).collect(Collectors.toList());
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
		return !(Pattern.compile("^[ A-Öa-ö0-9-_.,()+]+$").matcher(string).matches());
	}
}
