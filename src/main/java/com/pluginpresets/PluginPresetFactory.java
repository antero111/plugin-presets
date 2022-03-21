package com.pluginpresets;

import java.time.Instant;
import java.util.HashMap;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;

public class PluginPresetFactory
{

	private final PluginPresetsPlugin pluginPresets;
	private final PluginManager pluginManager;
	private final ConfigManager configManager;
	private final RuneLiteConfig runeLiteConfig;

	public PluginPresetFactory(PluginPresetsPlugin pluginPresets, PluginManager pluginManager, ConfigManager configManager, RuneLiteConfig runeLiteConfig)
	{
		this.pluginPresets = pluginPresets;
		this.pluginManager = pluginManager;
		this.configManager = configManager;
		this.runeLiteConfig = runeLiteConfig;
	}

	public PluginPreset createPluginPreset(String presetName)
	{
		presetName = createDefaultPlaceholderNameIfNoNameSet(presetName);

		return new PluginPreset(
			Instant.now().toEpochMilli(),
			presetName,
			false,
			getEnabledPlugins(),
			getPluginSettings()
		);
	}

	private String createDefaultPlaceholderNameIfNoNameSet(String presetName)
	{
		if (presetName.equals(""))
		{
			presetName = PluginPresetsPlugin.DEFAULT_PRESET_NAME + " " + (pluginPresets.getPluginPresets().size() + 1);
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
		return !PluginPresetsPlugin.IGNORED_PLUGINS.contains(pluginName);
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
		return PluginPresetsPlugin.IGNORED_KEYS.contains(key);
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
}
