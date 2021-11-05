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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class ChangeInspector extends JDialog
{


	ChangeInspector(Component parent, Map<String, ArrayList<String[]>> changes)
	{
		setTitle("Configurations changes to current preset");
		setModal(true);

		setLayout(new BorderLayout());
		setSize(600, 300);
		setLocationRelativeTo(parent);

		JPanel changeView = new JPanel(new GridBagLayout());
		changeView.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel topStuff = new JPanel();
		JLabel topText = new JLabel("Changes");
		topStuff.add(topText);
		topStuff.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel changesPanel = new JPanel();
		changesPanel.setLayout(new GridLayout(0, 1));

		changes.forEach((key, value) -> {
			JPanel pluginBox = new JPanel();
			pluginBox.setLayout(new BorderLayout());
			Border border = BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR, 1);
			pluginBox.setBorder(border);

			JLabel pluginNameBox = new JLabel(key);
			pluginNameBox.setHorizontalAlignment(SwingConstants.LEFT);

			JPanel grid = new JPanel();
			grid.setLayout(new GridLayout(0, 3));

			for (String[] configuration : value)
			{
				for (String string : configuration)
				{
					JPanel confi = new JPanel();
					confi.setBorder(border);
					confi.add(new JLabel(string));
					grid.add(confi);
				}
			}

			pluginBox.add(pluginNameBox, BorderLayout.NORTH);
			pluginBox.add(grid, BorderLayout.CENTER);

			changesPanel.add(pluginBox);
		});

		changeView.add(topStuff);
		changeView.add(changesPanel);
		add(changeView);

		JButton b = new JButton("OK");
		b.addActionListener(e -> setVisible(false));
		add(b, BorderLayout.SOUTH);

		setVisible(true);
	}
}
