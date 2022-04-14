package com.pluginpresets.ui;

import com.pluginpresets.PluginConfig;
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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class ConfigPanel extends JPanel
{
	private static final ImageIcon SWITCH_ON_ICON;
	private static final ImageIcon SWITCH_ON_HOVER_ICON;
	private static final ImageIcon SWITCH_OFF_ICON;
	private static final ImageIcon SWITCH_OFF_HOVER_ICON;
	private static final ImageIcon UPDATE_ICON;
	private static final ImageIcon UPDATE_HOVER_ICON;

	static
	{
		final BufferedImage switchOnImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_on_icon.png");
		SWITCH_ON_ICON = new ImageIcon(switchOnImg);
		SWITCH_ON_HOVER_ICON = new ImageIcon(ImageUtil.luminanceOffset(switchOnImg, 20));

		final BufferedImage switchOffImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_off_icon.png");
		final BufferedImage switchOffHoverImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_off_hover_icon.png");
		SWITCH_OFF_ICON = new ImageIcon(switchOffImg);
		SWITCH_OFF_HOVER_ICON = new ImageIcon(switchOffHoverImg);

		final BufferedImage updateImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "refresh_icon.png");
		UPDATE_ICON = new ImageIcon(updateImg);
		UPDATE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(updateImg, -100));
	}

	private final PluginConfig presetConfig;
	private final PluginConfig currentConfig;
	private final JLabel title = new JLabel();
	private final JPanel settings = new JPanel(new GridBagLayout());
	private final JLabel switchLabel = new JLabel();
	private final JLabel updateLabel = new JLabel();
	private final boolean presetHasConfigurations;

	public ConfigPanel(PluginConfig currentConfig, PluginConfig presetConfig, PluginPresetsPlugin plugin)
	{
		this.presetConfig = presetConfig;
		this.currentConfig = currentConfig;

		presetHasConfigurations = presetHasConfigurations();
		boolean configsMatch = configsMatch();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(3, 10, 3, 0));

		title.setText(currentConfig.getName());
		Color titleColor = presetHasConfigurations ? Color.WHITE : ColorScheme.LIGHT_GRAY_COLOR;
		title.setForeground(titleColor);
		// 0 width is to prevent the title causing the panel to grow in y direction on long plugin names
		// 16 height is UPDATE_ICONs height
		title.setPreferredSize(new Dimension(0, 16));
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
				title.setForeground(titleColor);
			}
		});

		JLabel statusLabel = new JLabel();
		if (presetHasConfigurations)
		{
			switchLabel.setIcon(SWITCH_ON_ICON);
			switchLabel.setToolTipText("Remove configurations for " + currentConfig.getName() + " from the preset.");
			switchLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.removeConfiguration(presetConfig);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_ON_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_ON_ICON);
				}
			});

			if (!configsMatch)
			{
				title.setToolTipText("Your configurations for " + currentConfig.getName() + " do not match the preset.");

				statusLabel.setText("Modified");
				statusLabel.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
				statusLabel.setBorder(new EmptyBorder(0, 0, 0, 2));
				statusLabel.setToolTipText("Your configurations for " + currentConfig.getName() + " do not match the preset.");

				updateLabel.setIcon(UPDATE_ICON);
				updateLabel.setToolTipText("Replace the presets configuration for " + currentConfig.getName() + " with your current configuration.");
				updateLabel.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mousePressed(MouseEvent mouseEvent)
					{
						plugin.removeConfiguration(presetConfig);
						plugin.addConfiguration(currentConfig);
					}

					@Override
					public void mouseEntered(MouseEvent mouseEvent)
					{
						updateLabel.setIcon(UPDATE_HOVER_ICON);
					}

					@Override
					public void mouseExited(MouseEvent mouseEvent)
					{
						updateLabel.setIcon(UPDATE_ICON);
					}
				});
			}
			else
			{
				title.setToolTipText("Your configurations for " + currentConfig.getName() + " match the preset.");
			}
		}
		else
		{
			title.setToolTipText("This preset does not include any configurations to " + currentConfig.getName() + " plugin.");

			switchLabel.setIcon(SWITCH_OFF_ICON);
			switchLabel.setToolTipText("Add your current configuration for " + currentConfig.getName() + " to the preset.");
			switchLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent mouseEvent)
				{
					plugin.addConfiguration(currentConfig);
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_OFF_HOVER_ICON);
				}

				@Override
				public void mouseExited(MouseEvent mouseEvent)
				{
					switchLabel.setIcon(SWITCH_OFF_ICON);
				}
			});
		}

		switchLabel.setPreferredSize(new Dimension(20, 16));

		JPanel rightActions = new JPanel();
		rightActions.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		rightActions.add(statusLabel);
		rightActions.add(updateLabel);
		rightActions.add(switchLabel);

		JPanel topActions = new JPanel(new BorderLayout());
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

			settings.add(new JLabel("Plugin " + (currentConfig.getEnabled() ? "enabled" : "disabled")), constraints);
			constraints.gridy++;

			currentConfig.getSettings().forEach(setting -> {
				settings.add(new JLabel(setting.getName()), constraints);
				constraints.gridy++;
			});
		}

		settings.setVisible(!visible);

		revalidate();
		repaint();
	}

	private boolean presetHasConfigurations()
	{
		return presetConfig != null;
	}

	private boolean configsMatch()
	{
		return presetHasConfigurations && presetConfig.equals(currentConfig);
	}

}
