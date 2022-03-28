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
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.pushingpixels.substance.internal.SubstanceSynapse;

public class PresetCreatorDialog extends JDialog
{
	private static final int FRAME_WIDTH = 250;
	private static final int FRAME_HEIGHT = 150;
	private static final int BUTTON_WIDTH = 80;
	private static final int BUTTON_HEIGHT = 40;
	private static final Dimension BUTTON_SIZE = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);

	public PresetCreatorDialog(Window owner, PluginPresetsPlugin plugin)
	{
		super(owner, "Create a new Plugin Preset", ModalityType.APPLICATION_MODAL);

		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLayout(new BorderLayout());

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(5, 5, 5, 5));
		// To reduce substance's colorization (tinting)
		content.putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);

		JPanel buttonContainer = new JPanel();

		JButton fullCreate = new JButton("All");
		fullCreate.setPreferredSize(BUTTON_SIZE);
		fullCreate.setToolTipText("Create a new preset with all your plugin configurations");
		fullCreate.addActionListener(e -> {
			String presetName = JOptionPane.showInputDialog(PresetCreatorDialog.this, "Give your new preset a name.", "New preset", JOptionPane.QUESTION_MESSAGE);
			if (presetName != null)
			{
				plugin.createPreset(presetName);
			}
		});


		JButton partialCreate = new JButton("Partial");
		partialCreate.setPreferredSize(BUTTON_SIZE);
		partialCreate.setToolTipText("Create a new empty preset");

		JButton defaultsCreate = new JButton("Defaults");
		defaultsCreate.setPreferredSize(BUTTON_SIZE);
		defaultsCreate.setToolTipText("Create preset with default RuneLite configurations");

		JButton importCreate = new JButton("Import");
		importCreate.setPreferredSize(BUTTON_SIZE);
		importCreate.setToolTipText("Import preset from clipboard");
		importCreate.addActionListener(e ->
			plugin.importPresetFromClipboard()
		);

		buttonContainer.add(fullCreate);
		buttonContainer.add(partialCreate);
		buttonContainer.add(defaultsCreate);
		buttonContainer.add(importCreate);

		content.add(buttonContainer, BorderLayout.CENTER);

		setContentPane(content);
	}
}
