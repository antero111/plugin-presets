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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pluginpresets.ui.PluginPresetsPluginPanel;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.runelite.api.GameState;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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
	static final List<String> IGNORED_PLUGINS = Stream.of("Plugin Presets", "Configuration", "Xtea").collect(Collectors.toList());
	/**
	 * Non-user configurable settings that don't contain any context or user
	 * sensitive data that should not to be saved to presets
	 */
	static final List<String> IGNORED_KEYS = Stream.of("channel", "oauth", "username", "notesData", "tzhaarStartTime", "tzhaarLastTime", "chatsData", "previousPartyId", "lastWorld", "tab", "position").collect(Collectors.toList());
	private static final String PLUGIN_NAME = "Plugin Presets";
	private static final String ICON_FILE = "panel_icon.png";
	private static final String CONFIG_GROUP = "pluginpresets";
	private static final String CONFIG_KEY_PRESETS = "presets";
	private static final String CONFIG_KEY_AUTO_UPDATE = "autoUpdate";

	@Getter
	private final HashMap<Keybind, PluginPreset> keybinds = new HashMap<>();

	@Getter
	private final List<PluginPreset> pluginPresets = new ArrayList<>();

	@Getter
	@Inject
	private CurrentConfigurations currentConfigurations;

	@Getter
	@Inject
	private CustomSettingsManager customSettingsManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private KeyManager keyManager;

	private NavigationButton navigationButton;

	private PluginPresetsPluginPanel pluginPanel;

	@Getter
	@Inject
	private PluginPresetsPresetManager presetManager;

	@Inject
	private PluginPresetsStorage presetStorage;

	@Getter
	@Setter
	private PluginPresetsPresetEditor presetEditor;

	@Getter
	@Setter
	private PluginPresetsPresetEditor autoUpdater;

	@Getter
	private Boolean loggedIn = false; // Used to inform that keybinds don't work in login screen

	private Boolean loadingPreset = false;
	private final KeyListener keybindListener = new KeyListener()
	{
		@Override
		public void keyTyped(KeyEvent e)
		{
			// Ignore
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			PluginPreset preset = keybinds.get(new Keybind(e));
			if (preset != null)
			{
				loadPreset(preset);
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			// Ignore
		}
	};

	@Getter
	@Setter
	private Boolean focusChangedPaused = false;

	@Override
	protected void startUp()
	{
		PluginPresetsStorage.createPresetFolder();
		pluginPanel = new PluginPresetsPluginPanel(this);

		loadPresets();
		updateCurrentConfigurations();
		setupAutoUpdater();
		savePresets();
		rebuildPluginUi();

		presetStorage.watchFolderChanges();

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

		keyManager.registerKeyListener(keybindListener);
	}

	@Override
	protected void shutDown()
	{
		pluginPresets.clear();
		keybinds.clear();
		autoUpdater = null;

		presetStorage.stopWatcher();
		clientToolbar.removeNavigation(navigationButton);
		keyManager.unregisterKeyListener(keybindListener);
		presetStorage.deletePresetFolderIfEmpty();

		pluginPanel = null;
		presetEditor = null;
		navigationButton = null;
	}

	@Subscribe
	public void onExternalPluginsChanged(ExternalPluginsChanged externalPluginsChanged)
	{
		updateCurrentConfigurations();
		SwingUtilities.invokeLater(this::rebuildPluginUi);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (validConfigChange(configChanged) && !loadingPreset)
		{
			updateCurrentConfigurations();
			if (autoUpdater != null)
			{
				autoUpdater.updateAllModified();
			}
			else
			{
				SwingUtilities.invokeLater(this::rebuildPluginUi);
			}
		}
	}

	public void updateCurrentConfigurations()
	{
		currentConfigurations.update();
	}

	private boolean validConfigChange(ConfigChanged configChanged)
	{
		return !(configChanged.getKey().equals("pluginpresetsplugin") || configChanged.getGroup().equals("pluginpresets"));
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		loggedIn = event.getGameState() == GameState.LOGGED_IN;
		SwingUtilities.invokeLater(this::rebuildPluginUi);
	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged)
	{
		if (!focusChangedPaused)
		{
			boolean focused = focusChanged.isFocused();
			for (PluginPreset preset : pluginPresets)
			{
				Boolean loadOnFocus = preset.getLoadOnFocus();
				if (loadOnFocus != null && loadOnFocus == focused)
				{
					loadPreset(preset);
				}
			}
		}
	}

	public void updateConfig()
	{
		List<PluginPreset> syncPresets = pluginPresets.stream()
			.filter(preset -> !preset.getLocal())
			.collect(Collectors.toList());

		syncPresets.forEach(preset -> preset.setLocal(null));

		if (syncPresets.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY_PRESETS);
		}
		else
		{
			final String json = gson.toJson(syncPresets);
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_PRESETS, json);
		}

		syncPresets.forEach(preset -> preset.setLocal(false));

		// If all presets are saved to config, do a refresh since...
		// ...presetStorage folder watcher does not recognize any file change and doesn't refresh presets.
		if (syncPresets.size() == pluginPresets.size())
		{
			refreshPresets();
		}
	}

	private void loadConfig(String json)
	{
		if (Strings.isNullOrEmpty(json))
		{
			return;
		}

		final List<PluginPreset> configPresetData = gson.fromJson(json, new TypeToken<ArrayList<PluginPreset>>()
		{
		}.getType());

		configPresetData.forEach(preset -> preset.setLocal(false));
		pluginPresets.addAll(configPresetData);
	}

	public void createPreset(String presetName, boolean empty)
	{
		if (presetName.equals(""))
		{
			presetName = PluginPresetsPlugin.DEFAULT_PRESET_NAME + " " + (pluginPresets.size() + 1);
		}

		PluginPreset preset = presetManager.createPluginPreset(presetName);
		if (!empty)
		{
			preset.setPluginConfigs(currentConfigurations.getPluginConfigs());
		}

		pluginPresets.add(preset);

		savePresets();
	}

	/**
	 * Saves presets to preset folder and RuneLite config.
	 * Changes in preset directory or config causes refreshPresets() to run.
	 */
	@SneakyThrows
	public void savePresets()
	{
		presetStorage.savePresets(pluginPresets);
		updateConfig();
	}

	/**
	 * Clears presets from memory, loads them again and then rebuilds ui.
	 */
	public void refreshPresets()
	{
		pluginPresets.clear();
		loadPresets();
		rebuildPluginUi();
	}

	@SneakyThrows
	public void loadPreset(final PluginPreset preset)
	{
		if (preset.match(currentConfigurations))
		{
			return;
		}

		loadingPreset = true;

		// Auto updater gets disabled if loading some preset
		if (autoUpdater != null)
		{
			setAutoUpdater(null);
		}

		presetManager.loadPreset(preset, () -> {
			loadingPreset = false;

			updateCurrentConfigurations();
			rebuildPluginUi();
		});
	}

	public void deletePreset(final PluginPreset preset)
	{
		pluginPresets.remove(preset);
		savePresets();
	}

	/**
	 * Loads presets from preset folder and RuneLite config and adds them to plugin memory.
	 */
	@SneakyThrows
	public void loadPresets()
	{
		pluginPresets.addAll(presetStorage.loadPresets());
		loadConfig(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_PRESETS));
		pluginPresets.sort(Comparator.comparing(PluginPreset::getName)); // Keep presets in order
		customSettingsManager.parseCustomSettings(pluginPresets);
		cacheKeybinds();
	}

	private void cacheKeybinds()
	{
		keybinds.clear();
		pluginPresets.forEach(preset ->
		{
			Keybind keybind = preset.getKeybind();
			if (keybind != null && keybinds.get(keybind) == null)
			{
				keybinds.put(keybind, preset);
			}
		});
	}

	private void setupAutoUpdater()
	{
		String configuration = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_AUTO_UPDATE);
		if (configuration != null)
		{
			long id = Long.parseLong(configuration);
			boolean failed = true;
			for (PluginPreset p : pluginPresets)
			{
				if (p.getId() == id)
				{
					PluginPresetsPresetEditor autoUpdater = new PluginPresetsPresetEditor(this, p, currentConfigurations);
					setAutoUpdater(autoUpdater);
					failed = false;
				}
			}

			// If auto preset does not exist or it got deleted, unset autoUpdate configuration 
			if (failed)
			{
				setAutoUpdatedPreset(null);
			}
			else
			{
				autoUpdater.updateAllModified();
			}
		}
	}

	public void setAutoUpdatedPreset(Long id)
	{
		if (id == null)
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY_AUTO_UPDATE);
			setAutoUpdater(null);
			rebuildPluginUi();
		}
		else
		{
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_AUTO_UPDATE, id);
			setupAutoUpdater();
		}

	}

	public void importPresetFromClipboard()
	{
		String clipboardText = PluginPresetsUtils.getClipboardText();
		if (clipboardText == null)
		{
			renderPanelErrorNotification("Unable read to clipboard content.");
			return;
		}

		PluginPreset newPreset = presetStorage.parsePluginPresetFrom(clipboardText);
		if (newPreset != null)
		{
			newPreset.setId(Instant.now().toEpochMilli());
			newPreset.setName(PluginPresetsUtils.createNameWithSuffixIfNeeded(newPreset.getName(), pluginPresets));
			newPreset.setLocal(true); // Presets are imported to /presets folder 

			pluginPresets.add(newPreset);
			savePresets();
			refreshPresets();
		}
		else
		{
			renderPanelErrorNotification("You do not have any valid presets in your clipboard.");
		}
	}

	public void exportPresetToClipboard(final PluginPreset preset)
	{
		final String json = gson.toJson(preset);
		final StringSelection contents = new StringSelection(json);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, null);
	}

	public void renderPanelErrorNotification(String message)
	{
		pluginPanel.renderNotification(message);
	}

	public void rebuildPluginUi()
	{
		pluginPanel.rebuild();
	}
}
