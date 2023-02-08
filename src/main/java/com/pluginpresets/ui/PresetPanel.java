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
import com.pluginpresets.PluginPresetsPresetEditor;
import com.pluginpresets.PluginPresetsUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;

/**
 * Row representing a single preset in the list of presets
 */
class PresetPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private final PluginPresetsPlugin plugin;
	private final PluginPreset preset;
	private final JPanel labelWrapper = new JPanel();
	private final JLabel keybindLabel = new JLabel();
	private final JLabel shareLabel = new JLabel();
	private final JLabel editLabel = new JLabel();
	private final JLabel deleteLabel = new JLabel();
	private final JLabel loadLabel = new JLabel();
	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel rename = new JLabel("Rename");
	private final JLabel saveRename = new JLabel("Save");
	private final JLabel cancelRename = new JLabel("Cancel");
	private final JPanel keybindWrapper = new JPanel();
	private final JLabel keybind = new JLabel();
	private final JLabel saveKeybind = new JLabel("Save");
	private final JLabel cancelKeybind = new JLabel("Cancel");
	private final JPanel presetNameContainer = new JPanel();
	private KeyEvent savedKeybind = null;

	PresetPanel(PluginPreset pluginPreset, PluginPresetsPlugin pluginPresetsPlugin)
	{
		this.plugin = pluginPresetsPlugin;
		this.preset = pluginPreset;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel(new BorderLayout(3, 0));
		nameActions.setBorder(new EmptyBorder(0, 0, 0, 8));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getClickCount() == 2)  // double click
				{
					editPreset(pluginPreset);
				}
			}
		});

		saveRename.setVisible(false);
		saveRename.setFont(FontManager.getRunescapeSmallFont());
		saveRename.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		saveRename.setToolTipText("Save new name");
		saveRename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				saveRename();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				saveRename.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				saveRename.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancelRename.setVisible(false);
		cancelRename.setFont(FontManager.getRunescapeSmallFont());
		cancelRename.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancelRename.setToolTipText("Cancel rename");
		cancelRename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				cancelRename();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancelRename.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancelRename.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		rename.setFont(FontManager.getRunescapeSmallFont());
		rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		rename.setToolTipText("Rename preset");
		rename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				updateNameActions(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			}
		});

		nameActions.add(saveRename, BorderLayout.EAST);
		nameActions.add(cancelRename, BorderLayout.WEST);
		nameActions.add(rename, BorderLayout.CENTER);

		JPanel nameContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		nameContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		nameInput.setText(preset.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(145, 25));
		nameInput.setVisible(false);
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(0, 5, 0, 0));
		nameInput.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					saveRename();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					cancelRename();
				}
			}
		});

		JLabel nameLabel = new JLabel(preset.getName());
		nameLabel.setPreferredSize(new Dimension(170, 15));
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		if (!preset.getLocal())
		{
			nameLabel.setIcon(Icons.SYNC_CONFIG_ICON);
			nameLabel.setToolTipText("Stored in RuneLite config");
		}

		presetNameContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		presetNameContainer.add(nameLabel);

		nameContainer.add(nameInput);
		nameContainer.add(presetNameContainer);

		nameWrapper.add(nameContainer, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		JPanel bottomContainer = new JPanel(new BorderLayout());
		bottomContainer.setBorder(new EmptyBorder(6, 0, 6, 0));
		bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
		leftActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftActions.setPreferredSize(new Dimension(65, 14));

		loadLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
		loadLabel.setPreferredSize(new Dimension(25, 14));

		Boolean loadOnFocus = preset.getLoadOnFocus();
		boolean autoUpdate = plugin.getAutoUpdater() != null && plugin.getAutoUpdater().getEditedPreset().getId() == preset.getId();
		JPopupMenu loadLabelPopup = getLoadLabelPopup();
		loadLabel.setComponentPopupMenu(loadLabelPopup);

		JLabel notice = new JLabel();

		boolean emptyPreset = false;
		Boolean match = preset.match(plugin.getCurrentConfigurations());
		if (match)
		{
			loadLabel.setIcon(Icons.SWITCH_ON_ICON);
			String text = preset.canBeDisabled()
				? "Current configurations match this preset. Click to disable preset."
				: "Current configurations match this preset.";
			loadLabel.setToolTipText(text);
			loadLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					if (mouseEvent.getButton() == MouseEvent.BUTTON1)
					{
						plugin.disablePreset(preset);
					}
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					loadLabel.setIcon(Icons.SWITCH_ON_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					loadLabel.setIcon(Icons.SWITCH_ON_ICON);
				}
			});

			emptyPreset = preset.isEmpty();
			if (emptyPreset)
			{
				notice.setFont(FontManager.getRunescapeSmallFont());
				notice.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
				notice.setText("Empty preset");
				notice.setBorder(new EmptyBorder(0, 5, 0, 0));
				notice.setToolTipText("This preset has no configurations, click the edit icon to add configurations.");
				leftActions.setVisible(false);
			}
		}
		else
		{
			loadLabel.setIcon(Icons.SWITCH_OFF_ICON);
			loadLabel.setToolTipText("Load this preset");
			loadLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					if (mouseEvent.getButton() == MouseEvent.BUTTON1)
					{
						plugin.loadPreset(preset);
					}
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					loadLabel.setIcon(Icons.SWITCH_OFF_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					loadLabel.setIcon(Icons.SWITCH_OFF_ICON);
				}
			});
		}

		JLabel focusActionLabel = new JLabel();
		if (loadOnFocus != null && !emptyPreset)
		{
			String text;
			if (loadOnFocus)
			{
				focusActionLabel.setIcon(Icons.FOCUS_ICON);
				text = "This preset gets loaded when client gets focused";
			}
			else
			{
				focusActionLabel.setIcon(Icons.UNFOCUS_ICON);
				text = "This preset gets loaded when client gets unfocused";
			}
			focusActionLabel.setToolTipText(text);
		}

		JLabel autoUpdateLabel = new JLabel();
		if (!emptyPreset)
		{
			if (autoUpdate)
			{
				autoUpdateLabel.setIcon(Icons.ORANGE_REFRESH_ICON);
				autoUpdateLabel.setToolTipText("Auto updated preset");
			}
			else if (preset.getAutoUpdated() != null)
			{
				autoUpdateLabel.setIcon(Icons.REFRESH_INACTIVE_ICON);
				autoUpdateLabel.setToolTipText("Auto updated when loaded");
			}
		}

		leftActions.add(loadLabel);
		leftActions.add(autoUpdateLabel);
		leftActions.add(focusActionLabel);

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		keybindWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 3));
		keybindWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		keybindWrapper.setVisible(false);

		JPanel keybindActions = new JPanel();
		keybindActions.setLayout(new BorderLayout(3, 0));
		keybindActions.setBorder(new EmptyBorder(0, 5, 0, 2));
		keybindActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		keybindActions.setAlignmentX(RIGHT_ALIGNMENT);

		saveKeybind.setFont(FontManager.getRunescapeSmallFont());
		saveKeybind.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		saveKeybind.setToolTipText("Save keybind");
		saveKeybind.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				saveKeybind();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				saveKeybind.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				saveKeybind.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancelKeybind.setFont(FontManager.getRunescapeSmallFont());
		cancelKeybind.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancelKeybind.setToolTipText("Cancel keybind");
		cancelKeybind.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				cancelKeybind();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancelKeybind.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancelKeybind.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		keybindActions.add(cancelKeybind, BorderLayout.WEST);
		keybindActions.add(saveKeybind, BorderLayout.EAST);

		keybind.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		keybind.setFont(FontManager.getRunescapeSmallFont());
		int keybindWidth = emptyPreset ? 50 : 60;
		keybind.setPreferredSize(new Dimension(keybindWidth, 12));
		keybind.setHorizontalAlignment(SwingConstants.RIGHT);

		boolean keybindSet = preset.getKeybind() != null;
		if (keybindSet)
		{
			String keybindText = preset.getKeybind().toString();
			keybindLabel.setText(keybindText);
			keybindLabel.setToolTipText(keybindText + " (Click to edit)");

			keybindLabel.setPreferredSize(new Dimension(keybindWidth, 12));
			keybindLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			keybindLabel.setAlignmentX(RIGHT_ALIGNMENT);

			keybind.setText(keybindText);
			keybind.setToolTipText(keybindText + " (Clear with backspace)");

			// Inform that keybind don't work in login screen
			if (!plugin.getLoggedIn())
			{
				keybindLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
				keybindLabel.setToolTipText(keybindText + " (Keybinds are disabled on login screen)");
			}
		}
		else
		{
			keybindLabel.setText("Not set");
			keybindLabel.setToolTipText("Click to bind this preset to a keybind");
			keybindLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());

			keybind.setText("No keybind set");
			keybind.setToolTipText("Type a keybind you wish to set");
		}

		keybindLabel.addMouseListener(new MouseAdapter()
		{
			private Color foreground;

			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				editKeybind();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				foreground = keybindLabel.getForeground();
				keybindLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				keybindLabel.setForeground(foreground);
			}
		});

		keybindWrapper.add(keybind);
		keybindWrapper.add(keybindActions);

		shareLabel.setIcon(Icons.COPY_ICON);
		shareLabel.setToolTipText("Copy preset to clipboard");
		shareLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.exportPresetToClipboard(preset);
				JOptionPane.showMessageDialog(shareLabel,
					"Preset data of '" + preset.getName() + "' copied to clipboard.", "Preset exported",
					JOptionPane.INFORMATION_MESSAGE);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				shareLabel.setIcon(Icons.COPY_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				shareLabel.setIcon(Icons.COPY_ICON);
			}
		});

		deleteLabel.setIcon(Icons.DELETE_ICON);
		deleteLabel.setToolTipText("Delete preset");
		deleteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				int confirm = JOptionPane.showConfirmDialog(PresetPanel.this,
					"Are you sure you want to permanently delete this plugin preset?",
					"Delete preset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if (confirm == 0)
				{
					plugin.deletePreset(preset);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(Icons.DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(Icons.DELETE_ICON);
			}
		});

		editLabel.setIcon(Icons.EDIT_ICON);
		editLabel.setToolTipText("Edit preset configurations");
		editLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				editPreset(preset);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				editLabel.setIcon(Icons.EDIT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				editLabel.setIcon(Icons.EDIT_ICON);
			}
		});

		labelWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		labelWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		labelWrapper.add(keybindLabel);
		labelWrapper.add(shareLabel);
		labelWrapper.add(editLabel);
		labelWrapper.add(deleteLabel);

		rightActions.add(keybindWrapper);
		rightActions.add(labelWrapper);

		bottomContainer.add(leftActions, BorderLayout.WEST);
		bottomContainer.add(notice, BorderLayout.CENTER);
		bottomContainer.add(rightActions, BorderLayout.EAST);

		add(nameWrapper, BorderLayout.NORTH);
		add(bottomContainer, BorderLayout.CENTER);
	}

	private void saveRename()
	{
		updatePresetName();

		nameInput.setEditable(false);
		updateNameActions(false);
		requestFocusInWindow();

		plugin.savePresets();
		plugin.rebuildPluginUi();
	}

	private void updatePresetName()
	{
		String nameInputText = nameInput.getText();
		if (nameIsValid(nameInputText))
		{
			preset.setName(nameInputText);
		}
		else
		{
			if (!nameIsValid(preset.getName()))
			{
				setDefaultPresetName();
			}
			// Else keep the old name
		}
	}

	private boolean nameIsValid(String name)
	{
		return !nameInput.getText().equals("") && !PluginPresetsUtils.stringContainsInvalidCharacters(name);
	}

	private void setDefaultPresetName()
	{
		String defaultPresetName = PluginPresetsPlugin.DEFAULT_PRESET_NAME;
		preset.setName(defaultPresetName);
		nameInput.setText(defaultPresetName);
	}

	private void cancelRename()
	{
		nameInput.setEditable(false);
		nameInput.setText(preset.getName());
		updateNameActions(false);
		requestFocusInWindow();
	}

	private void updateNameActions(boolean saveAndCancel)
	{
		saveRename.setVisible(saveAndCancel);
		cancelRename.setVisible(saveAndCancel);
		rename.setVisible(!saveAndCancel);

		nameInput.setEditable(saveAndCancel);
		nameInput.setVisible(saveAndCancel);
		presetNameContainer.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
		}
	}

	private void showKeybind(boolean show)
	{
		labelWrapper.setVisible(!show);
		keybindWrapper.setVisible(show);
	}

	private void editKeybind()
	{
		showKeybind(true);
		keybind.requestFocusInWindow();
		keybind.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				handleKeybindInput(e);
			}

		});
	}

	private void handleKeybindInput(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_ENTER)
		{
			saveKeybind();
		}
		else if (keyCode == KeyEvent.VK_ESCAPE)
		{
			cancelKeybind();
		}
		else if (keyCode == KeyEvent.VK_BACK_SPACE)
		{
			savedKeybind = e;
			clearKeybind();
		}
		else
		{
			savedKeybind = e;
			String keyText = KeyEvent.getKeyText(keyCode);
			String keybindText = keyText.toUpperCase();

			int modifiersEx = e.getModifiersEx();
			if (modifiersEx != 0) // If modifier included e.g. CTRL
			{
				String modifiersExText = InputEvent.getModifiersExText(modifiersEx);
				String text = (modifiersExText + "+" + keyText).toUpperCase();
				if (!modifiersExText.equals(keyText)) // Don't include only multiplier keybindings e.g. ALT+ALT
				{
					keybindText = text;
				}

				keybind.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				keybind.setToolTipText(keybindText + " (Clear with backspace)");
			}
			else
			{
				keybind.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
				keybind.setToolTipText("Add some modifier to this keybind! This preset could possibly be accidentally enabled e.g. when typing chat messages.");
			}

			if (!keybind.getText().equals(keybindText))
			{
				keybind.setText(keybindText);
			}
		}
	}

	private void saveKeybind()
	{
		showKeybind(false);

		// Save pressed but no changes made to preset keybind
		if (preset.getKeybind() != null && savedKeybind == null)
		{
			return;
		}

		// Clear keybind if keybind was cleared with backspace and then saved
		Keybind presetKeybind = (savedKeybind == null || savedKeybind.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			? null
			: new Keybind(savedKeybind);

		preset.setKeybind(presetKeybind);
		plugin.savePresets();
	}

	private void cancelKeybind()
	{
		savedKeybind = null;
		showKeybind(false);
	}

	private void clearKeybind()
	{
		keybind.setText("No keybind set");
		keybind.setToolTipText("Save to clear keybind");
	}

	private JPopupMenu getLoadLabelPopup()
	{
		JPopupMenu popupMenu = new JPopupMenu();

		Boolean match = preset.match(plugin.getCurrentConfigurations());
		if (!match)
		{
			JMenuItem loadOption = new JMenuItem();
			loadOption.setText("Load preset");
			loadOption.addActionListener(e -> plugin.loadPreset(preset));

			JMenuItem divider = getDivider();

			popupMenu.add(loadOption);
			popupMenu.add(divider);
		}

		JMenuItem toggleAutoUpdate = new JMenuItem();
		toggleAutoUpdate.setText("Toggle auto update");
		toggleAutoUpdate.addActionListener(e -> toggleAutoUpdate());

		JMenuItem clearAutoUpdate = new JMenuItem();
		clearAutoUpdate.setText("Clear auto update");
		clearAutoUpdate.addActionListener(e -> clearAutoUpdate());

		JMenuItem divider = getDivider();

		JMenuItem focusedOption = new JMenuItem();
		focusedOption.setText("Load when focused");
		focusedOption.setToolTipText("Load this preset automatically when client gets focused");
		focusedOption.addActionListener(e -> setPresetWindowFocus(true));

		JMenuItem unfocusedOption = new JMenuItem();
		unfocusedOption.setText("Load when unfocused");
		unfocusedOption.setToolTipText("Load this preset automatically when client gets unfocused");
		unfocusedOption.addActionListener(e -> setPresetWindowFocus(false));

		JMenuItem clearFocusOption = new JMenuItem();
		clearFocusOption.setText("Clear focus action");
		clearFocusOption.addActionListener(e -> setPresetWindowFocus(null));

		popupMenu.setBorder(new EmptyBorder(2, 2, 2, 0));
		popupMenu.add(toggleAutoUpdate);
		popupMenu.add(clearAutoUpdate);
		popupMenu.add(divider);
		popupMenu.add(focusedOption);
		popupMenu.add(unfocusedOption);
		popupMenu.add(clearFocusOption);
		return popupMenu;
	}

	private JMenuItem getDivider()
	{
		JMenuItem divider = new JMenuItem();
		divider.setBorder(new EmptyBorder(1, 5, 1, 5));
		divider.setPreferredSize(new Dimension(0, 1));
		divider.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
		return divider;
	}

	private void toggleAutoUpdate()
	{
		if (preset.match(plugin.getCurrentConfigurations()))
		{
			plugin.setAutoUpdatedPreset(preset.getId());
		}
		else
		{
			preset.setAutoUpdated(true);
			plugin.savePresets();
		}
	}

	private void clearAutoUpdate()
	{
		boolean thisAutoUpdated = plugin.getAutoUpdater() != null && plugin.getAutoUpdater().getEditedPreset().getId() == preset.getId();
		if (thisAutoUpdated)
		{
			plugin.setAutoUpdatedPreset(null);
		}
		plugin.removeAutoUpdateFrom(preset);
	}

	private void setPresetWindowFocus(Boolean loadOnFocus)
	{
		preset.setLoadOnFocus(loadOnFocus);
		plugin.savePresets();
	}

	public void editPreset(PluginPreset preset)
	{
		plugin.setPresetEditor(new PluginPresetsPresetEditor(plugin, preset, plugin.getCurrentConfigurations()));
		plugin.setFocusChangedPaused(true);
		plugin.rebuildPluginUi();
	}
}
