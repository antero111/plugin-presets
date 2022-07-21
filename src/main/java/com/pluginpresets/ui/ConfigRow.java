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

import com.pluginpresets.PluginSetting;
import com.pluginpresets.PluginConfig;
import com.pluginpresets.PluginPresetsPlugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

public class ConfigRow extends JPanel
{
	private static final ImageIcon CHECKBOX_CHECKED_ICON;
	private static final ImageIcon CHECKBOX_CHECKED_HOVER_ICON;
	private static final ImageIcon CHECKBOX_ICON;

	static
	{
		final BufferedImage checkboxCheckedImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_checked_icon.png");
		CHECKBOX_CHECKED_ICON = new ImageIcon(checkboxCheckedImg);
		CHECKBOX_CHECKED_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(checkboxCheckedImg, 20));

		final BufferedImage checkboxImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "checkbox_icon.png");
		CHECKBOX_ICON = new ImageIcon(checkboxImg);
	}

	private final PluginSetting currentSetting;
	private final PluginSetting presetSetting;
	private final JLabel checkboxLabel = new JLabel();
	private final boolean presetHasConfigurations;

	public ConfigRow(PluginConfig currentConfig, PluginSetting currentSetting, PluginSetting presetSetting, PluginPresetsPlugin plugin)
	{
		this.currentSetting = currentSetting;
		this.presetSetting = presetSetting;

		presetHasConfigurations = presetHasConfigurations();
		boolean configsMatch = configsMatch();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));

		JLabel title = new JLabel();
		title.setText(currentSetting.getName().length() == 0 ? currentSetting.getKey() : currentSetting.getName());
		title.setToolTipText(currentSetting.getName() + " - " + currentSetting.getKey());
		// 0 width is to prevent the title causing the panel to grow in y direction on long setting descriptions
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 16));
		title.setFont(FontManager.getRunescapeSmallFont());

		if (presetHasConfigurations)
		{
			checkboxLabel.setIcon(CHECKBOX_CHECKED_ICON);
			checkboxLabel.setToolTipText("Remove setting from the preset.");
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
		else
		{
			title.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
			checkboxLabel.setIcon(CHECKBOX_ICON);
			checkboxLabel.setToolTipText("Add setting to the preset.");
			checkboxLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.getPresetEditor().addSettingToEdited(currentConfig, currentSetting);

				}
			});
		}

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		JLabel updateLabel = new JLabel();
		rightActions.add(updateLabel);
		rightActions.add(checkboxLabel);

		add(title, BorderLayout.CENTER);
		add(rightActions, BorderLayout.EAST);
	}

	private boolean presetHasConfigurations()
	{
		return presetSetting != null;
	}

	private boolean configsMatch()
	{
		return presetHasConfigurations && presetSetting.equals(currentSetting);
	}
}
