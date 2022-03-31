package com.pluginpresets.ui;

import com.pluginpresets.PluginConfig;
import com.pluginpresets.PluginPreset;
import com.pluginpresets.PluginPresetsPlugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class ConfigPanel extends JPanel
{
	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	static
	{
		final BufferedImage deleteImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));
	}

	private final PluginConfig config;
	private final JLabel title = new JLabel();
	private final JPanel settings = new JPanel(new GridBagLayout());
	private final JLabel deleteLabel = new JLabel();

	public ConfigPanel(PluginConfig config, PluginPreset preset, PluginPresetsPlugin plugin)
	{
		this.config = config;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 10, 0, 0));

		JPanel topActions = new JPanel(new BorderLayout());

		title.setText(config.getName());
		title.setForeground(Color.WHITE);
		title.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				toggleSettings();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				title.setForeground(ColorScheme.BRAND_ORANGE);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				title.setForeground(Color.WHITE);
			}
		});

		JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 3));

		deleteLabel.setIcon(DELETE_ICON);
		deleteLabel.setToolTipText("Remove plugin configuration");
		deleteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.getPresetManager().removeConfiguration(config, preset);
				plugin.savePresets();

				setVisible(false);

				repaint();
				revalidate();
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

		rightActions.add(deleteLabel);

		topActions.add(title, BorderLayout.CENTER);
		topActions.add(rightActions, BorderLayout.EAST);

		settings.setVisible(false);
		settings.setBorder(new EmptyBorder(0, 5, 0, 0));

		add(topActions, BorderLayout.NORTH);
		add(settings, BorderLayout.CENTER);
	}

	private void toggleSettings()
	{
		boolean visible = settings.isVisible();

		if (!visible)
		{
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
			constraints.gridx = 0;
			constraints.gridy = 0;

			settings.removeAll();

			settings.add(new JLabel("Plugin " + (config.getEnabled() ? "enabled" : "disabled")), constraints);
			constraints.gridy++;

			config.getSettings().forEach(setting -> {
				settings.add(new JLabel(setting.getName()), constraints);
				constraints.gridy++;
			});
		}

		settings.setVisible(!visible);

		revalidate();
		repaint();
	}
}
