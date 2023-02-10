/*
 * Copyright (c) 2023, antero111 <https://github.com/antero111>
 * Original code that has been since modified here Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.LinkBrowser;

public class NoPresetsPanel extends JPanel
{
	private final JLabel wikiLink = new JLabel();

	public NoPresetsPanel()
	{
		setOpaque(false);
		setBorder(new EmptyBorder(50, 10, 0, 10));
		setLayout(new BorderLayout());

		JLabel title = new JShadowedLabel();
		title.setForeground(Color.WHITE);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setText("Plugin Presets");

		JLabel description = new JLabel();
		description.setFont(FontManager.getRunescapeSmallFont());
		description.setBorder(new EmptyBorder(5, 0, 0, 0));

		description.setForeground(Color.GRAY);
		description.setHorizontalAlignment(SwingConstants.CENTER);
		description.setText("<html><body style = 'text-align:center'>Presets of your plugin configurations.</body></html>");

		wikiLink.setFont(FontManager.getRunescapeSmallFont());
		wikiLink.setBorder(new EmptyBorder(10, 0, 0, 0));
		wikiLink.setForeground(Color.GRAY);
		wikiLink.setHorizontalAlignment(SwingConstants.CENTER);
		wikiLink.setIcon(Icons.HELP_ICON);
		wikiLink.setText("<html><body style = 'text-align:center'>See wiki for usage guide.</body></html>");
		wikiLink.setToolTipText("Open Plugin Presets wiki: " + PluginPresetsPlugin.HELP_LINK);
		wikiLink.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				LinkBrowser.browse(PluginPresetsPlugin.HELP_LINK);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				wikiLink.setIcon(Icons.HELP_HOVER_ICON);
				wikiLink.setForeground(Color.GRAY.darker());

			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				wikiLink.setIcon(Icons.HELP_ICON);
				wikiLink.setForeground(Color.GRAY);

			}
		});

		add(title, BorderLayout.NORTH);
		add(description, BorderLayout.CENTER);
		add(wikiLink, BorderLayout.SOUTH);
	}
}
