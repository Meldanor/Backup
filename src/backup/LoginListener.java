/*
 *  Copyright (C) 2011 Kilian Gaertner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package backup;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Kilian Gaertner
 */
public class LoginListener extends PlayerListener implements PropertyConstants {

    private int taskID = -2;
    private PropertiesSystem pSystem;
    private Plugin plugin;

    public LoginListener (Plugin plugin, PropertiesSystem pSystem) {
        this.pSystem = pSystem;
        this.plugin = plugin;
    }

    @Override
    public void onPlayerLogin (PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        if (taskID != -2 && server.getOnlinePlayers().length == 0) {
            server.getScheduler().cancelTask(taskID);
            System.out.println("[BACKUP] Stopped last backup, start with normal backup cyclus!");
            taskID = -2;
        }
    }

    @Override
    public void onPlayerQuit (PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        if (server.getOnlinePlayers().length <= 1) {
            int intervall = pSystem.getIntProperty(INT_BACKUP_INTERVALL);
            System.out.println("[BACKUP] Initiate a last backup because the last player left. It will start to backup in " + (intervall / 1200) + " minutes when no player will have connected in this time.");
            taskID = server.getScheduler().scheduleAsyncDelayedTask(plugin, new LastBackupTask(server, pSystem), intervall);
        }
    }
}
