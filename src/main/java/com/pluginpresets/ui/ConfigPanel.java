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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class ConfigPanel extends JPanel
{
	private static final ImageIcon SWITCH_ON_ICON;
	private static final ImageIcon SWITCH_ON_HOVER_ICON;
	private static final ImageIcon SWITCH_OFF_ICON;
	private static final ImageIcon SWITCH_OFF_HOVER_ICON;
	private static final ImageIcon UPDATE_ICON;
	private static final ImageIcon UPDATE_HOVER_ICON;

	static
	{
		final BufferedImage switchOnImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_on_icon.png");
		SWITCH_ON_ICON = new ImageIcon(switchOnImg);
		SWITCH_ON_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(switchOnImg, 20));

		final BufferedImage switchOffImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_off_icon.png");
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
	private final JLabel switchLabel = new JLabel();
	private final JLabel updateLabel = new JLabel();
	private final boolean presetHasConfigurations;
	private final boolean external;

	public ConfigPanel(PluginConfig currentConfig, PluginConfig presetConfig, PluginPresetsPlugin plugin)
	{
		this.presetConfig = presetConfig;
		this.currentConfig = currentConfig;
		this.plugin = plugin;

		presetHasConfigurations = presetHasConfigurations();
		boolean configsMatch = configsMatch();
		external = isExternalPluginConfig();
		boolean installed = isExternalPluginInstalled();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(3, 10, 3, 0));

		JLabel title = new JLabel();
		title.setText(currentConfig.getName());
		// 0 width is to prevent the title causing the panel to grow in y direction on long plugin names
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 16));

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
			switchLabel.setIcon(SWITCH_ON_ICON);
			switchLabel.setToolTipText("Remove configurations for " + currentConfig.getName() + " from the preset.");
			switchLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.removeConfiguration(presetConfig);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_ON_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_ON_ICON);
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
						plugin.removeConfiguration(presetConfig);
						plugin.addConfiguration(currentConfig);
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
				notInstalledLabel.setIcon(UPDATE_ICON); // TODO: notInstalledLabel add label icon
				notInstalledLabel.setToolTipText("Not installed, download from Plugin Hub if you want to use these settings.");
				title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			}
		}
		else
		{
			title.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			title.setToolTipText("This preset does not include any configurations to " + currentConfig.getName() + " plugin.");

			switchLabel.setIcon(SWITCH_OFF_ICON);
			switchLabel.setToolTipText("Add your current configuration for " + currentConfig.getName() + " to the preset.");
			switchLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.addConfiguration(currentConfig);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_OFF_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_OFF_ICON);
				}
			});
		}

		switchLabel.setPreferredSize(new Dimension(20, 16));

		JPanel leftActions = new JPanel();
		leftActions.setLayout(new BoxLayout(leftActions, BoxLayout.X_AXIS));
		leftActions.add(title);
		leftActions.add(externalNotice);

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		rightActions.add(statusLabel);
		rightActions.add(notInstalledLabel);
		rightActions.add(updateLabel);
		rightActions.add(switchLabel);

		JPanel topActions = new JPanel(new BorderLayout());
		topActions.add(leftActions, BorderLayout.CENTER);
		topActions.add(rightActions, BorderLayout.EAST);

		JPanel settings = new JPanel(new GridBagLayout());
		settings.setVisible(false);
		settings.setBorder(new EmptyBorder(0, 5, 0, 0));

		add(topActions, BorderLayout.NORTH);
		add(settings, BorderLayout.CENTER);
	}

	// private void toggleSettings()
	// {
	// 	boolean visible = settings.isVisible();
	// 	if (!visible)
	// 	{
	// 		GridBagConstraints constraints = new GridBagConstraints();
	// 		constraints.fill = GridBagConstraints.HORIZONTAL;
	// 		constraints.weightx = 1;
	// 		constraints.gridx = 0;
	// 		constraints.gridy = 0;

	// 		settings.removeAll();

	// 		settings.add(new JLabel("Plugin " + (currentConfig.getEnabled() ? "enabled" : "disabled")), constraints);
	// 		constraints.gridy++;

	// 		currentConfig.getSettings().forEach(setting -> {
	// 			settings.add(new JLabel(setting.getName()), constraints);
	// 			constraints.gridy++;
	// 		});
	// 	}

	// 	settings.setVisible(!visible);

	// 	revalidate();
	// 	repaint();
	// }

	private boolean presetHasConfigurations()
	{
		return presetConfig != null;
	}

	private boolean configsMatch()
	{
		return presetHasConfigurations && presetConfig.equals(currentConfig);
	}

	private boolean isExternalPluginConfig()
	{
		return plugin.getPresetManager().isExternalPlugin(currentConfig.getName());
	}

	private boolean isExternalPluginInstalled()
	{
		return external && plugin.getPresetManager().isExternalPluginInstalled(currentConfig.getName());
	}
}
