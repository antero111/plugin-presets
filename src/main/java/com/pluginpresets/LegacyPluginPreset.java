package com.pluginpresets;

import java.util.HashMap;
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
}