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

import com.google.common.collect.ImmutableMap;
import com.pluginpresets.ui.PluginPresetsPluginPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

@PluginDescriptor(
	name = "Plugin Presets",
	description = "Create presets of your plugin configurations.",
	tags = {"preset", "setups", "plugins"}
)
public class PluginPresetsPlugin extends Plugin
{
	public static final File PRESETS_DIR = new File(RUNELITE_DIR, "presets");
	public static final String HELP_LINK = "https://github.com/antero111/plugin-presets#using-plugin-presets";
	public static final String DEFAULT_PRESET_NAME = "Preset";
	protected static final List<String> IGNORED_PLUGINS = Stream.of("Plugin Presets", "Configuration", "Xtea").collect(Collectors.toList());
	protected static final List<String> IGNORED_KEYS = Stream.of("channel", "oauth", "username", "notesData").collect(Collectors.toList());
	private static final String PLUGIN_NAME = "Plugin Presets";
	private static final String ICON_FILE = "panel_icon.png";

	@Getter
	private final List<PluginPreset> pluginPresets = new ArrayList<>();

	@Getter
	@Setter
	private PluginPreset editedPreset = null;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private ConfigManager configManager;

	@Getter
	@Inject
	private PresetCreatorManager presetCreatorManager;

	private NavigationButton navigationButton;

	private PluginPresetsPluginPanel pluginPanel;

	private PluginPresetsSharingManager sharingManager;

	@Getter
	private PluginPresetsPresetManager presetManager;

	@Override
	protected void startUp()
	{
		PluginPresetsStorage.createPresetFolder();

		pluginPanel = new PluginPresetsPluginPanel(this);

		sharingManager = new PluginPresetsSharingManager(pluginPanel);

		presetManager = new PluginPresetsPresetManager(this, pluginManager, configManager, runeLiteConfig);

		loadPresets();

		rebuildPluginUi();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.priority(8)
			.icon(icon)
			.panel(pluginPanel)
			.popup(ImmutableMap
				.<String, Runnable>builder()
				.put("Open preset folder...", () ->
					LinkBrowser.open(PRESETS_DIR.toString()))
				.build())
			.build();
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown()
	{
		pluginPresets.clear();
		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		sharingManager = null;
		presetManager = null;
		navigationButton = null;
		editedPreset = null;
	}

	@Subscribe
	public void onExternalPluginsChanged(ExternalPluginsChanged externalPluginsChanged)
	{
		SwingUtilities.invokeLater(this::rebuildPluginUi);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (validConfigChange(configChanged))
		{
			SwingUtilities.invokeLater(this::rebuildPluginUi);
		}
	}

	private boolean validConfigChange(ConfigChanged configChanged)
	{
		return !configChanged.getKey().equals("pluginpresetsplugin");
	}

	public List<PluginPreset> getMatchingPresets()
	{
		return presetManager.getMatchingPresets();
	}

	public void createPreset(String presetName)
	{
		PluginPreset preset = presetManager.createPluginPreset(presetName);
		pluginPresets.add(preset);
		presetCreatorManager.close();

		savePresets();
		rebuildPluginUi();
	}

	@SneakyThrows
	public void savePresets()
	{
		PluginPresetsStorage.savePresets(pluginPresets);
	}

	public void refreshPresets()
	{
		pluginPresets.clear();
		loadPresets();
		savePresets();
		rebuildPluginUi();
	}

	@SneakyThrows
	public void loadPreset(final PluginPreset preset)
	{
		presetManager.loadPreset(preset);
	}

	public void deletePreset(final PluginPreset preset)
	{
		pluginPresets.remove(preset);
		savePresets();
		rebuildPluginUi();
	}

	@SneakyThrows
	public void loadPresets()
	{
		pluginPresets.addAll(PluginPresetsStorage.loadPresets());
	}

	public void importPresetFromClipboard()
	{
		PluginPreset newPreset = sharingManager.importPresetFromClipboard();
		presetCreatorManager.close();

		if (newPreset == null)
		{
			return;
		}

		newPreset.setId(Instant.now().toEpochMilli());
		pluginPresets.add(newPreset);

		savePresets();
		refreshPresets();
		rebuildPluginUi();
	}

	public void exportPresetToClipboard(final PluginPreset preset)
	{
		sharingManager.exportPresetToClipboard(preset);
	}

	public void rebuildPluginUi()
	{
		pluginPanel.rebuild();
	}

	public void removeConfiguration(PluginConfig configuration)
	{
		List<PluginConfig> pluginConfigs = editedPreset.getPluginConfigs().stream()
			.filter(c -> !(c.getName().equals(configuration.getName()))).collect(Collectors.toList());
		editedPreset.setPluginConfigs(pluginConfigs);
		updateEditedPreset();
	}

	public void addConfiguration(PluginConfig configuration)
	{
		List<PluginConfig> pluginConfigs = editedPreset.getPluginConfigs();
		pluginConfigs.add(configuration);
		updateEditedPreset();
	}

	private void updateEditedPreset()
	{
		pluginPresets.forEach(plugin ->
		{
			if (plugin.getName().equals(editedPreset.getName()))
			{
				plugin.setPluginConfigs(editedPreset.getPluginConfigs());
			}
		});

		savePresets();
		refreshPresets();
	}
}
