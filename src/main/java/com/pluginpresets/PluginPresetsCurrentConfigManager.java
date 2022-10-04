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
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.plugins.PluginManager;

@Slf4j
public class PluginPresetsCurrentConfigManager
{
	// private final CurrentConfigurations currentConfigurations;
	private final PluginManager pluginManager;
	private final ConfigManager configManager;
	private final RuneLiteConfig runeLiteConfig;

	public PluginPresetsCurrentConfigManager(PluginManager pluginManager, ConfigManager configManager, RuneLiteConfig runeLiteConfig)
	{
		// this.currentConfigurations = currentConfigurations;
		this.pluginManager = pluginManager;
		this.configManager = configManager;
		this.runeLiteConfig = runeLiteConfig;
	}

	public List<PluginConfig> getCurrentConfigs()
	{
		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		// List<PluginSetting> customConfigss = customConfigs.getConfigs();
		// List<String> customConfigNames = customConfigss.stream().map(PluginSetting::getConfigName).collect(Collectors.toList());

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

				// if (customConfigNames.contains(configName))
				// {
				// 	for (PluginSetting setting : customConfigss)
				// 	{
				// 		if (configName.equals(setting.getConfigName()))
				// 		{
				// 			String value = configManager.getConfiguration(setting.getCustomConfigName(), setting.getKey());
				// 			setting.setValue(value);

				// 			pluginSettings.add(setting);
				// 		}
				// 	}
				// }

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

		// if (customConfigNames.contains(RuneLiteConfig.GROUP_NAME))
		// {
		// 	for (PluginSetting setting : customConfigss)
		// 	{
		// 		if (RuneLiteConfig.GROUP_NAME.equals(setting.getConfigName()))
		// 		{
		// 			String value = configManager.getConfiguration(setting.getCustomConfigName(), setting.getKey());
		// 			setting.setValue(value);

		// 			runelitePluginSettings.add(setting);
		// 		}
		// 	}
		// }

		pluginConfigs.add(runeliteConfig);

		return pluginConfigs;
	}
}
