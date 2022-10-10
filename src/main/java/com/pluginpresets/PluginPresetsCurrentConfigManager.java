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

import java.util.ArrayList;
import java.util.List;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.plugins.PluginManager;

public class PluginPresetsCurrentConfigManager
{
	private final PluginManager pluginManager;
	private final ConfigManager configManager;
	private final RuneLiteConfig runeLiteConfig;
	private final CustomSettings customSettings;

	public PluginPresetsCurrentConfigManager(PluginManager pluginManager, ConfigManager configManager, RuneLiteConfig runeLiteConfig, CustomSettings customSettings)
	{
		this.pluginManager = pluginManager;
		this.configManager = configManager;
		this.runeLiteConfig = runeLiteConfig;
		this.customSettings = customSettings;
	}

	public List<PluginConfig> getCurrentConfigs()
	{
		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		pluginManager.getPlugins().forEach(p ->
		{
			String name = p.getName();
			if (!PluginPresetsPlugin.IGNORED_PLUGINS.contains(name))
			{
				Config pluginConfigProxy = pluginManager.getPluginConfigProxy(p);

				boolean enabled = pluginManager.isPluginEnabled(p);

				ArrayList<PluginSetting> pluginSettings = new ArrayList<>();
				String configName = null;

				if (pluginConfigProxy == null)
				{
					configName = p.getClass().getSimpleName().toLowerCase();
				}

				if (pluginConfigProxy != null)
				{
					ConfigDescriptor configDescriptor = configManager.getConfigDescriptor(pluginConfigProxy);
					configName = configDescriptor.getGroup().value();

					configDescriptor.getItems().forEach(i ->
					{
						if (!PluginPresetsPlugin.IGNORED_KEYS.contains(i.key()))
						{
							String settingName = i.name();
							if (i.name().equals(""))
							{
								settingName = PluginPresetsUtils.splitAndCapitalize(settingName);
							}

							String configuration = configManager.getConfiguration(configDescriptor.getGroup().value(), i.key());
							PluginSetting pluginSetting = new PluginSetting(settingName, i.key(),
								configuration, null, null);
							pluginSettings.add(pluginSetting);
						}
					});
				}

				List<CustomSetting> configsCustomSettings = customSettings.getCustomConfigsFor(configName);
				if (!configsCustomSettings.isEmpty())
				{
					// Don't add duplicate custom settings
					ArrayList<String> addedCustomSettings = new ArrayList<>();

					configsCustomSettings.forEach(customSetting ->
					{
						PluginSetting setting = customSetting.getSetting();
						String customConfigName = setting.getCustomConfigName();
						if (!addedCustomSettings.contains(customConfigName))
						{
							String value = configManager.getConfiguration(customConfigName, setting.getKey());
							PluginSetting pluginSetting = new PluginSetting(setting.getName(), setting.getKey(), value, customConfigName, setting.getConfigName());
							pluginSettings.add(pluginSetting);
							addedCustomSettings.add(customConfigName);
						}
					});
				}

				PluginConfig pluginConfig = new PluginConfig(name, configName, enabled, pluginSettings);

				pluginConfigs.add(pluginConfig);
			}
		});

		// Add RuneLite settings
		ArrayList<PluginSetting> runelitePluginSettings = new ArrayList<>();

		PluginConfig runeliteConfig = new PluginConfig("RuneLite", RuneLiteConfig.GROUP_NAME, true, runelitePluginSettings);

		configManager.getConfigDescriptor(runeLiteConfig).getItems().forEach(i ->
		{
			if (!PluginPresetsPlugin.IGNORED_KEYS.contains(i.key()))
			{
				String configuration = configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, i.key());
				PluginSetting pluginSetting = new PluginSetting(i.name(), i.key(),
					configuration, null, null);
				runelitePluginSettings.add(pluginSetting);
			}
		});

		// Add possible custom RuneLite settings
		List<CustomSetting> customRuneLiteSettings = customSettings.getCustomConfigsFor(RuneLiteConfig.GROUP_NAME);
		if (!customRuneLiteSettings.isEmpty())
		{
			customRuneLiteSettings.forEach(customSetting ->
			{
				PluginSetting setting = customSetting.getSetting();
				String value = configManager.getConfiguration(setting.getCustomConfigName(), setting.getKey());
				setting.setValue(value);

				runelitePluginSettings.add(setting);
			});
		}

		pluginConfigs.add(runeliteConfig);

		return pluginConfigs;
	}
}
