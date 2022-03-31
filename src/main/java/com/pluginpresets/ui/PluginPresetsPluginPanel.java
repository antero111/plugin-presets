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

import com.pluginpresets.PluginConfig;
import com.pluginpresets.PluginPreset;
import com.pluginpresets.PluginPresetsPlugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

public class PluginPresetsPluginPanel extends PluginPanel
{
	private static final ImageIcon NOTIFICATION_ICON;
	private static final ImageIcon NOTIFICATION_HOVER_ICON;
	private static final ImageIcon HELP_ICON;
	private static final ImageIcon HELP_HOVER_ICON;
	private static final ImageIcon REFRESH_ICON;
	private static final ImageIcon REFRESH_HOVER_ICON;
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;
	private static final ImageIcon ARROW_LEFT_ICON;
	private static final ImageIcon ARROW_LEFT_HOVER_ICON;

	static
	{
		final BufferedImage notificationIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"warning_icon.png");
		NOTIFICATION_ICON = new ImageIcon(notificationIcon);
		NOTIFICATION_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(notificationIcon, 0.53f));

		final BufferedImage helpIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "help_icon.png");
		HELP_ICON = new ImageIcon(helpIcon);
		HELP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(helpIcon, 0.53f));

		final BufferedImage refreshIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "refresh_icon.png");
		REFRESH_ICON = new ImageIcon(refreshIcon);
		REFRESH_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(refreshIcon, 0.53f));

		final BufferedImage addIcon = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage arrowLeft = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "arrow_left_icon.png");
		ARROW_LEFT_ICON = new ImageIcon(arrowLeft);
		ARROW_LEFT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(arrowLeft, 0.53f));
	}

	private final PluginPresetsPlugin plugin;
	private final JLabel errorNotification = new JLabel(NOTIFICATION_ICON);
	private final JLabel helpButton = new JLabel(HELP_ICON);
	private final JLabel refreshPlugins = new JLabel(REFRESH_ICON);
	private final JLabel addPreset = new JLabel(ADD_ICON);
	private final JLabel stopEdit = new JLabel(ARROW_LEFT_ICON);
	private final JLabel title = new JLabel();
	private final JLabel editTitle = new JLabel();
	private final IconTextField searchBar = new IconTextField();
	private final PluginErrorPanel noPresetsPanel = new PluginErrorPanel();
	private final JPanel titlePanel = new JPanel(new BorderLayout());
	private final JPanel editPanel = new JPanel(new BorderLayout());
	private final JPanel contentView = new JPanel(new GridBagLayout());

	public PluginPresetsPluginPanel(PluginPresetsPlugin pluginPresetsPlugin)
	{
		super(false);

		this.plugin = pluginPresetsPlugin;

		setLayout(new BorderLayout());

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(10, 10, 10, 5));

		title.setText("Plugin Presets");
		title.setForeground(Color.WHITE);
		title.setBorder(new EmptyBorder(0, 0, 0, 40));

		JPanel presetActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));

		errorNotification.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				errorNotification.setVisible(false);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				errorNotification.setIcon(NOTIFICATION_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				errorNotification.setIcon(NOTIFICATION_ICON);
			}
		});

		helpButton.setToolTipText("Need help?");
		helpButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				LinkBrowser.browse(PluginPresetsPlugin.HELP_LINK);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				helpButton.setIcon(HELP_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				helpButton.setIcon(HELP_ICON);
			}
		});

		refreshPlugins.setToolTipText("Refresh presets");
		refreshPlugins.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.refreshPresets();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				refreshPlugins.setIcon(REFRESH_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				refreshPlugins.setIcon(REFRESH_ICON);
			}
		});

		addPreset.setToolTipText("Create new plugin preset");
		addPreset.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseEvent.BUTTON3) // Right click
				{
					JPopupMenu importPopupMenu = getImportMenuPopup();
					addPreset.setComponentPopupMenu(importPopupMenu);
				}
				else
				{
					openPresetCreator();
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

		presetActions.add(errorNotification);
		presetActions.add(helpButton);
		presetActions.add(refreshPlugins);
		presetActions.add(addPreset);

		titlePanel.add(title, BorderLayout.WEST);
		titlePanel.add(presetActions, BorderLayout.EAST);

		editTitle.setForeground(Color.WHITE);

		stopEdit.setToolTipText("Stop editing");
		stopEdit.setBorder(new EmptyBorder(0, 0, 0, 10));
		stopEdit.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.setEditedPreset(null);
				emptyBar();
				rebuild();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				stopEdit.setIcon(ARROW_LEFT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				stopEdit.setIcon(ARROW_LEFT_ICON);
			}
		});

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				rebuild();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				rebuild();

			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				rebuild();
			}
		});

		JPanel searchWrapper = new JPanel(new BorderLayout());
		searchWrapper.add(searchBar, BorderLayout.CENTER);
		searchWrapper.setBorder(new EmptyBorder(10, 0, 3, 0));

		editPanel.add(stopEdit, BorderLayout.WEST);
		editPanel.add(editTitle, BorderLayout.CENTER);
		editPanel.add(searchWrapper, BorderLayout.SOUTH);
		editPanel.setVisible(false);

		northPanel.add(titlePanel, BorderLayout.NORTH);
		northPanel.add(editPanel, BorderLayout.CENTER);

		contentView.setBorder(new EmptyBorder(0, 7, 0, 7));

		JPanel contentWrapper = new JPanel(new BorderLayout());
		contentWrapper.add(contentView, BorderLayout.NORTH);

		JScrollPane scrollableContainer = new JScrollPane(contentWrapper);
		scrollableContainer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		noPresetsPanel.setContent("Plugin Presets", "Presets of your plugin configurations.");
		noPresetsPanel.setVisible(false);

		add(northPanel, BorderLayout.NORTH);
		add(scrollableContainer, BorderLayout.CENTER);

	}

	public void rebuild()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		contentView.removeAll();

		boolean editingPreset = plugin.getEditedPreset() != null;

		titlePanel.setVisible(!editingPreset);
		editPanel.setVisible(editingPreset);

		if (editingPreset)
		{
			PluginPreset preset = plugin.getEditedPreset();

			editTitle.setText("Editing Preset " + preset.getName());

			// Sort alphabetically similar to the configurations tab
			preset.getPluginConfigs().sort(Comparator.comparing(PluginConfig::getConfigName));

			List<PluginConfig> configs = preset.getPluginConfigs();

			final String text = searchBar.getText();
			if (!text.isEmpty())
			{
				configs = configs.stream().filter(
						c -> c.getName().toLowerCase()
							.contains(text.toLowerCase()))
					.collect(Collectors.toList());
			}

			for (final PluginConfig config : configs)
			{
				contentView.add(new ConfigPanel(config, preset, plugin), constraints);
				constraints.gridy++;
			}
		}
		else
		{
			for (final PluginPreset preset : plugin.getPluginPresets())
			{
				contentView.add(new PluginPresetsPanel(preset, plugin), constraints);
				constraints.gridy++;

				contentView.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
				constraints.gridy++;
			}

			boolean empty = constraints.gridy == 0;
			noPresetsPanel.setVisible(empty);
			title.setVisible(!empty);

			contentView.add(noPresetsPanel, constraints);
			constraints.gridy++;
		}

		errorNotification.setVisible(false);

		repaint();
		revalidate();
	}

	private JPopupMenu getImportMenuPopup()
	{
		JMenuItem importMenuOption = new JMenuItem();
		importMenuOption.setText("Import preset from clipboard");
		importMenuOption.addActionListener(e -> plugin.importPresetFromClipboard());

		JPopupMenu importPopupMenu = new JPopupMenu();
		importPopupMenu.setBorder(new EmptyBorder(2, 2, 2, 0));
		importPopupMenu.add(importMenuOption);
		return importPopupMenu;
	}

	public void renderNotification(String errorMessage)
	{
		errorNotification.setToolTipText(errorMessage);
		errorNotification.setVisible(true);
	}

	private void emptyBar()
	{
		searchBar.setText("");
	}

	private void openPresetCreator()
	{
		PresetCreatorDialog presetCreatorDialog = plugin.getPresetCreatorManager().create(
			SwingUtilities.windowForComponent(this),
			plugin);
		presetCreatorDialog.setLocation(getLocationOnScreen());
		presetCreatorDialog.setVisible(true);
	}
}
