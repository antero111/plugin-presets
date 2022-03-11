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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;


class ConfigurationRow extends JPanel
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

	ConfigurationRow(PluginPresetsPlugin pluginPresetsPlugin, String pluginName, String[] configuration)
	{
		this.plugin = pluginPresetsPlugin;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(0, 5, 0, 8));

		JPanel confss = new JPanel();
		confss.setPreferredSize(new Dimension(100, 22));
		confss.setToolTipText(configuration[0] + " changed from " + configuration[1] + " to " + configuration[2]);
		JLabel conf2 = new JLabel(configuration[0] + ":");
		conf2.setFont(FontManager.getRunescapeSmallFont());
		conf2.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		conf2.setPreferredSize(new Dimension(120, 22));
		conf2.setToolTipText(pluginName + ": " + configuration[0]);
		add(conf2, BorderLayout.WEST);

		JLabel conf = new JLabel("<html><strike>" + addLinebreaks(configuration[1]) + "</strike><html>");
		conf.setFont(FontManager.getRunescapeSmallFont());
		conf.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		JLabel conasf = new JLabel("<html>" + addLinebreaks(configuration[2]) + "<html>");
		conasf.setForeground(ColorScheme.BRAND_ORANGE);
		conasf.setFont(FontManager.getRunescapeSmallFont());
		confss.add(conf);
		confss.add(conasf);
		confss.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		add(confss, BorderLayout.CENTER);

		JPanel sideActions = new JPanel(new BorderLayout(3, 0));
		sideActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);


		JLabel revertSetting = new JLabel(REVERT_ICON);
		revertSetting.setToolTipText(("revert setting change"));
		revertSetting.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				System.out.println("revert setting");
				// TODO: revert setting
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				revertSetting.setIcon(REVERT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				revertSetting.setIcon(REVERT_ICON);
			}
		});
		sideActions.add(revertSetting, BorderLayout.WEST);

		JLabel ignoreSetting = new JLabel(IGNORE_ICON);
		ignoreSetting.setToolTipText("ignore setting");
		ignoreSetting.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				System.out.println("ignre stting");
				// TODO: ignore setting
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				ignoreSetting.setIcon(IGNORE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				ignoreSetting.setIcon(IGNORE_ICON);
			}
		});
		sideActions.add(ignoreSetting, BorderLayout.EAST);

		add(sideActions, BorderLayout.EAST);

	}

	private String addLinebreaks(String input)
	{
		// Split long input fields to multiline html
		if (input.contains(","))
		{
			StringTokenizer tok = new StringTokenizer(input, ",");
			StringBuilder output = new StringBuilder(input.length());
			int lineLen = 0;
			while (tok.hasMoreTokens())
			{
				String word = tok.nextToken();

				if (lineLen + word.length() > 20)
				{
					output.append("<br>");
					lineLen = 0;
				}
				output.append(word).append(", ");
				lineLen += word.length();
			}
			return output.toString();
		}
		else
		{
			return input;
		}
	}

}
