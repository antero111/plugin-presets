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
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;

/**
 * Handles loading presets and creating new ones
 */
@Slf4j
@Singleton
public class PluginPresetsPresetManager
{
	private final PluginManager pluginManager;
	private final ConfigManager configManager;
	private final List<String> corePlugins;

	@Inject
	public PluginPresetsPresetManager(PluginManager pluginManager, ConfigManager configManager)
	{
		this.pluginManager = pluginManager;
		this.configManager = configManager;
		this.corePlugins = getCorePlugins();
	}

	/**
	 * Disable a preset, turn every plugin off that has the enabled==true
	 *
	 * @param preset     the preset to be disabled
	 * @param onComplete callback invoked once preset is loaded
	 */
	public void disablePreset(PluginPreset preset, Runnable onComplete)
	{
		Collection<Plugin> plugins = pluginManager.getPlugins();

		preset.getPluginConfigs().forEach(pluginConfig ->
		{
			Plugin plugin = findPlugin(pluginConfig.getName(), plugins);

			// Set plugin off
			Boolean enabled = pluginConfig.getEnabled();
			if (plugin != null && enabled != null && enabled)
			{
				enablePlugin(plugin, false);
			}
		});

		onComplete.run();
	}

	/**
	 * Loads a preset, changing its specified settings and enabling/disabling its plugins.
	 *
	 * @param preset     the preset to be loaded
	 * @param onComplete callback invoked once preset is loaded
	 */
	public void loadPreset(PluginPreset preset, Runnable onComplete)
	{
		Collection<Plugin> plugins = pluginManager.getPlugins();

		// A plugin that contains custom settings should be restarted asynchronously.
		// However, it only needs to be restarted if it's not going to be toggled by the preset
		Map<Plugin, Boolean> customPluginsToRestart = preset.getPluginConfigs().stream()
			.filter(PluginConfig::containsCustomSettings)
			.map(config -> {
				Plugin plugin = findPlugin(config.getName(), plugins);
				Boolean enabled = plugin != null ? pluginManager.isPluginEnabled(plugin) : null;
				Boolean shouldEnable = config.getEnabled();
				return shouldEnable == null || Objects.equals(enabled, shouldEnable) ? plugin : null;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(p -> p, pluginManager::isPluginEnabled));

		preset.getPluginConfigs().forEach(pluginConfig ->
		{
			Plugin plugin = findPlugin(pluginConfig.getName(), plugins);

			pluginConfig.getSettings().forEach(setting ->
			{
				// Some values e.g. hidden timers like tzhaar
				// or color inputs with "Pick a color" option appears as null
				String value = setting.getValue();
				if (value != null)
				{
					String customConfigName = setting.getCustomConfigName();
					boolean customConfig = customConfigName != null;
					String groupName = customConfig ? customConfigName : pluginConfig.getConfigName();
					// Set configuration
					configManager.setConfiguration(groupName, setting.getKey(), value);
				}
			});

			// Set plugin on/off
			Boolean enabled = pluginConfig.getEnabled();
			if (plugin != null && enabled != null)
			{
				enablePlugin(plugin, enabled);
			}
		});

		if (customPluginsToRestart.isEmpty())
		{
			onComplete.run();
		}
		else
		{
			// Toggle plugin immediately, then toggle again later (strange things happen otherwise)
			customPluginsToRestart.forEach((plugin, enabled) -> enablePlugin(plugin, !enabled, false));
			SwingUtilities.invokeLater(() -> {
				customPluginsToRestart.forEach((plugin, enabled) -> enablePlugin(plugin, enabled, false));
				onComplete.run();
			});
		}
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
		enablePlugin(plugin, enabled, false);
	}

	private void enablePlugin(Plugin plugin, boolean enabled, boolean skipSetEnable)
	{
		if (!skipSetEnable)
		{
			pluginManager.setPluginEnabled(plugin, enabled);
		}

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

	public PluginPreset createPluginPreset(String presetName)
	{
		return new PluginPreset(presetName);
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
		return pluginManager.getPlugins().stream()
			.map(Plugin::getName).collect(Collectors.toList())
			.contains(pluginName);
	}

	public String getConfiguration(String groupName, String key)
	{
		return configManager.getConfiguration(groupName, key);
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
