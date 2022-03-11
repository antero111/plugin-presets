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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.util.ImageUtil;
import org.pushingpixels.substance.internal.SubstanceSynapse;

public class ChangeInspectorDialog extends JDialog
{
	private final static int FRAME_WIDTH = 400;

	public ChangeInspectorDialog(PluginPresetsPlugin pluginPresetsPlugin, Window owner, String presetName, Map<String, ArrayList<String[]>> changes)
	{
		super(owner, "Plugin Presets - Pending changes for preset: " + presetName, ModalityType.MODELESS);

		setSize(FRAME_WIDTH, calculateFrameHeight(changes));
		setLayout(new BorderLayout());

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(10, 5, 5, 5));

		// To reduce substance's colorization (tinting)
		content.putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);
		JPanel changeView = new JPanel(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		changes.forEach((key, value) ->
		{
			changeView.add(new ChangeContainer(pluginPresetsPlugin, key, value), constraints);
			constraints.gridy++;

			changeView.add(Box.createRigidArea(new Dimension(0, 3)), constraints);
			constraints.gridy++;
		});

		JPanel bottomActions = new JPanel(new BorderLayout());
		JButton close = new JButton("Close");
		close.addActionListener(e -> setVisible(false));
		bottomActions.add(close, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane(changeView);

		content.add(scrollPane, BorderLayout.CENTER);
		content.add(bottomActions, BorderLayout.SOUTH);

		setContentPane(content);
	}

	private int calculateFrameHeight(Map<String, ArrayList<String[]>> changes)
	{
		int size = 35 + 25 + 20;

		for (Entry<String, ArrayList<String[]>> joo : changes.entrySet())
		{
			// Plugin name + changes + bottom border
			size += 22 + joo.getValue().size() * 22 + 5;
		}

		return Math.min(size, 500);

	}
}
