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

import com.pluginpresets.InnerPluginConfig;
import com.pluginpresets.PluginConfig;
import com.pluginpresets.PluginPresetsPlugin;
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
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

public class ConfigPanel extends JPanel
{
	private static final ImageIcon SWITCH_ON_ICON;
	private static final ImageIcon SWITCH_ON_HOVER_ICON;
	private static final ImageIcon SWITCH_OFF_ICON;
	private static final ImageIcon SWITCH_OFF_HOVER_ICON;
	private static final ImageIcon UPDATE_ICON;
	private static final ImageIcon UPDATE_HOVER_ICON;
	private static final ImageIcon ARROW_DOWN_ICON;
	private static final ImageIcon NOTIFICATION_ICON;

	static
	{
		final BufferedImage notificationImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"warning_icon.png");
		NOTIFICATION_ICON = new ImageIcon(notificationImg);

		final BufferedImage switchOnImg2 = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_on_icon.png");
		final BufferedImage switchOnImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_checked.png");
		SWITCH_ON_ICON = new ImageIcon(switchOnImg);
		// SWITCH_ON_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(switchOnImg, 20));
		SWITCH_ON_HOVER_ICON = new ImageIcon(switchOnImg2);

		final BufferedImage arrowDownImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "arrow_up_icon.png");
		ARROW_DOWN_ICON = new ImageIcon(arrowDownImg);
		// SWITCH_ON_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(arrowDownImg, 20));

		final BufferedImage switchOffImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox.png");
		final BufferedImage switchOffHoverImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_off_hover_icon.png");
		SWITCH_OFF_ICON = new ImageIcon(switchOffImg);
		SWITCH_OFF_HOVER_ICON = new ImageIcon(switchOffHoverImg);

		final BufferedImage updateImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "refresh_icon.png");
		UPDATE_ICON = new ImageIcon(updateImg);
		UPDATE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(updateImg, -100));
	}

	private final PluginConfig presetConfig;
	private final PluginConfig currentConfig;
	private final PluginPresetsPlugin plugin;
	private final JCheckBox checkbox = new JCheckBox();
	private final JLabel updateLabel = new JLabel();
	private final boolean presetHasConfigurations;
	private final boolean external;
	private final boolean configsMatch;
	private final JPanel settings = new JPanel(new GridBagLayout());
	private final List<String> openSettings;
	private final boolean settingsVisible;

	public ConfigPanel(PluginConfig currentConfig, PluginConfig presetConfig, PluginPresetsPlugin plugin, List<String> openSettings)
	{
		this.presetConfig = presetConfig;
		this.currentConfig = currentConfig;
		this.plugin = plugin;
		this.openSettings = openSettings;

		presetHasConfigurations = presetHasConfigurations();
		configsMatch = configsMatch();
		external = isExternalPluginConfig();
		boolean installed = isExternalPluginInstalled();
		settingsVisible = isSettingsVisible();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 10, 0, 0));
		checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);

		JLabel title = new JLabel();
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
				toggleSettings();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				foreground = title.getForeground(); // Remember the original foreground color
				title.setForeground(ColorScheme.BRAND_ORANGE);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				title.setForeground(foreground);
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

		JLabel statusLabel = new JLabel();
		JLabel notInstalledLabel = new JLabel();
		if (presetHasConfigurations)
		{
			checkbox.setSelected(true);
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
						plugin.getPresetEditor().removeConfigurationFromEdited(presetConfig);
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
				updateLabel.setToolTipText("Replace the presets configuration for " + currentConfig.getName() + " with your current configuration.");
				updateLabel.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent mouseEvent)
					{
						plugin.getPresetEditor().removeConfigurationFromEdited(presetConfig);
						List<String> collect = presetConfig.getSettings().stream().map(InnerPluginConfig::getKey).collect(Collectors.toList());
						List<InnerPluginConfig> collect2 = currentConfig.getSettings().stream().filter(s -> collect.contains(s.getKey())).collect(Collectors.toList());
						currentConfig.setSettings((ArrayList<InnerPluginConfig>) collect2);

						if (presetConfig.getEnabled() == null)
						{
							currentConfig.setEnabled(null);
						}

						plugin.getPresetEditor().addConfigurationToEdited(currentConfig);
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
				notInstalledLabel.setIcon(NOTIFICATION_ICON);
				notInstalledLabel.setToolTipText("Plugin not installed, download from Plugin Hub if you want to use these settings.");
				title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			}
		}
		else
		{
			title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			title.setToolTipText("This preset does not include any configurations to " + currentConfig.getName() + " plugin.");

			checkbox.setSelected(false);
			checkbox.setToolTipText("Add your current configuration for " + currentConfig.getName() + " to the preset.");
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
						plugin.getPresetEditor().addConfigurationToEdited(currentConfig);
					}
				}
			});
		}

		JLabel downArrow = new JLabel();
		downArrow.setIcon(ARROW_DOWN_ICON);
		downArrow.setVisible(settingsVisible);
		checkbox.setVisible(!settingsVisible);

		JPanel leftActions = new JPanel();
		leftActions.setLayout(new BoxLayout(leftActions, BoxLayout.X_AXIS));
		leftActions.add(title);
		leftActions.add(externalNotice);
		leftActions.add(downArrow);

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		rightActions.add(statusLabel);
		rightActions.add(notInstalledLabel);
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
		ArrayList<InnerPluginConfig> presetSettings = (presetConfig != null) ? presetConfig.getSettings() : null;
		currentConfig.getSettings().forEach(currentSetting ->
		{
			InnerPluginConfig presetSetting = getPresetSettings(presetSettings, currentSetting);
			settings.add(new ConfigRow(currentConfig, currentSetting, presetSetting, plugin), constraints);
			constraints.gridy++;
		});
	}

	private JPanel getEnabledRow()
	{
		JPanel enabledRow = new JPanel(new BorderLayout());

		JLabel title = new JLabel();
		title.setText("Plugin on/off");
		// 0 width is to prevent the title causing the panel to grow in y direction on long setting descriptions
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 16));
		title.setFont(FontManager.getRunescapeSmallFont());

		JLabel checkBox = new JLabel();
		if (presetHasConfigurations && presetConfig.getEnabled() != null)
		{
			checkBox.setIcon(SWITCH_ON_ICON);
			checkBox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.getPresetEditor().removeEnabledFromEdited(currentConfig);
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
			checkBox.setIcon(SWITCH_OFF_ICON);
			checkBox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.getPresetEditor().addEnabledToEdited(currentConfig);
				}
			});
		}

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		rightActions.add(checkBox);

		enabledRow.add(title, BorderLayout.CENTER);
		enabledRow.add(rightActions, BorderLayout.EAST);

		return enabledRow;
	}

	private InnerPluginConfig getPresetSettings(List<InnerPluginConfig> presetSettings, final InnerPluginConfig currentSetting)
	{
		InnerPluginConfig presetSetting = null;
		if (presetSettings != null)
		{
			for (InnerPluginConfig setting : presetSettings)
			{
				if (setting.getName().equals(currentSetting.getName()))
				{
					presetSetting = setting;
					break;
				}
			}
		}
		return presetSetting;
	}

	private boolean isSettingsVisible()
	{
		return openSettings.contains(currentConfig.getConfigName());
	}

	private void toggleSettings()
	{
		if (settingsVisible)
		{
			openSettings.remove(currentConfig.getConfigName());
		}
		else
		{
			openSettings.add(currentConfig.getConfigName());
		}

		plugin.rebuildPluginUi();
	}

	private boolean presetHasConfigurations()
	{
		return presetConfig != null;
	}

	private boolean configsMatch()
	{
		if (!presetHasConfigurations)
		{
			return false;
		}

		if (presetConfig.getEnabled() != null && !presetConfig.getEnabled().equals(currentConfig.getEnabled()))
		{
			return false;
		}

		ArrayList<InnerPluginConfig> currentSettings = currentConfig.getSettings();
		// Compare plugin settings from preset to current config settings
		for (InnerPluginConfig presetConfigSetting : presetConfig.getSettings())
		{
			// Get current config setting for compared preset setting
			InnerPluginConfig currentConfigSetting = currentSettings.stream().filter(c -> c.getKey().equals(presetConfigSetting.getKey())).findFirst().orElse(null);

			if (currentConfigSetting != null &&
				presetConfigSetting.getValue() != null &&
				!presetConfigSetting.getValue().equals(currentConfigSetting.getValue()))
			{
				return false;
			}
		}

		return true;
	}

	private boolean isExternalPluginConfig()
	{
		return plugin.getPresetManager().isExternalPlugin(currentConfig.getName());
	}

	private boolean isExternalPluginInstalled()
	{
		return external && plugin.getPresetManager().isExternalPluginInstalled(currentConfig.getName());
	}

	private JPopupMenu getUpdateAllMenuPopup()
	{
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(2, 2, 2, 0));

		if (configsMatch)
		{
			JMenuItem removeOption = new JMenuItem();
			removeOption.setText("Remove configurations for " + currentConfig.getName() + " from all presets");
			removeOption.addActionListener(e -> plugin.getPresetEditor().removeConfigurationFromPresets(currentConfig));
			popupMenu.add(removeOption);
		}
		else
		{
			JMenuItem addOption = new JMenuItem();
			addOption.setText("Add configurations from " + currentConfig.getName() + " to all presets");
			addOption.addActionListener(e -> plugin.getPresetEditor().addConfigurationToPresets(currentConfig));
			popupMenu.add(addOption);
		}

		return popupMenu;
	}
}
