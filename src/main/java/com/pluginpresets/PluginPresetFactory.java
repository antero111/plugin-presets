/*
 * Copyright (c) 2022, antero111 <https://github.com/antero111>
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import net.runelite.client.config.Config;
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
		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		pluginManager.getPlugins().forEach(plugin ->
		{
			String name = plugin.getName();
			if (!PluginPresetsPlugin.IGNORED_PLUGINS.contains(name))
			{
				Config pluginConfigProxy = pluginManager.getPluginConfigProxy(plugin);

				boolean enabled = pluginManager.isPluginEnabled(plugin);
				
				ArrayList<InnerPluginConfig> innerPluginConfigs = new ArrayList<>();
				String configName = null;

				if (pluginConfigProxy != null)
				{
					ConfigDescriptor configDescriptor = configManager.getConfigDescriptor(pluginConfigProxy);
					configName = configDescriptor.getGroup().value();

					configDescriptor.getItems().forEach(i ->
						{
							if (!PluginPresetsPlugin.IGNORED_KEYS.contains(i.key()))
							{
								InnerPluginConfig innerPluginConfig = new InnerPluginConfig(i.name(), i.key(), configManager.getConfiguration(configDescriptor.getGroup().value(), i.key()));
								innerPluginConfigs.add(innerPluginConfig);
							}

						}
					);

				}
				PluginConfig pluginConfig = new PluginConfig(name, configName, enabled, innerPluginConfigs);
				pluginConfigs.add(pluginConfig);
			}
		});


		// Add RuneLite settings
		ArrayList<InnerPluginConfig> runeliteInnerPluginConfigs = new ArrayList<>();
		PluginConfig runeliteConfig = new PluginConfig("RuneLite", RuneLiteConfig.GROUP_NAME, true, runeliteInnerPluginConfigs);
		configManager.getConfigDescriptor(runeLiteConfig).getItems().forEach(i ->
		{
			if (!PluginPresetsPlugin.IGNORED_KEYS.contains(i.key()))
			{
				InnerPluginConfig innerPluginConfig = new InnerPluginConfig(i.name(), i.key(), configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, i.key()));
				runeliteInnerPluginConfigs.add(innerPluginConfig);
			}
		});

		pluginConfigs.add(runeliteConfig);

		presetName = createDefaultPlaceholderNameIfNoNameSet(presetName);

		return new PluginPreset(
			Instant.now().toEpochMilli(),
			presetName,
			false,
			getEnabledPlugins(),
			getPluginSettings(),
			pluginConfigs
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
