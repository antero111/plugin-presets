/*
 * Copyright (c) 2021, antero111 <https://github.com/antero111>
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

import com.pluginpresets.PluginPresetsPlugin;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

class ChangeContainer extends JPanel
{

	private final PluginPresetsPlugin plugin;
	private static final ImageIcon REVERT_ICON;
	private static final ImageIcon REVERT_HOVER_ICON;
	private static final ImageIcon IGNORE_ICON;
	private static final ImageIcon IGNORE_HOVER_ICON;

	static
	{
		final BufferedImage revertIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "revert_icon.png");
		REVERT_ICON = new ImageIcon(revertIcon);
		REVERT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(revertIcon, 0.53f));

		final BufferedImage ignoreIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "ignore_icon.png");
		IGNORE_ICON = new ImageIcon(ignoreIcon);
		IGNORE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(ignoreIcon, 0.53f));
	}

	ChangeContainer(PluginPresetsPlugin pluginPresetsPlugin, String pluginName, ArrayList<String[]> value)
	{
		this.plugin = pluginPresetsPlugin;

		setLayout(new BorderLayout());

		JPanel topContainer = new JPanel(new BorderLayout());
		topContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		topContainer.setBorder(new EmptyBorder(0, 5, 0, 8));

		JLabel name = new JLabel(pluginName);
		name.setFont(FontManager.getRunescapeSmallFont());
		name.setBackground(ColorScheme.BRAND_ORANGE);
		name.setForeground(Color.WHITE);
		name.setToolTipText("Unsaved configuration changes for " + pluginName + "plugin");

		JPanel topActions = new JPanel(new BorderLayout(6, 0));
		topActions.setBorder(new EmptyBorder(4, 1, 3, 0));
		topActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel revertChange = new JLabel(REVERT_ICON);
		revertChange.setToolTipText("Revert all settings in this plugin");
		revertChange.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				// plugin.ign
				// TODO: tee revert setting
				System.out.println(pluginName);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				revertChange.setIcon(REVERT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				revertChange.setIcon(REVERT_ICON);
			}
		});

		JLabel ignoreChange = new JLabel(IGNORE_ICON);
		ignoreChange.setToolTipText("Add plugin to ignores");
		ignoreChange.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.ignorePlugin(pluginName);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				ignoreChange.setIcon(IGNORE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				ignoreChange.setIcon(IGNORE_ICON);
			}
		});

		topActions.add(revertChange, BorderLayout.CENTER);
		topActions.add(ignoreChange, BorderLayout.EAST);

		topContainer.add(topActions, BorderLayout.EAST);
		topContainer.add(name, BorderLayout.WEST);

		JPanel bottomContainer = new JPanel(new BorderLayout());

		JPanel changesContainer = new JPanel();
		changesContainer.setLayout(new GridBagLayout());

		GridBagConstraints changesContainerc = new GridBagConstraints();
		changesContainerc.fill = GridBagConstraints.HORIZONTAL;
		changesContainerc.weightx = 1;
		changesContainerc.gridx = 0;
		changesContainerc.gridy = 0;

		changesContainer.add(Box.createRigidArea(new Dimension(0, 1)), changesContainerc);
		changesContainerc.gridy++;

		for (String[] configuration : value)
		{
			changesContainer.add(new ConfigurationRow(plugin, pluginName, configuration), changesContainerc);
			changesContainerc.gridy++;
		}

		bottomContainer.add(changesContainer, BorderLayout.CENTER);

		add(topContainer, BorderLayout.NORTH);
		add(bottomContainer, BorderLayout.SOUTH);
	}
}
