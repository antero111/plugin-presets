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
import java.util.stream.Collectors;
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

			if (!currentConfigurations.get(config.getConfigName()).isEmpty())
			{
				for (InnerPluginConfig setting : config.getSettings())
				{
					if (setting.getValue() != null && !currentConfigurations.get(config.getConfigName()).get(setting.getKey()).equals(setting.getValue()))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	private HashMap<String, HashMap<String, String>> getCurrentConfigurationsMap()
	{
		HashMap<String, HashMap<String, String>> configurations = new HashMap<>();

		getCurrentConfigurations().forEach(configuration -> {
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

		preset.getPluginConfigs().forEach(pluginConfig -> {
			pluginConfig.getSettings().forEach(setting -> {
				// Some values e.g. hidden timers like tzhaar or color inputs with "Pick a
				// color" option appears as null
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
			log.warn("Error when {} plugin {}", enabled ? "starting" : "stopping", plugin.getClass().getSimpleName(),
				ex);
		}
	}

	public PluginPreset createPluginPreset(String presetName, boolean empty)
	{
		return new PluginPreset(
			Instant.now().toEpochMilli(),
			createDefaultPlaceholderNameIfNoNameSet(presetName),
			null,
			empty ? new ArrayList<>() : getCurrentConfigurations());
	}

	public List<PluginConfig> getCurrentConfigurations()
	{
		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		pluginManager.getPlugins().forEach(p -> {
			String name = p.getName();
			if (!PluginPresetsPlugin.IGNORED_PLUGINS.contains(name))
			{
				Config pluginConfigProxy = pluginManager.getPluginConfigProxy(p);

				boolean enabled = pluginManager.isPluginEnabled(p);

				ArrayList<InnerPluginConfig> innerPluginConfigs = new ArrayList<>();
				String configName = null;

				if (pluginConfigProxy == null)
				{
					configName = p.getClass().getSimpleName().toLowerCase();
				}

				if (pluginConfigProxy != null)
				{
					ConfigDescriptor configDescriptor = configManager.getConfigDescriptor(pluginConfigProxy);
					configName = configDescriptor.getGroup().value();

					configDescriptor.getItems().forEach(i -> {
						if (!PluginPresetsPlugin.IGNORED_KEYS.contains(i.key()))
						{
							// Don't save 'hidden' plugin configs to presets 
							if (!i.name().equals(""))
							{
								InnerPluginConfig innerPluginConfig = new InnerPluginConfig(i.name(), i.key(),
									configManager.getConfiguration(configDescriptor.getGroup().value(), i.key()));
								innerPluginConfigs.add(innerPluginConfig);
							}
						}

					});

				}

				PluginConfig pluginConfig = new PluginConfig(name, configName, enabled, innerPluginConfigs);

				pluginConfigs.add(pluginConfig);
			}
		});

		// Add RuneLite settings
		ArrayList<InnerPluginConfig> runeliteInnerPluginConfigs = new ArrayList<>();
		PluginConfig runeliteConfig = new PluginConfig("RuneLite", RuneLiteConfig.GROUP_NAME, true,
			runeliteInnerPluginConfigs);
		configManager.getConfigDescriptor(runeLiteConfig).getItems().forEach(i -> {
			if (!PluginPresetsPlugin.IGNORED_KEYS.contains(i.key()))
			{
				InnerPluginConfig innerPluginConfig = new InnerPluginConfig(i.name(), i.key(),
					configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, i.key()));
				runeliteInnerPluginConfigs.add(innerPluginConfig);
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
}
