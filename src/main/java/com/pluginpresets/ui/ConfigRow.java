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
import com.pluginpresets.PluginSetting;
import com.pluginpresets.Utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

public class ConfigRow extends JPanel
{
	private static final ImageIcon CHECKBOX_CHECKED_ICON;
	private static final ImageIcon CHECKBOX_CHECKED_HOVER_ICON;
	private static final ImageIcon CHECKBOX_ICON;
	private static final ImageIcon NOTIFICATION_ICON;
	private static final ImageIcon NOTIFICATION_HOVER_ICON;

	static
	{
		final BufferedImage checkboxCheckedImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_checked_icon.png");
		CHECKBOX_CHECKED_ICON = new ImageIcon(checkboxCheckedImg);
		CHECKBOX_CHECKED_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(checkboxCheckedImg, 20));

		final BufferedImage checkboxImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_icon.png");
		CHECKBOX_ICON = new ImageIcon(checkboxImg);

		final BufferedImage notificationImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"warning_icon.png");
		NOTIFICATION_ICON = new ImageIcon(notificationImg);
		NOTIFICATION_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(notificationImg, 20));
	}

	private final PluginPresetsPlugin plugin;
	private final PluginSetting currentSetting;
	private final PluginSetting presetSetting;
	private final JLabel checkboxLabel = new JLabel();
	private final boolean presetHasConfigurations;

	public ConfigRow(PluginConfig currentConfig, PluginSetting currentSetting, PluginSetting presetSetting, PluginPresetsPlugin plugin)
	{
		this.currentSetting = currentSetting;
		this.presetSetting = presetSetting;
		this.plugin = plugin;

		presetHasConfigurations = presetHasConfigurations();
		boolean configsMatch = configsMatch();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JLabel title = new JLabel();
		if (currentSetting == null)
		{
			title.setText(Utils.splitAndCapitalize(presetSetting.getKey()));
			title.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			title.setToolTipText(presetSetting.getKey());
		}
		else if (currentSetting.getName().length() == 0)
		{
			title.setText(Utils.splitAndCapitalize(currentSetting.getKey()));
			title.setToolTipText(currentSetting.getKey());
		}
		else
		{
			title.setText(currentSetting.getName());
			title.setToolTipText(currentSetting.getName() + " - " + currentSetting.getKey());
		}
		// 0 width is to prevent the title causing the panel to grow in y direction on long setting descriptions
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 16));
		title.setFont(FontManager.getRunescapeSmallFont());

		if (presetHasConfigurations)
		{
			checkboxLabel.setIcon(CHECKBOX_CHECKED_ICON);
			checkboxLabel.setToolTipText("Remove " + presetSetting.getName() + " from preset.");
			checkboxLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.getPresetEditor().removeSettingFromEdited(currentConfig, presetSetting);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					checkboxLabel.setIcon(CHECKBOX_CHECKED_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					checkboxLabel.setIcon(CHECKBOX_CHECKED_ICON);
				}
			});

			if (configsMatch)
			{
				title.setForeground(Color.LIGHT_GRAY);
			}
			else
			{
				title.setForeground(ColorScheme.BRAND_ORANGE);
			}
		}
		else if (currentSetting == null)
		{
			checkboxLabel.setIcon(NOTIFICATION_ICON);
			checkboxLabel.setToolTipText("Invalid plugin setting configuration (Click to remove)");
			checkboxLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
			checkboxLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.getPresetEditor().removeSettingFromEdited(null, presetSetting);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					checkboxLabel.setIcon(NOTIFICATION_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					checkboxLabel.setIcon(NOTIFICATION_ICON);
				}
			});
		}
		else
		{
			title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			checkboxLabel.setIcon(CHECKBOX_ICON);
			checkboxLabel.setToolTipText("Add " + currentSetting.getName() + " to preset.");
			checkboxLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.getPresetEditor().addSettingToEdited(currentConfig, currentSetting);

				}
			});
		}

		JLabel customSettingLabel = new JLabel();
		if ((currentSetting != null && currentSetting.getCustomConfigName() != null) || (presetSetting != null && presetSetting.getCustomConfigName() != null))
		{
			customSettingLabel.setText("(Custom) ");
			customSettingLabel.setToolTipText("User added custom setting");
			customSettingLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			title.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					if (mouseEvent.getButton() == MouseEvent.BUTTON3) // Right click
					{
						JPopupMenu popup = getMenuPopup();
						title.setComponentPopupMenu(popup);
					}
				}
			});
		}

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
		JLabel updateLabel = new JLabel();
		rightActions.add(updateLabel);
		rightActions.add(checkboxLabel);

		JPanel leftActions = new JPanel();
		leftActions.setLayout(new BorderLayout());
		leftActions.add(title, BorderLayout.CENTER);
		leftActions.add(customSettingLabel, BorderLayout.EAST);

		add(leftActions, BorderLayout.CENTER);
		add(rightActions, BorderLayout.EAST);
	}

	private boolean presetHasConfigurations()
	{
		return currentSetting != null && presetSetting != null;
	}

	private boolean configsMatch()
	{
		return presetHasConfigurations && presetSetting.equals(currentSetting);
	}

	private JPopupMenu getMenuPopup()
	{
		JMenuItem removeOption = new JMenuItem();
		removeOption.setText("Remove custom setting");
		removeOption.addActionListener(e -> removeSetting());

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(2, 2, 2, 0));
		popupMenu.add(removeOption);
		return popupMenu;
	}

	private void removeSetting()
	{
		plugin.getPresetEditor().removeCustomSetting(currentSetting);
		plugin.getPresetManager().removeCustomSetting(currentSetting);

	}
}
