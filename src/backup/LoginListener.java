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
 * @author Meldanor
 */
public class LoginListener extends PlayerListener implements PropertyConstants{

    private int taskID = 0;
    private PropertiesSystem pSystem;
    private Plugin plugin;

    public LoginListener(Plugin plugin, PropertiesSystem pSystem) {
        this.pSystem = pSystem;
        this.plugin = plugin;
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        if (server.getOnlinePlayers().length > 0)
            server.getScheduler().cancelTask(taskID);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Server server = player.getServer();
        if (server.getOnlinePlayers().length <= 1) {
            taskID =  server.getScheduler().scheduleAsyncDelayedTask(plugin,new LastBackupTask(server,pSystem),pSystem.getIntProperty(INT_BACKUP_INTERVALL));
        }
    }


}
