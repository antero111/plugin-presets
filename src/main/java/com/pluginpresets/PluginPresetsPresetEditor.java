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

public class PluginPresetsPresetEditor
{
	private final PluginPresetsPlugin plugin;

	@Getter
	private final PluginPreset editedPreset;

	public PluginPresetsPresetEditor(PluginPresetsPlugin plugin, PluginPreset editedPreset)
	{
		this.editedPreset = editedPreset;
		this.plugin = plugin;
	}

	private List<PluginConfig> getCurrentConfigurations(PluginPresetsPlugin plugin)
	{
		return plugin.getPresetManager().getCurrentConfigurations();
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
		editedPreset.getPluginConfigs().forEach(configurations ->
		{
			if (currentConfig == null || configurations.getConfigName().equals(currentConfig.getConfigName()))
			{
				configurations.getSettings().removeIf(s -> s.getKey().equals(setting.getKey()));

				if (configurations.getSettings().isEmpty() && configurations.getEnabled() == null)
				{
					removeConfigurationFromEdited(configurations);
				}
			}
		});
		updateEditedPreset();
	}

	public void addSettingToEdited(PluginConfig currentConfig, PluginSetting setting)
	{
		if (editedPreset.getPluginConfigs().stream()
			.noneMatch(c -> c.getConfigName().equals(currentConfig.getConfigName())))
		{
			ArrayList<PluginSetting> settings = new ArrayList<>();
			settings.add(setting);
			currentConfig.setSettings(settings);
			currentConfig.setEnabled(null);
			editedPreset.getPluginConfigs().add(currentConfig);
		}
		else
		{
			editedPreset.getPluginConfigs().forEach(configuration ->
			{
				if (configuration.getConfigName().equals(currentConfig.getConfigName()))
				{
					configuration.getSettings().add(setting);
				}
			});
		}
		updateEditedPreset();
	}

	public void addEnabledToEdited(PluginConfig currentConfig)
	{
		if (editedPreset.getPluginConfigs().stream()
			.noneMatch(c -> c.getConfigName().equals(currentConfig.getConfigName())))
		{
			ArrayList<PluginSetting> settings = new ArrayList<>();

			currentConfig.setEnabled(currentConfig.getEnabled());
			currentConfig.setSettings(settings);
			editedPreset.getPluginConfigs().add(currentConfig);
		}
		else
		{
			editedPreset.getPluginConfigs().forEach(configuration ->
			{
				if (configuration.getConfigName().equals(currentConfig.getConfigName()))
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
			if (configurations.getConfigName().equals(currentConfig.getConfigName()))
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
			boolean contains = preset.getPluginConfigs().stream().map(PluginConfig::getName)
				.collect(Collectors.toList()).contains(configuration.getName());
			if (contains)
			{
				List<PluginConfig> pluginConfigs = preset.getPluginConfigs().stream()
					.filter(c -> !(c.getName().equals(configuration.getName())))
					.collect(Collectors.toList());
				preset.setPluginConfigs(pluginConfigs);
			}
			preset.getPluginConfigs().add(configuration);
		});
		plugin.savePresets();
		plugin.refreshPresets();
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
		plugin.refreshPresets();
	}

	public void updateAllModified()
	{
		List<PluginConfig> pluginConfigs = getEditedPreset().getPluginConfigs();
		List<PluginConfig> currentConfigurations = getCurrentConfigurations(plugin);

		pluginConfigs.forEach(presetConfig ->
		{
			PluginConfig currentConfig = currentConfigurations
				.stream()
				.filter(c -> c.getConfigName().equals(presetConfig.getConfigName()))
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

			currentConfig.setSettings((ArrayList<PluginSetting>) updatedSettings);

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
		for (PluginConfig pluginConfig : pluginConfigs) {
			addConfigurationToEdited(pluginConfig, true);
		}
		updateEditedPreset();
	}

	public void removeAll(List<PluginConfig> pluginConfigs)
	{
		for (PluginConfig pluginConfig : pluginConfigs) {
			removeConfigurationFromEdited(pluginConfig, true);
		}
		updateEditedPreset();
	}

	public void toggleAll(boolean enable)
	{
		if (enable)
		{
			List<PluginConfig> currentConfigurations = getCurrentConfigurations(plugin);
			editedPreset.setPluginConfigs(currentConfigurations);
		}
		else
		{
			List<PluginConfig> pluginConfigs = editedPreset.getPluginConfigs();
			pluginConfigs.clear();
		}
		updateEditedPreset();
	}

	private PluginPreset getPreset()
	{
		for (PluginPreset preset : plugin.getPluginPresets())
		{
			if (preset.getId() == editedPreset.getId())
			{
				return preset;
			}
		}
		return null;
	}

	public void toggleLocal()
	{
		editedPreset.setLocal(!editedPreset.getLocal());

		PluginPreset preset = getPreset();
		if (preset != null)
		{
			preset.setLocal(editedPreset.getLocal());
		}

		plugin.savePresets();
	}

	private void updateEditedPreset()
	{
		PluginPreset preset = getPreset();
		if (preset != null)
		{
			preset.setPluginConfigs(editedPreset.getPluginConfigs());
		}

		plugin.savePresets();
		plugin.refreshPresets();
	}
}
