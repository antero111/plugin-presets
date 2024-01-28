package com.pluginpresets.ui;

import com.pluginpresets.PluginPresetsPlugin;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import net.runelite.client.util.ImageUtil;

public final class Icons
{

	static final ImageIcon NOTIFICATION_ICON;
	static final ImageIcon NOTIFICATION_HOVER_ICON;
	static final ImageIcon HELP_ICON;
	static final ImageIcon HELP_HOVER_ICON;
	static final ImageIcon ELLIPSIS;
	static final ImageIcon ELLIPSIS_HOVER;
	static final ImageIcon SYNC_CONFIG_ICON;
	static final ImageIcon SYNC_CONFIG_HOVER_ICON;
	static final ImageIcon SYNC_LOCAL_ICON;
	static final ImageIcon SYNC_LOCAL_HOVER_ICON;
	static final ImageIcon REFRESH_ICON;
	static final ImageIcon REFRESH_INACTIVE_ICON;
	static final ImageIcon REFRESH_HOVER_ICON;
	static final ImageIcon ORANGE_REFRESH_ICON;
	static final ImageIcon ORANGE_REFRESH_HOVER_ICON;
	static final ImageIcon ADD_ICON;
	static final ImageIcon ADD_HOVER_ICON;
	static final ImageIcon ARROW_LEFT_ICON;
	static final ImageIcon ARROW_LEFT_HOVER_ICON;
	static final ImageIcon PAUSE_ICON;
	static final ImageIcon PAUSE_HOVER_ICON;
	static final ImageIcon PLAY_ICON;
	static final ImageIcon PLAY_HOVER_ICON;
	static final ImageIcon SWITCH_ON_ICON;
	static final ImageIcon SWITCH_OFF_ICON;
	static final ImageIcon SWITCH_OFF_HOVER_ICON;
	static final ImageIcon DUPLICATE_ICON;
	static final ImageIcon DUPLICATE_HOVER_ICON;
	static final ImageIcon DELETE_ICON;
	static final ImageIcon DELETE_HOVER_ICON;
	static final ImageIcon EDIT_ICON;
	static final ImageIcon EDIT_HOVER_ICON;
	static final ImageIcon FOCUS_ICON;
	static final ImageIcon UNFOCUS_ICON;
	static final ImageIcon NOT_INSTALLED_ICON;
	static final ImageIcon NOT_INSTALLED_HOVER_ICON;
	static final ImageIcon ARROW_DOWN_ICON;
	static final ImageIcon ARROW_RIGHT_ICON;
	static final ImageIcon ARROW_RIGHT_HOVER_ICON;
	static final ImageIcon UPDATE_ICON;
	static final ImageIcon UPDATE_HOVER_ICON;

	static
	{
		final BufferedImage notificationImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"warning_icon.png");
		NOTIFICATION_ICON = new ImageIcon(notificationImg);
		NOTIFICATION_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(notificationImg, 0.53f));

		final BufferedImage helpImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "help_icon.png");
		HELP_ICON = new ImageIcon(helpImg);
		HELP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(helpImg, 0.53f));

		final BufferedImage ellipsisImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "ellipsis_icon.png");
		ELLIPSIS = new ImageIcon(ellipsisImg);
		ELLIPSIS_HOVER = new ImageIcon(ImageUtil.alphaOffset(ellipsisImg, 0.53f));

		final BufferedImage cloudImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "cloud_icon.png");
		SYNC_CONFIG_ICON = new ImageIcon(cloudImg);
		SYNC_CONFIG_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(cloudImg, -100));

		final BufferedImage folderImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "folder_icon.png");
		SYNC_LOCAL_ICON = new ImageIcon(folderImg);
		SYNC_LOCAL_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(folderImg, -100));

		final BufferedImage refreshImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "refresh_icon.png");
		REFRESH_ICON = new ImageIcon(refreshImg);
		REFRESH_INACTIVE_ICON = new ImageIcon(ImageUtil.alphaOffset(refreshImg, -120));
		REFRESH_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(refreshImg, 0.53f));

		final BufferedImage orangeRefreshImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"orange_refresh_icon.png");
		ORANGE_REFRESH_ICON = new ImageIcon(orangeRefreshImg);
		ORANGE_REFRESH_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(orangeRefreshImg, 0.63f));

		final BufferedImage addImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addImg);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addImg, 0.53f));

		final BufferedImage arrowLeftImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"arrow_left_icon.png");
		ARROW_LEFT_ICON = new ImageIcon(arrowLeftImg);
		ARROW_LEFT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(arrowLeftImg, 0.53f));

		final BufferedImage pauseImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "pause_icon.png");
		PAUSE_ICON = new ImageIcon(pauseImg);
		PAUSE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(pauseImg, 0.53f));

		final BufferedImage playImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "play_icon.png");
		PLAY_ICON = new ImageIcon(playImg);
		PLAY_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(playImg, 0.53f));

		final BufferedImage switchOnImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "switch_on_icon.png");
		SWITCH_ON_ICON = new ImageIcon(switchOnImg);

		final BufferedImage switchOffImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"switch_off_icon.png");
		final BufferedImage switchOffHoverImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"switch_off_hover_icon.png");
		SWITCH_OFF_ICON = new ImageIcon(switchOffImg);
		SWITCH_OFF_HOVER_ICON = new ImageIcon(switchOffHoverImg);

		final BufferedImage duplicateImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "duplicate_icon.png");
		DUPLICATE_ICON = new ImageIcon(duplicateImg);
		DUPLICATE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(duplicateImg, -100));

		final BufferedImage deleteImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));

		final BufferedImage editImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "edit_icon.png");
		EDIT_ICON = new ImageIcon(editImg);
		EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImg, 0.53f));

		final BufferedImage focusImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "focus_icon.png");
		FOCUS_ICON = new ImageIcon(focusImg);

		final BufferedImage unfocusImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "unfocus_icon.png");
		UNFOCUS_ICON = new ImageIcon(unfocusImg);

		final BufferedImage notInstalledImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"not_installed_icon.png");
		NOT_INSTALLED_ICON = new ImageIcon(notInstalledImg);
		NOT_INSTALLED_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(notInstalledImg, 0.80f));

		final BufferedImage arrowDownImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class,
			"arrow_right_icon.png");
		ARROW_DOWN_ICON = new ImageIcon(ImageUtil.rotateImage(arrowDownImg, (Math.PI / 2)));
		ARROW_RIGHT_ICON = new ImageIcon(ImageUtil.alphaOffset(arrowDownImg, 0.45f));
		ARROW_RIGHT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(arrowDownImg, 0.80f));

		final BufferedImage updateImg = ImageUtil.loadImageResource(PluginPresetsPlugin.class, "refresh_icon.png");
		UPDATE_ICON = new ImageIcon(updateImg);
		UPDATE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(updateImg, -100));
	}
}
