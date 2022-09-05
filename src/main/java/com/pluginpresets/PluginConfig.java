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

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class for a single RuneLite plugin.
 *
 * @param name       Name of the plugin configuration
 * @param configName RuneLite config name
 * @param enabled    Configuration sidepanel switch value on/off
 * @param settings   List of saved plugins settings.
 *                   Some plugins don't have any configurable settings e.g. Ammo Plugin, in those cases this will be an empty array.
 */
@Data
@AllArgsConstructor
public class PluginConfig
{
	private String name;
	private String configName;
	private Boolean enabled;
	private List<PluginSetting> settings;

	public Boolean match(PluginConfig presetConfig)
	{
		if (presetConfig == null)
		{
			return false;
		}

		Boolean presetEnabled = presetConfig.getEnabled();
		if ((presetEnabled != null && enabled != null) && !presetEnabled.equals(enabled))
		{
			return false;
		}

		List<PluginSetting> currentSettings = settings;
		// Compare plugin settings from preset to current config settings
		for (PluginSetting presetConfigSetting : presetConfig.getSettings())
		{
			// Get current config setting for compared preset setting
			PluginSetting currentConfigSetting = currentSettings.stream()
				.filter(c ->c.getKey().equals(presetConfigSetting.getKey()))
				.findFirst()
				.orElse(null);

			if (currentConfigSetting != null &&
				presetConfigSetting.getValue() != null &&
				!presetConfigSetting.getValue().equals(currentConfigSetting.getValue()))
			{
				return false;
			}
		}
		return true;
	}

	public PluginSetting getSetting(PluginSetting searchedSetting)
	{
		PluginSetting presetSetting = null;
		if (settings != null)
		{
			for (PluginSetting setting : settings)
			{
				if (setting.getKey().equals(searchedSetting.getKey()))
				{
					presetSetting = setting;
					break;
				}
			}
		}
		return presetSetting;
	}

	public List<String> getSettingKeys()
	{
		return settings.stream().map(PluginSetting::getKey).collect(Collectors.toList());
	}

	public boolean containsCustomSettings()
	{
		for (PluginSetting s : settings)
		{
			if (s.getCustomConfigName() != null)
			{
				return true;
			}
		}
		return false;
	}
}