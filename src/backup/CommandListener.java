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

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * For manual backups
 * @author Kilian Gaertner
 */
public class CommandListener extends PlayerListener {

    private BackupTask backupTask = null;

    public CommandListener(BackupTask backupTask) {
        this.backupTask = backupTask;
    }

    @Override
    public void onPlayerCommandPreprocess (PlayerChatEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        String[] split = command.split(" ");
        if (!event.isCancelled() && split[0].equalsIgnoreCase("/backup")) {
            event.setCancelled(true);
            if (Main.Permissions != null && !Main.Permissions.has(player, "backup.canbackup"))
                return;
            if (split.length == 1)
                backupTask.backup(null);
            else if (split.length == 2)
                backupTask.backup(split[1]);
            else
                player.sendMessage("/backup OPTIONALNAME");
        }
    }
}
