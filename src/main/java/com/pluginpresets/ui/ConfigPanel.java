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
package com.pluginpresets.ui;

import com.pluginpresets.PluginConfig;
import com.pluginpresets.PluginPresetsPlugin;
import com.pluginpresets.PluginPresetsPresetEditor;
import com.pluginpresets.PluginPresetsPresetManager;
import com.pluginpresets.PluginSetting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

public class ConfigPanel extends JPanel
{
	private static final ImageIcon CHECKBOX_ICON;
	private static final ImageIcon CHECKBOX_CHECKED_ICON;
	private static final ImageIcon CHECKBOX_CHECKED_HOVER_ICON;
	private static final ImageIcon UPDATE_ICON;
	private static final ImageIcon UPDATE_HOVER_ICON;
	private static final ImageIcon ARROW_DOWN_ICON;
	private static final ImageIcon ARROW_RIGHT_ICON;
	private static final ImageIcon ARROW_RIGHT_HOVER_ICON;
	private static final ImageIcon NOTIFICATION_ICON;
	private static final ImageIcon NOT_INSTALLED_ICON;

	static
	{
		final BufferedImage notificationImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"warning_icon.png");
		NOTIFICATION_ICON = new ImageIcon(notificationImg);

		final BufferedImage notInstalledImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"not_installed_icon.png");
		NOT_INSTALLED_ICON = new ImageIcon(notInstalledImg);

		final BufferedImage checkboxImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_icon.png");
		CHECKBOX_ICON = new ImageIcon(checkboxImg);

		final BufferedImage checkboxCheckedImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_checked_icon.png");
		CHECKBOX_CHECKED_ICON = new ImageIcon(checkboxCheckedImg);
		CHECKBOX_CHECKED_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(checkboxCheckedImg, 20));

		final BufferedImage arrowDownImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "arrow_right_icon.png");
		ARROW_DOWN_ICON = new ImageIcon(ImageUtil.rotateImage(arrowDownImg, (Math.PI / 2)));
		ARROW_RIGHT_ICON = new ImageIcon(ImageUtil.alphaOffset(arrowDownImg, 0.45f));
		ARROW_RIGHT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(arrowDownImg, 0.80f));

		final BufferedImage updateImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "refresh_icon.png");
		UPDATE_ICON = new ImageIcon(updateImg);
		UPDATE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(updateImg, -100));
	}

	private final PluginConfig presetConfig;
	private final PluginConfig currentConfig;
	private final PluginPresetsPlugin plugin;
	private final JCheckBox checkbox = new JCheckBox();
	private final JLabel updateLabel = new JLabel();
	private final JLabel notificationLabel = new JLabel();
	private final boolean presetHasConfigurations;
	private final boolean external;
	private final boolean configsMatch;
	private final JPanel settings = new JPanel(new GridBagLayout());
	private final List<String> openSettings;
	private final boolean settingsVisible;
	private final PluginPresetsPresetManager presetManager;
	private final PluginPresetsPresetEditor presetEditor;

	public ConfigPanel(PluginConfig currentConfig, PluginConfig presetConfig, PluginPresetsPlugin plugin, List<String> openSettings)
	{
		this.presetConfig = presetConfig;
		this.currentConfig = currentConfig;
		this.plugin = plugin;
		this.openSettings = openSettings;

		presetManager = plugin.getPresetManager();
		presetEditor = plugin.getPresetEditor();

		presetHasConfigurations = presetHasConfigurations();
		configsMatch = currentConfig.match(presetConfig);
		external = isExternalPluginConfig();
		boolean installed = isExternalPluginInstalled();
		settingsVisible = isSettingsVisible();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 3, 0, 0));
		checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);

		JLabel title = new JLabel();
		JLabel downArrow = new JLabel();
		title.setText(currentConfig.getName());
		// 0 width is to prevent the title causing the panel to grow in y direction on long plugin names
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 26));
		title.addMouseListener(new MouseAdapter()
		{
			private Color foreground;

			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseEvent.BUTTON3) // Right click
				{
					JPopupMenu importPopupMenu = getImportMenuPopup();
					title.setComponentPopupMenu(importPopupMenu);
				}
				else
				{
					toggleSettings();
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				foreground = title.getForeground(); // Remember the original foreground color
				title.setForeground(ColorScheme.BRAND_ORANGE);
				if (!settingsVisible)
				{
					downArrow.setIcon(ARROW_RIGHT_HOVER_ICON);
				}
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				title.setForeground(foreground);
				if (!settingsVisible)
				{
					downArrow.setIcon(ARROW_RIGHT_ICON);
				}
			}
		});

		JLabel externalNotice = new JLabel();
		if (external)
		{
			externalNotice.setText("(E)");
			externalNotice.setToolTipText("Plugin from Plugin Hub");
			externalNotice.setBorder(new EmptyBorder(0, 3, 0, 0));
			externalNotice.setForeground(ColorScheme.BRAND_ORANGE);
		}

		JLabel customNotice = new JLabel();
		if (currentConfig.containsCustomSettings())
		{
			customNotice.setText("(C)");
			customNotice.setToolTipText("Contains custom settings");
			customNotice.setBorder(new EmptyBorder(0, 3, 0, 0));
			customNotice.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		}

		JLabel statusLabel = new JLabel();
		JLabel notInstalledLabel = new JLabel();
		if (presetHasConfigurations)
		{
			checkbox.setSelected(true);
			checkbox.setToolTipText("Remove " + currentConfig.getName() + " configurations from the preset.");
			checkbox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					if (mouseEvent.getButton() == MouseEvent.BUTTON3) // Right click
					{
						JPopupMenu updateAllPopupMenu = getUpdateAllMenuPopup();
						checkbox.setComponentPopupMenu(updateAllPopupMenu);
					}
					else
					{
						presetEditor.removeConfigurationFromEdited(presetConfig);
					}
				}
			});

			if (!configsMatch)
			{
				title.setToolTipText("Your configurations for " + currentConfig.getName() + " do not match the preset.");

				statusLabel.setText("Modified");
				statusLabel.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
				statusLabel.setToolTipText("Your configurations for " + currentConfig.getName() + " do not match the preset.");

				updateLabel.setIcon(UPDATE_ICON);
				updateLabel.setToolTipText("Replace presets configuration for " + currentConfig.getName() + " with your current configuration.");
				updateLabel.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent mouseEvent)
					{
						presetEditor.updateConfigurations(presetConfig, currentConfig);
					}

					@Override
					public void mouseEntered(MouseEvent mouseEvent)
					{
						updateLabel.setIcon(UPDATE_HOVER_ICON);
					}

					@Override
					public void mouseExited(MouseEvent mouseEvent)
					{
						updateLabel.setIcon(UPDATE_ICON);
					}
				});
			}
			else
			{
				title.setToolTipText("Your configurations for " + currentConfig.getName() + " match the preset.");
				title.setForeground(Color.WHITE);
			}

			if (external && !installed)
			{
				notInstalledLabel.setIcon(NOT_INSTALLED_ICON);
				notInstalledLabel.setToolTipText("Plugin not installed, download from Plugin Hub if you want to use these settings.");
				title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			}
		}
		else
		{
			title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			title.setToolTipText("This preset does not include any configurations to " + currentConfig.getName() + " plugin.");

			checkbox.setSelected(false);
			checkbox.setToolTipText("Add your current " + currentConfig.getName() + " configurations to the preset.");
			checkbox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					if (mouseEvent.getButton() == MouseEvent.BUTTON3) // Right click
					{
						JPopupMenu updateAllPopupMenu = getUpdateAllMenuPopup();
						checkbox.setComponentPopupMenu(updateAllPopupMenu);
					}
					else
					{
						presetEditor.addConfigurationToEdited(currentConfig);
					}
				}
			});
		}

		notificationLabel.setIcon(NOTIFICATION_ICON);
		notificationLabel.setVisible(false);

		downArrow.setIcon(settingsVisible ? ARROW_DOWN_ICON : ARROW_RIGHT_ICON);
		downArrow.addMouseListener(new MouseAdapter()
		{
			private Color foreground;

			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				toggleSettings();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				foreground = title.getForeground(); // Remember the original foreground color
				title.setForeground(ColorScheme.BRAND_ORANGE);
				if (!settingsVisible)
				{
					downArrow.setIcon(ARROW_RIGHT_HOVER_ICON);
				}
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				title.setForeground(foreground);
				if (!settingsVisible)
				{
					downArrow.setIcon(ARROW_RIGHT_ICON);
				}
			}
		});

		checkbox.setVisible(!settingsVisible);

		JPanel leftActions = new JPanel();
		leftActions.setLayout(new BoxLayout(leftActions, BoxLayout.X_AXIS));
		leftActions.add(downArrow);
		leftActions.add(title);
		leftActions.add(externalNotice);
		leftActions.add(customNotice);

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		rightActions.add(statusLabel);
		rightActions.add(notInstalledLabel);
		rightActions.add(notificationLabel);
		rightActions.add(updateLabel);
		rightActions.add(checkbox);

		JPanel topActions = new JPanel(new BorderLayout());
		topActions.add(leftActions, BorderLayout.CENTER);
		topActions.add(rightActions, BorderLayout.EAST);

		createSettings();
		settings.setBorder(new EmptyBorder(0, 5, 3, 0));
		settings.setVisible(settingsVisible);

		add(topActions, BorderLayout.NORTH);
		add(settings, BorderLayout.CENTER);
	}

	private void createSettings()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		settings.removeAll();

		settings.add(getEnabledRow(), constraints);
		constraints.gridy++;

		// Some plugins don't have settings like Ammo, Account, Emojis etc.
		List<PluginSetting> presetSettings = (presetConfig != null) ? presetConfig.getSettings() : null;

		ArrayList<String> loopedInvalidConfigurations = new ArrayList<>();

		currentConfig.getSettings().forEach(currentSetting ->
		{
			PluginSetting presetSetting;
			if (presetHasConfigurations)
			{
				assert presetConfig != null;
				presetSetting = presetConfig.getSetting(currentSetting);
			}
			else
			{
				presetSetting = null;
			}
			String configName = currentConfig.getConfigName();
			List<String> keys = currentConfig.getSettingKeys();

			if (presetSettings != null && !loopedInvalidConfigurations.contains(configName))
			{
				presetSettings.forEach(setting ->
				{
					if (!keys.contains(setting.getKey()))
					{
						settings.add(new ConfigRow(null, null, setting, plugin), constraints);
						constraints.gridy++;

						if (!settingsVisible)
						{
							notificationLabel.setVisible(true);
							notificationLabel.setToolTipText("Preset contains invalid configurations for this plugin");
						}
					}
				});

				loopedInvalidConfigurations.add(configName);
			}
			settings.add(new ConfigRow(currentConfig, currentSetting, presetSetting, plugin), constraints);
			constraints.gridy++;
		});
	}

	private JPanel getEnabledRow()
	{
		JPanel enabledRow = new JPanel(new BorderLayout());

		JLabel title = new JLabel();
		title.setText("Plugin on/off");
		title.setToolTipText("Whether the plugin is disabled or not.");
		// 0 width is to prevent the title causing the panel to grow in y direction on long setting descriptions
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 16));
		title.setFont(FontManager.getRunescapeSmallFont());

		JLabel checkBox = new JLabel();
		if (presetHasConfigurations && presetConfig.getEnabled() != null)
		{
			checkBox.setIcon(CHECKBOX_CHECKED_ICON);
			checkBox.setToolTipText("Remove plugin on/off configuration from the preset.");
			checkBox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					presetEditor.removeEnabledFromEdited(currentConfig);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					checkBox.setIcon(CHECKBOX_CHECKED_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					checkBox.setIcon(CHECKBOX_CHECKED_ICON);
				}
			});

			if (currentConfig.getEnabled().equals(presetConfig.getEnabled()))
			{
				title.setForeground(Color.LIGHT_GRAY);
			}
			else
			{
				title.setForeground(ColorScheme.BRAND_ORANGE);
			}

		}
		else
		{
			title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			checkBox.setIcon(CHECKBOX_ICON);
			checkBox.setToolTipText("Add plugin on/off configuration to the preset.");
			checkBox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					presetEditor.addEnabledToEdited(currentConfig);
				}
			});
		}

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
		rightActions.add(checkBox);

		enabledRow.add(title, BorderLayout.CENTER);
		enabledRow.add(rightActions, BorderLayout.EAST);

		return enabledRow;
	}

	private boolean isSettingsVisible()
	{
		return openSettings.contains(currentConfig.getName());
	}

	private JPopupMenu getImportMenuPopup()
	{
		JMenuItem importOption = new JMenuItem();
		importOption.setText("Add custom setting");
		importOption.addActionListener(e -> promptPresetCreation());

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(2, 2, 2, 0));
		popupMenu.add(importOption);
		return popupMenu;
	}

	private void promptPresetCreation()
	{
		String customPresetName = JOptionPane.showInputDialog(ConfigPanel.this,
			"Format: configName.settingKey", "Add custom setting to " + currentConfig.getName(), JOptionPane.PLAIN_MESSAGE);

		if (customPresetName != null && customPresetName.length() > 0)
		{
			presetEditor.addCustomSettingToEdited(currentConfig, customPresetName);
		}
	}

	private void toggleSettings()
	{
		String name = currentConfig.getName();
		if (settingsVisible)
		{
			openSettings.remove(name);
		}
		else
		{
			openSettings.add(name);
		}

		plugin.rebuildPluginUi();
	}

	private boolean presetHasConfigurations()
	{
		return presetConfig != null;
	}

	private boolean isExternalPluginConfig()
	{
		return presetManager.isExternalPlugin(currentConfig.getName());
	}

	private boolean isExternalPluginInstalled()
	{
		return external && presetManager.isExternalPluginInstalled(currentConfig.getName());
	}

	private JPopupMenu getUpdateAllMenuPopup()
	{
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(2, 2, 2, 0));

		if (configsMatch)
		{
			JMenuItem removeOption = new JMenuItem();
			removeOption.setText("Remove " + currentConfig.getName() + " from all presets");
			removeOption.addActionListener(e -> presetEditor.removeConfigurationFromPresets(currentConfig));
			popupMenu.add(removeOption);
		}
		else
		{
			JMenuItem addOption = new JMenuItem();
			addOption.setText("Add " + currentConfig.getName() + " to all presets");
			addOption.addActionListener(e -> presetEditor.addConfigurationToPresets(currentConfig));
			popupMenu.add(addOption);
		}

		return popupMenu;
	}
}
