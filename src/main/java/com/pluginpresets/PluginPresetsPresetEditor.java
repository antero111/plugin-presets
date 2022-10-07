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
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginPresetsPresetEditor
{
	private final PluginPresetsPlugin plugin;
	private final CurrentConfigurations currentConfigurations;

	@Getter
	private final PluginPreset editedPreset;

	public PluginPresetsPresetEditor(PluginPresetsPlugin plugin, PluginPreset editedPreset, CurrentConfigurations currentConfigurations)
	{
		this.plugin = plugin;
		this.editedPreset = editedPreset;
		this.currentConfigurations = currentConfigurations;
	}

	public void removeConfigurationFromEdited(PluginConfig configuration)
	{
		removeConfigurationFromEdited(configuration, false);
	}

	private void removeConfigurationFromEdited(PluginConfig configuration, Boolean skipUpdate)
	{
		List<PluginConfig> pluginConfigs = editedPreset.getPluginConfigs().stream()
			.filter(c -> !(c.getName().equals(configuration.getName())))
			.collect(Collectors.toList());

		editedPreset.setPluginConfigs(pluginConfigs);

		if (!skipUpdate)
		{
			updateEditedPreset();
		}
	}

	public void addConfigurationToEdited(PluginConfig configuration)
	{
		addConfigurationToEdited(configuration, false);
	}

	private void addConfigurationToEdited(PluginConfig configuration, Boolean skipUpdate)
	{
		List<PluginConfig> pluginConfigs = editedPreset.getPluginConfigs();
		pluginConfigs.add(configuration);
		editedPreset.setPluginConfigs(pluginConfigs);

		if (!skipUpdate)
		{
			updateEditedPreset();
		}
	}

	public void removeSettingFromEdited(PluginConfig currentConfig, PluginSetting setting)
	{
		editedPreset.getPluginConfigs().forEach(configuration ->
		{
			boolean configToBeRemoved = currentConfig == null || configuration.getName().equals(currentConfig.getName());
			if (configToBeRemoved)
			{
				List<PluginSetting> settings = configuration.getSettings().stream().filter((s -> !s.getKey().equals(setting.getKey()))).collect(Collectors.toList());
				configuration.setSettings(settings);

				boolean lastSetting = configuration.getSettings().isEmpty() && configuration.getEnabled() == null;
				if (lastSetting)
				{
					removeConfigurationFromEdited(configuration);
				}
			}
		});
		updateEditedPreset();
	}

	public void addSettingToEdited(PluginConfig currentConfig, PluginSetting setting)
	{
		boolean noneMatch = editedPreset.getPluginConfigs().stream().noneMatch(c -> c.getName().equals(currentConfig.getName()));
		if (noneMatch)
		{
			ArrayList<PluginSetting> settings = new ArrayList<>();
			settings.add(setting);
			PluginConfig pluginConfig = new PluginConfig(currentConfig.getName(), currentConfig.getConfigName(), null, settings);
			editedPreset.getPluginConfigs().add(pluginConfig);
		}
		else
		{
			editedPreset.getPluginConfigs().forEach(configuration ->
			{
				if (configuration.getName().equals(currentConfig.getName()))
				{
					configuration.getSettings().add(setting);
				}
			});
		}
		updateEditedPreset();
	}

	public void addCustomSettingToEdited(PluginConfig currentConfig, String customSetting)
	{
		String configName;
		String key;

		// Parse user input
		try
		{
			String[] split = customSetting.split("\\.");
			configName = split[0];
			key = split[1].split("=")[0];
		}
		catch (Exception e)
		{
			log.warn("Failed to add custom setting " + customSetting + " to preset. Reason: " + e.getMessage());
			return;
		}

		PluginConfig config = editedPreset.getPluginConfigs().stream()
			.filter(customer -> customer.getConfigName().equals(currentConfig.getConfigName()))
			.findAny()
			.orElse(null);

		if (config == null)
		{
			for (PluginConfig c : currentConfigurations.getPluginConfigs())
			{
				if (c.getConfigName().equals(currentConfig.getConfigName()))
				{
					config = new PluginConfig(c.getName(), c.getConfigName(), null, new ArrayList<>());
				}
			}
			editedPreset.getPluginConfigs().add(config);
		}

		if (config == null)
		{
			log.warn("Could not add custom setting.");
			return;
		}

		String value = plugin.getPresetManager().getConfiguration(configName, key);
		PluginSetting setting = new PluginSetting(PluginPresetsUtils.splitAndCapitalize(key), key, value, configName, config.getConfigName());

		config.getSettings().add(setting);

		updateEditedPreset();
		plugin.refreshPresets(); // Must do refresh to reload custom configs
	}

	public void addEnabledToEdited(PluginConfig currentConfig)
	{
		boolean noneMatch = editedPreset.getPluginConfigs().stream().noneMatch(c -> currentConfig.getName().equals(c.getName()));
		if (noneMatch)
		{
			ArrayList<PluginSetting> settings = new ArrayList<>();
			PluginConfig pluginConfig = new PluginConfig(currentConfig.getName(), currentConfig.getConfigName(), currentConfig.getEnabled(), settings);
			editedPreset.getPluginConfigs().add(pluginConfig);
		}
		else
		{
			editedPreset.getPluginConfigs().forEach(configuration ->
			{
				if (configuration.getName().equals(currentConfig.getName()))
				{
					configuration.setEnabled(currentConfig.getEnabled());
				}
			});
		}
		updateEditedPreset();
	}

	public void removeEnabledFromEdited(PluginConfig currentConfig)
	{
		editedPreset.getPluginConfigs().forEach(configurations ->
		{
			if (configurations.getName().equals(currentConfig.getName()))
			{
				configurations.setEnabled(null);
				if (configurations.getSettings().isEmpty())
				{
					removeConfigurationFromEdited(configurations);
				}
			}
		});

		updateEditedPreset();
	}

	public void addConfigurationToPresets(PluginConfig configuration)
	{
		addConfigurationToEdited(configuration);

		plugin.getPluginPresets().forEach(preset ->
		{
			boolean contains = preset.getPluginConfigs().stream().map(PluginConfig::getName).collect(Collectors.toList()).contains(configuration.getName());
			if (contains)
			{
				List<PluginConfig> pluginConfigs = preset.getPluginConfigs().stream()
					.filter(c -> !(c.getName().equals(configuration.getName())))
					.collect(Collectors.toList());
				preset.setPluginConfigs(pluginConfigs);
			}
			preset.getPluginConfigs().add(configuration);
		});
	}

	public void removeConfigurationFromPresets(PluginConfig configuration)
	{
		removeConfigurationFromEdited(configuration);

		plugin.getPluginPresets().forEach(preset ->
		{
			List<PluginConfig> pluginConfigs = preset.getPluginConfigs().stream()
				.filter(c -> !(c.getName().equals(configuration.getName())))
				.collect(Collectors.toList());
			preset.setPluginConfigs(pluginConfigs);
		});
		plugin.savePresets();
	}

	public void updateConfigurations(PluginConfig presetConfig, PluginConfig currentConfig)
	{
		removeConfigurationFromEdited(presetConfig, true);
		List<String> presetConfigKeys = presetConfig.getSettingKeys();
		List<PluginSetting> currentSettings = currentConfig.getSettings().stream()
			.filter(s -> presetConfigKeys.contains(s.getKey()))
			.collect(Collectors.toList());

		currentConfig.setSettings(currentSettings);

		if (presetConfig.getEnabled() == null)
		{
			currentConfig.setEnabled(null);
		}

		addConfigurationToEdited(currentConfig, true);
		updateEditedPreset();
	}

	public void updateAllModified()
	{
		List<PluginConfig> pluginConfigs = editedPreset.getPluginConfigs();

		pluginConfigs.forEach(presetConfig ->
		{
			PluginConfig currentConfig = currentConfigurations
				.getPluginConfigs()
				.stream()
				.filter(c -> c.getName().equals(presetConfig.getName()))
				.findAny()
				.orElse(null);

			removeConfigurationFromEdited(presetConfig, true);

			List<String> keys = presetConfig
				.getSettings()
				.stream()
				.map(PluginSetting::getKey)
				.collect(Collectors.toList());

			assert currentConfig != null;
			List<PluginSetting> updatedSettings = currentConfig
				.getSettings()
				.stream()
				.filter(s -> keys.contains(s.getKey()))
				.collect(Collectors.toList());

			currentConfig.setSettings(updatedSettings);

			if (presetConfig.getEnabled() == null)
			{
				currentConfig.setEnabled(null);
			}

			addConfigurationToEdited(currentConfig, true);
		});

		updateEditedPreset();
	}

	public void addAll(List<PluginConfig> pluginConfigs)
	{
		for (PluginConfig pluginConfig : pluginConfigs)
		{
			removeConfigurationFromEdited(pluginConfig, true);
			addConfigurationToEdited(pluginConfig, true);
		}
		updateEditedPreset();
	}

	public void removeAll(List<PluginConfig> pluginConfigs)
	{
		for (PluginConfig pluginConfig : pluginConfigs)
		{
			removeConfigurationFromEdited(pluginConfig, true);
		}
		updateEditedPreset();
	}

	public void toggleLocal()
	{
		editedPreset.setLocal(!editedPreset.getLocal());
		updateEditedPreset();
	}

	/**
	 * Update edited presets plugin configs in pluginPresets and then save.
	 */
	private void updateEditedPreset()
	{
		for (PluginPreset preset : plugin.getPluginPresets())
		{
			if (preset.getId() == editedPreset.getId())
			{
				preset.setPluginConfigs(editedPreset.getPluginConfigs());
				preset.setLocal(editedPreset.getLocal());
				plugin.savePresets();
				return;
			}
		}
	}
}
