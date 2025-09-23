package com.necro.raid.dens.common.client.gui;

import com.necro.raid.dens.common.client.gui.buttons.LeaveRaidButton;
import com.necro.raid.dens.common.client.gui.buttons.OverlayButton;
import com.necro.raid.dens.common.client.gui.screens.RaidOverlay;
import com.necro.raid.dens.common.client.gui.screens.RaidRequestOverlay;
import com.necro.raid.dens.common.client.gui.screens.RaidRewardOverlay;

public class RaidScreenComponents {
    public static final RaidOverlay RAID_OVERLAY = new RaidOverlay();
    public static final RaidRequestOverlay REQUEST_OVERLAY = new RaidRequestOverlay();
    public static final RaidRewardOverlay REWARD_OVERLAY = new RaidRewardOverlay();

    public static LeaveRaidButton LEAVE_RAID_BUTTON;

    public static OverlayButton ACCEPT_REQUEST_BUTTON;
    public static OverlayButton DENY_REQUEST_BUTTON;
    public static OverlayButton ACCEPT_REWARD_BUTTON;
    public static OverlayButton DENY_REWARD_BUTTON;
}
