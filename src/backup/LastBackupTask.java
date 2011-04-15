/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package backup;

import org.bukkit.Server;

/**
 *
 * @author Meldanor
 */
public class LastBackupTask extends BackupTask {

    Server server = null;

    public LastBackupTask(Server server,PropertiesSystem pSystem) {
        super(server,pSystem);
        this.server = server;
    }

    @Override
    public void run() {
        if (server.getOnlinePlayers().length <= 0)
            super.backup(null);
    }



}
