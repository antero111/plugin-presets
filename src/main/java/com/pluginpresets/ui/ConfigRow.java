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
import com.pluginpresets.PluginPresetsUtils;
import com.pluginpresets.PluginSetting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * Row for editing a single config of a plugin in the current preset
 */
public class ConfigRow extends JPanel
{
	private final PluginSetting currentSetting;
	private final PluginSetting presetSetting;
	private final JLabel customLabel = new JLabel();
	private JCheckBox checkBox = new JCheckBox();
	private final boolean presetHasConfigurations;
	private final PluginPresetsPresetEditor presetEditor;

	public ConfigRow(PluginConfig currentConfig, PluginSetting currentSetting, PluginSetting presetSetting, PluginPresetsPlugin plugin)
	{
		this.currentSetting = currentSetting;
		this.presetSetting = presetSetting;

		presetEditor = plugin.getPresetEditor();

		presetHasConfigurations = presetHasConfigurations();
		boolean configsMatch = configsMatch();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JLabel title = new JLabel();
		if (currentSetting == null)
		{
			title.setText(PluginPresetsUtils.splitAndCapitalize(presetSetting.getKey()));
			title.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			title.setToolTipText(presetSetting.getKey());
		}
		else if (currentSetting.getName().length() == 0)
		{
			title.setText(PluginPresetsUtils.splitAndCapitalize(currentSetting.getKey()));
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
			String customConfigName = presetSetting.getCustomConfigName();
			if (customConfigName != null)
			{
				checkBox = null; // Hide checkbox
				customLabel.setText("remove");
				customLabel.setPreferredSize(new Dimension(38, 16));
				customLabel.setFont(FontManager.getRunescapeSmallFont());
				customLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				customLabel.setToolTipText("Remove custom setting '" + presetSetting.getName() + "' from preset.");
				customLabel.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent mouseEvent)
					{
						int confirm = JOptionPane.showConfirmDialog(customLabel,
							"Are you sure to remove custom setting '" + presetSetting.getName() + "'?",
							"Remove custom setting", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

						if (confirm == 0)
						{
							presetEditor.removeSettingFromEdited(currentConfig, presetSetting);
						}
					}

					@Override
					public void mouseEntered(MouseEvent mouseEvent)
					{
						customLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);

					}

					@Override
					public void mouseExited(MouseEvent mouseEvent)
					{
						customLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
					}
				});
			}
			else
			{
				checkBox.setSelected(true);
				checkBox.setToolTipText("Remove '" + presetSetting.getName() + "' from preset.");
				checkBox.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent mouseEvent)
					{
						presetEditor.removeSettingFromEdited(currentConfig, presetSetting);
					}
				});
			}

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
			checkBox = null; // Hide checkbox
			customLabel.setIcon(Icons.NOTIFICATION_ICON);
			customLabel.setToolTipText("Invalid plugin setting configuration (Click to remove)");
			customLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
			customLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					presetEditor.removeSettingFromEdited(null, presetSetting);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					customLabel.setIcon(Icons.NOTIFICATION_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					customLabel.setIcon(Icons.NOTIFICATION_ICON);
				}
			});
		}
		else
		{
			title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			checkBox.setSelected(false);
			checkBox.setToolTipText("Add '" + currentSetting.getName() + "' to preset.");
			checkBox.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					presetEditor.addSettingToEdited(currentConfig, currentSetting);
				}
			});
		}

		JLabel customSettingLabel = new JLabel();
		boolean isCustomSetting = (currentSetting != null && currentSetting.getCustomConfigName() != null) || (presetSetting != null && presetSetting.getCustomConfigName() != null);
		if (isCustomSetting)
		{
			customSettingLabel.setText("(Custom)");
			customSettingLabel.setFont(FontManager.getRunescapeSmallFont());
			customSettingLabel.setToolTipText("User added custom setting");
			customSettingLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		}

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
		JLabel updateLabel = new JLabel();
		rightActions.add(updateLabel);
		rightActions.add(checkBox == null ? customLabel : checkBox);

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
}
