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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;

@Slf4j
public class PluginPresetsPresetManager
{
	private final PluginManager pluginManager;
	private final ConfigManager configManager;
	private final List<String> corePlugins;

	public PluginPresetsPresetManager(PluginManager pluginManager, ConfigManager configManager)
	{
		this.pluginManager = pluginManager;
		this.configManager = configManager;
		this.corePlugins = getCorePlugins();
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
					boolean customConfig = setting.getCustomConfigName() != null;
					String groupName = customConfig ? setting.getCustomConfigName() : pluginConfig.getConfigName();
					configManager.setConfiguration(groupName, setting.getKey(), setting.getValue());

					if (customConfig)
					{
						// Restart plugin that contained custom configs
						Plugin p = findPlugin(pluginConfig.getName(), plugins);

						boolean enabled = pluginConfig.getEnabled() != null;
						if (p != null)
						{
							SwingUtilities.invokeLater(() -> enablePlugin(p, !enabled));
							SwingUtilities.invokeLater(() -> enablePlugin(p, enabled));
						}
					}
				}
			});

			Plugin p = findPlugin(pluginConfig.getName(), plugins);
			Boolean enabled = pluginConfig.getEnabled();
			if (p != null && enabled != null)
			{
				enablePlugin(p, enabled);
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
