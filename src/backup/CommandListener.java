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

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * For manual backups
 * @author Kilian Gaertner
 */
public class CommandListener extends PlayerListener {

    private Runnable backupTask = null;

    public CommandListener(Runnable backupTask) {
        this.backupTask = backupTask;
    }

    @Override
    public void onPlayerCommandPreprocess (PlayerChatEvent event) {
        String command = event.getMessage();
        if (!event.isCancelled() && command.startsWith("/backup")) {
            event.setCancelled(true);
            if (Main.Permissions != null && !Main.Permissions.has(event.getPlayer(), "backup.canbackup"))
                return;

            backupTask.run();
        }
    }
}
