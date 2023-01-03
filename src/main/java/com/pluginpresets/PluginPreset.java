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
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.config.Keybind;

/**
 * A single preset which contains multiple PluginConfigs, each of which contains various PluginSettings.
 *
 * @param id            Time of creation used as id
 * @param name          Name of the preset
 * @param keybind       Used to enable the preset without the side panel. (Optional)
 * @param local         Used to identify whether the preset is stored in /presets or settings.properties
 * @param loadOnFocus   Used to enable the preset when client is (un)focused (Optional)
 * @param pluginConfigs List of saved plugin configurations.
 */
public class PluginPreset
{
	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private Keybind keybind;

	@Getter
	@Setter
	private Boolean local;

	/**
	 * True when loaded on focus and false when loaded on unfocus.
	 */
	@Getter
	@Setter
	private Boolean loadOnFocus;

	@Getter
	@Setter
	private List<PluginConfig> pluginConfigs;

	public PluginPreset(String name)
	{
		this.id = Instant.now().toEpochMilli();
		this.name = name;
		this.keybind = null;
		this.local = true;
		this.loadOnFocus = null;
		this.pluginConfigs = new ArrayList<>();
	}

	public Boolean match(PluginPreset preset)
	{
		for (PluginConfig presetConfig : pluginConfigs)
		{
			for (PluginConfig comparedConfig : preset.getPluginConfigs())
			{
				if (comparedConfig.getName().equals(presetConfig.getName()))
				{
					if (!presetConfig.match(comparedConfig))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public Boolean match(CurrentConfigurations currentConfigurations)
	{
		for (PluginConfig presetConfig : pluginConfigs)
		{
			PluginConfig currentConfig = null;
			for (PluginConfig config : currentConfigurations.getPluginConfigs())
			{
				if (config.getName().equals(presetConfig.getName()))
				{
					currentConfig = config;
					break;
				}
			}

			if (currentConfig == null)
			{
				continue;
			}

			if (presetConfig.getEnabled() != null && !presetConfig.getEnabled().equals(currentConfig.getEnabled()))
			{
				return false;
			}

			List<PluginSetting> currentSettings = currentConfig.getSettings();
			// Compare plugin settings from preset to current config settings
			for (PluginSetting presetConfigSetting : presetConfig.getSettings())
			{
				// Get current config setting for compared preset setting
				PluginSetting currentConfigSetting = currentSettings.stream()
					.filter(c -> c.getKey().equals(presetConfigSetting.getKey()))
					.findFirst()
					.orElse(null);

				if (currentConfigSetting != null &&
					presetConfigSetting.getValue() != null &&
					!presetConfigSetting.getValue().equals(currentConfigSetting.getValue()))
				{
					return false;
				}
			}
		}
		return true;
	}

	public PluginConfig getConfig(final PluginConfig searchedConfig)
	{
		PluginConfig presetConfig = null;
		for (PluginConfig config : pluginConfigs)
		{
			if (config.getName().equals(searchedConfig.getName()))
			{
				presetConfig = config;
				break;
			}
		}
		return presetConfig;
	}

	public boolean isEmpty()
	{
		return pluginConfigs.isEmpty();
	}
}
