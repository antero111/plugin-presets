package com.pluginpresets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This is only used to convert old plugin presets to new format.
 *
 * @deprecated This is old format. From v2, use the new Plugin Preset class
 */
@Data
@AllArgsConstructor
@Deprecated
public class LegacyPluginPreset
{
	private long id;
	private String name;
	private Boolean selected;
	private HashMap<String, Boolean> enabledPlugins;
	private HashMap<String, HashMap<String, String>> pluginSettings;

	/**
	 * Converts legacy format Plugin Presets to current format.
	 */
	public static PluginPreset convert(LegacyPluginPreset legacyPluginPreset, CurrentConfigurations currentConfigurations)
	{
		PluginPreset convertedPreset = new PluginPreset(legacyPluginPreset.getName());
		ArrayList<PluginConfig> pluginConfigs = new ArrayList<>();

		for (Entry<String, Boolean> entry : legacyPluginPreset.getEnabledPlugins().entrySet())
		{
			String name = entry.getKey();
			String configName = legacyGetConfigName(name, currentConfigurations.getPluginConfigs());

			if (configName == null)
			{
				// Could not find plugin from current configurations
				// This means that the legacy preset had configurations to hub plugin that is not currently installed
				// or plugin does not have any configurations e.g. Ammo or Account
				continue;
			}

			HashMap<String, HashMap<String, String>> legacyPluginSettings = legacyPluginPreset.getPluginSettings();
			ArrayList<PluginSetting> pluginSettings = legacyGetSettings(configName, currentConfigurations.getPluginConfigs(), legacyPluginSettings);

			boolean enabled = entry.getValue();
			PluginConfig pluginConfig = new PluginConfig(name, configName, enabled, pluginSettings);
			pluginConfigs.add(pluginConfig);
		}

		convertedPreset.setPluginConfigs(pluginConfigs);
		return convertedPreset;
	}

	private static String legacyGetConfigName(String name, List<PluginConfig> currentConfigurations)
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

	private static ArrayList<PluginSetting> legacyGetSettings(String configName, List<PluginConfig> currentConfigurations,
															  HashMap<String, HashMap<String, String>> legacyPluginSettings)
	{
		ArrayList<PluginSetting> pluginSettings = new ArrayList<>();

		HashMap<String, String> legacyPluginSettingsMap = legacyPluginSettings.get(configName);
		if (legacyPluginSettingsMap == null) // No settings e.g. Ammo plugin
		{
			return pluginSettings;
		}

		legacyPluginSettingsMap.forEach((key, value) ->
		{
			String name = legacyGetSettingName(configName, key, currentConfigurations);
			if (name != null)
			{
				PluginSetting pluginSetting = new PluginSetting(name, key, value, null, null);
				pluginSettings.add(pluginSetting);
			}
		});

		return pluginSettings;
	}

	private static String legacyGetSettingName(String configName, String key, List<PluginConfig> currentConfigurations)
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