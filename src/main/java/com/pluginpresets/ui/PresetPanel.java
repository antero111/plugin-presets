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
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;

class PresetPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon SWITCH_ON_ICON;
	private static final ImageIcon SWITCH_OFF_ICON;
	private static final ImageIcon SWITCH_OFF_HOVER_ICON;
	private static final ImageIcon UPDATE_ICON;
	private static final ImageIcon UPDATE_HOVER_ICON;
	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;
	private static final ImageIcon EDIT_ICON;
	private static final ImageIcon EDIT_HOVER_ICON;

	static
	{
		final BufferedImage switchOnImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_on_icon.png");
		SWITCH_ON_ICON = new ImageIcon(switchOnImg);

		final BufferedImage switchOffImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"switch_off_icon.png");
		final BufferedImage switchOffHoverImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"switch_off_hover_icon.png");
		SWITCH_OFF_ICON = new ImageIcon(switchOffImg);
		SWITCH_OFF_HOVER_ICON = new ImageIcon(switchOffHoverImg);

		final BufferedImage updateImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "copy_icon.png");
		UPDATE_ICON = new ImageIcon(updateImg);
		UPDATE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(updateImg, -100));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));

		final BufferedImage editImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "edit_icon.png");
		EDIT_ICON = new ImageIcon(editImg);
		EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImg, 0.53f));
	}

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
				nameInput.setEditable(true);
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

		nameInput.setText(preset.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 0));
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

		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		JPanel bottomContainer = new JPanel(new BorderLayout());
		bottomContainer.setBorder(new EmptyBorder(6, 0, 6, 0));
		bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
		leftActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		loadLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		JLabel notice = new JLabel();

		boolean presetMatches = plugin.getMatchingPresets().contains(preset);
		boolean emptyPreset = false;
		if (presetMatches)
		{
			loadLabel.setIcon(SWITCH_ON_ICON);
			loadLabel.setToolTipText("Current configurations match this preset");

			emptyPreset = preset.isEmpty();
			if (emptyPreset)
			{
				notice.setFont(FontManager.getRunescapeSmallFont());
				notice.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
				notice.setText("Empty preset");
				notice.setToolTipText("This preset has no configurations, click the edit icon to add configurations.");
				loadLabel.setVisible(false);
			}
		}
		else
		{
			loadLabel.setIcon(SWITCH_OFF_ICON);
			loadLabel.setToolTipText("Load this preset");
			loadLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.loadPreset(preset);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					loadLabel.setIcon(SWITCH_OFF_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					loadLabel.setIcon(SWITCH_OFF_ICON);
				}
			});
		}

		leftActions.add(loadLabel);

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
		int keybindWidth = emptyPreset ? 50 : 85;
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

		shareLabel.setIcon(UPDATE_ICON);
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
				shareLabel.setIcon(UPDATE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				shareLabel.setIcon(UPDATE_ICON);
			}
		});

		deleteLabel.setIcon(DELETE_ICON);
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
				deleteLabel.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_ICON);
			}
		});

		editLabel.setIcon(EDIT_ICON);
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
				editLabel.setIcon(EDIT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				editLabel.setIcon(EDIT_ICON);
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
		plugin.refreshPresets();
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
		plugin.updatePreset(preset);
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

	public void editPreset(PluginPreset preset)
	{
		plugin.setPresetEditor(new PluginPresetsPresetEditor(plugin, preset));
		plugin.rebuildPluginUi();
	}
}
