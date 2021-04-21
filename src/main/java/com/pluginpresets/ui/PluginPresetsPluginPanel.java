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

import com.pluginpresets.PluginPreset;
import com.pluginpresets.PluginPresetsPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

public class PluginPresetsPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private final JLabel addPreset = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final PluginErrorPanel noPresetsPanel = new PluginErrorPanel();
	private final JPanel presetView = new JPanel(new GridBagLayout());

	private final PluginPresetsPlugin plugin;

	static
	{
		final BufferedImage addIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
	}

	public PluginPresetsPluginPanel(PluginPresetsPlugin pluginPresetsPlugin)
	{
		this.plugin = pluginPresetsPlugin;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		title.setText("Plugin Presets");
		title.setForeground(Color.WHITE);

		northPanel.add(title, BorderLayout.WEST);
		northPanel.add(addPreset, BorderLayout.EAST);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		presetView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		noPresetsPanel.setContent("Plugin Presets", "Presets of your plugin configurations.");
		noPresetsPanel.setVisible(false);

		presetView.add(noPresetsPanel, constraints);
		constraints.gridy++;

		addPreset.setToolTipText("Create new plugin preset");
		addPreset.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				String customPresetName = JOptionPane.showInputDialog(PluginPresetsPluginPanel.this, "Give your new preset a name.", "New preset", JOptionPane.QUESTION_MESSAGE);
				if (customPresetName != null)
				{
					plugin.createPreset(customPresetName);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addPreset.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addPreset.setIcon(ADD_ICON);
			}
		});

		centerPanel.add(presetView, BorderLayout.CENTER);

		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
	}

	public void rebuild()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		presetView.removeAll();

		for (final PluginPreset preset : plugin.getPluginPresets())
		{
			presetView.add(new PluginPresetsPanel(preset, plugin), constraints);
			constraints.gridy++;

			presetView.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
			constraints.gridy++;
		}

		boolean empty = constraints.gridy == 0;
		noPresetsPanel.setVisible(empty);
		title.setVisible(!empty);

		presetView.add(noPresetsPanel, constraints);
		constraints.gridy++;

		repaint();
		revalidate();
	}
}
