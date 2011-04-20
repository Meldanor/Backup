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

/**
 *
 * @author Kilian Gaertner
 */
public class LastBackupTask extends BackupTask {

    private Server server = null;

    public LastBackupTask(Server server, PropertiesSystem pSystem) {
        super(server, pSystem);
        this.server = server;
    }

    @Override
    public void run() {
        if (server.getOnlinePlayers().length <= 0) {
            System.out.println("[BACKUP] Start last backup. When this is done, the server will not run a backup until a player joins the server.");
            super.backup();
        }
    }
}
