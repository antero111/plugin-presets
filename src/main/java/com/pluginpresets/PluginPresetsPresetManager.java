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

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;

@Slf4j
public class PluginPresetsPresetManager
{
	private static final List<String> IGNORED_PLUGINS = Stream.of("Plugin Presets", "Configuration", "Xtea").collect(Collectors.toList());

	/**
	 * Non-user configurable settings that don't contain any context or user
	 * sensitive data that should not to be saved to presets
	 */
	private static final List<String> IGNORED_KEYS = Stream.of("channel", "oauth", "username", "notesData", "tzhaarStartTime", "tzhaarLastTime", "chatsData", "previousPartyId", "lastWorld", "tab", "position").collect(Collectors.toList());

	private final PluginPresetsPlugin plugin;
	private final PluginManager pluginManager;
	private final ConfigManager configManager;
	private final RuneLiteConfig runeLiteConfig;
	private final List<String> corePlugins;

	public PluginPresetsPresetManager(PluginPresetsPlugin plugin, PluginManager pluginManager,
									  ConfigManager configManager, RuneLiteConfig runeLiteConfig)
	{
		this.plugin = plugin;
		this.pluginManager = pluginManager;
		this.configManager = configManager;
		this.runeLiteConfig = runeLiteConfig;
		this.corePlugins = getCorePlugins();
	}

	/**
	 * Returns a list of presets matching current configuration.
	 */
	public List<PluginPreset> getMatchingPresets()
	{
		HashMap<String, HashMap<String, String>> currentConfigurations = getCurrentConfigurationsMap();

		ArrayList<PluginPreset> presets = new ArrayList<>();

		for (PluginPreset preset : plugin.getPluginPresets())
		{
			if (configurationsMatch(preset, currentConfigurations))
			{
				presets.add(preset);
			}
		}

		return presets;
	}

	private boolean configurationsMatch(PluginPreset preset,
										HashMap<String, HashMap<String, String>> currentConfigurations)
	{
		for (PluginConfig config : preset.getPluginConfigs())
		{
			if (config.getEnabled() != null)
			{
				try
				{
					if (!currentConfigurations.get(config.getConfigName()).get("enabled").equals(config.getEnabled().toString()))
					{
						return false;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}
			}

			try
			{
				HashMap<String, String> configurations = currentConfigurations.get(config.getConfigName());
				if (!configurations.isEmpty())
				{
					for (PluginSetting setting : config.getSettings())
					{
						String string = configurations.get(setting.getKey());
						if (string == null)
						{
							continue;
						}

						boolean equals = string.equals(setting.getValue());
						if (setting.getValue() != null && !equals)
						{
							return false;
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				return false;
			}
		}

		return true;
	}

	private HashMap<String, HashMap<String, String>> getCurrentConfigurationsMap()
	{
		HashMap<String, HashMap<String, String>> configurations = new HashMap<>();

		getCurrentConfigurations().forEach(configuration ->
		{
			HashMap<String, String> settings = new HashMap<>();

			configuration.getSettings().forEach(setting -> settings.put(setting.getKey(), setting.getValue()));

			settings.put("enabled", configuration.getEnabled().toString());

			configurations.put(configuration.getConfigName(), settings);

		});

		return configurations;
	}

	/**
	 * Loads settings from given preset.
	 */
	public void loadPreset(PluginPreset preset)
	{
		Collection<Plugin> plugins = pluginManager.getPlugins();

		preset.getPluginConfigs().forEach(pluginConfig ->
		{
			pluginConfig.getSettings().forEach(setting ->
			{
				// Some values e.g. hidden timers like tzhaar
				// or color inputs with "Pick a color" option appears as null
				if (setting.getValue() != null)
				{
					configManager.setConfiguration(pluginConfig.getConfigName(), setting.getKey(), setting.getValue());
				}
			});

			Plugin p = findPlugin(pluginConfig.getName(), plugins);
			if (p != null)
			{
				if (pluginConfig.getEnabled() != null)
				{
					enablePlugin(p, pluginConfig.getEnabled());
				}
			}
		});

	}

	private Plugin findPlugin(String plugin, Collection<Plugin> plugins)
	{
		for (Plugin p : plugins)
		{
			if (p.getName().equals(plugin))
			{
				return p;
			}
		}
		return null;
	}

	private void enablePlugin(Plugin plugin, boolean enabled)
	{
		pluginManager.setPluginEnabled(plugin, enabled);

		try
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
		catch (PluginInstantiationException ex)
		{
			log.warn("Error when {} plugin {}", enabled ? "starting" : "stopping", plugin.getClass().getSimpleName(), ex);
		}
	}

	public PluginPreset createPluginPreset(String presetName, boolean empty)
	{
		return new PluginPreset(
			Instant.now().toEpochMilli(),
			createDefaultPlaceholderNameIfNoNameSet(presetName),
			null,
			true, // Local by default
			empty ? new ArrayList<>() : getCurrentConfigurations());
	}

	public List<PluginConfig> getCurrentConfigurations()
	{
		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		pluginManager.getPlugins().forEach(p ->
		{
			String name = p.getName();
			if (!IGNORED_PLUGINS.contains(name))
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
						if (!IGNORED_KEYS.contains(i.key()))
						{
							String settingName = i.name();
							if (i.name().equals(""))
							{
								settingName = Utils.splitAndCapitalize(settingName);
							}

							PluginSetting pluginSetting = new PluginSetting(settingName, i.key(),
								configManager.getConfiguration(configDescriptor.getGroup().value(), i.key()));
							pluginSettings.add(pluginSetting);
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
			if (!IGNORED_KEYS.contains(i.key()))
			{
				PluginSetting pluginSetting = new PluginSetting(i.name(), i.key(),
					configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, i.key()));
				runelitePluginSettings.add(pluginSetting);
			}
		});

		pluginConfigs.add(runeliteConfig);

		return pluginConfigs;
	}

	private String createDefaultPlaceholderNameIfNoNameSet(String presetName)
	{
		if (presetName.equals(""))
		{
			presetName = PluginPresetsPlugin.DEFAULT_PRESET_NAME + " " + (plugin.getPluginPresets().size() + 1);
		}
		return presetName;
	}

	public boolean isExternalPlugin(String pluginName)
	{
		if (pluginName.equals("RuneLite"))
		{
			return false;
		}
		return !corePlugins.contains(pluginName);
	}

	public boolean isExternalPluginInstalled(String pluginName)
	{
		Collection<Plugin> plugins = pluginManager.getPlugins();
		List<String> names = plugins.stream().map(Plugin::getName).collect(Collectors.toList());
		return names.contains(pluginName);
	}

	private List<String> getCorePlugins()
	{
		ArrayList<String> pluginNames = new ArrayList<>();
		try
		{
			ClassPath classPath = ClassPath.from(pluginManager.getClass().getClassLoader());
			List<Class<?>> plugins = classPath.getTopLevelClassesRecursive("net.runelite.client.plugins").stream()
				.map(ClassInfo::load)
				.collect(Collectors.toList());

			for (Class<?> clazz : plugins)
			{
				PluginDescriptor pluginDescriptor = clazz.getAnnotation(PluginDescriptor.class);
				if (pluginDescriptor != null)
				{
					pluginNames.add(pluginDescriptor.name());
				}
			}

		}
		catch (IOException e)
		{
			log.error("Error getting core plugins", e);
		}
		return pluginNames;
	}

	/**
	 * Converts legacy format Plugin Presets to current format.
	 */
	public PluginPreset convertLegacyPreset(LegacyPluginPreset legacyPluginPreset)
	{
		PluginPreset convertedPreset = createPluginPreset(legacyPluginPreset.getName(), true);
		List<PluginConfig> currentConfigurations = getCurrentConfigurations();

		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		for (Entry<String, Boolean> entry : legacyPluginPreset.getEnabledPlugins().entrySet())
		{
			String name = entry.getKey();
			String configName = legacyGetConfigName(name, currentConfigurations);

			if (configName == null)
			{
				// Could not find plugin from current configurations
				// This means that the legacy preset had configurations to hub plugin that is not currently installed
				// or plugin does not have any configurations e.g. Ammo or Account
				continue;
			}

			HashMap<String, HashMap<String, String>> legacyPluginSettings = legacyPluginPreset.getPluginSettings();
			ArrayList<PluginSetting> pluginSettings = legacyGetSettings(configName, currentConfigurations, legacyPluginSettings);

			boolean enabled = entry.getValue();
			PluginConfig pluginConfig = new PluginConfig(name, configName, enabled, pluginSettings);
			pluginConfigs.add(pluginConfig);
		}

		convertedPreset.setPluginConfigs(pluginConfigs);
		return convertedPreset;
	}

	private String legacyGetConfigName(String name, List<PluginConfig> currentConfigurations)
	{
		for (PluginConfig pluginConfig : currentConfigurations)
		{
			if (pluginConfig.getName().equals(name))
			{
				return pluginConfig.getConfigName();
			}
		}

		return null;
	}

	private ArrayList<PluginSetting> legacyGetSettings(String configName, List<PluginConfig> currentConfigurations,
													   HashMap<String, HashMap<String, String>> legacyPluginSettings)
	{
		ArrayList<PluginSetting> pluginSettings = new ArrayList<>();

		HashMap<String, String> legacyPluginSettingsMap = legacyPluginSettings.get(configName);
		if (legacyPluginSettingsMap == null) // No settings e.g. Ammo plugin
		{
			return pluginSettings;
		}

		legacyPluginSettingsMap.forEach((key, value) -> {
			String name = legacyGetSettingName(configName, key, currentConfigurations);
			if (name != null)
			{
				PluginSetting pluginSetting = new PluginSetting(name, key, value);
				pluginSettings.add(pluginSetting);
			}
		});

		return pluginSettings;
	}

	private String legacyGetSettingName(String configName, String key, List<PluginConfig> currentConfigurations)
	{
		for (PluginConfig pluginConfig : currentConfigurations)
		{
			if (pluginConfig.getConfigName().equals(configName))
			{
				for (PluginSetting setting : pluginConfig.getSettings())
				{
					if (setting.getKey().equals(key))
					{
						return setting.getName();
					}
				}
			}
		}

		return null;
	}
}
