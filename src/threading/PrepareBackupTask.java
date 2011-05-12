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

package threading;

import backup.PropertiesSystem;
import backup.PropertyConstants;
import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;

/**
 * This task is running by a syncronized thread from the sheduler. It prepare
 * everything for the BackupTask. It checks, whether it can run a backup now,
 * stop the autosave, make a server wide save of all player, save all world data
 * from the RAM to the disc and collects finnaly all worlds and directories to
 * backup. If this is done, it create an asyncronized thread, the BackupTask.
 * @author Kilian Gaertner
 * @see BackupTask
 */
public class PrepareBackupTask implements Runnable, PropertyConstants{

    // The server where the Task is running
    private final Server server;
    private final PropertiesSystem pSystem;
    private String backupName;
    private boolean isManuelBackup;

    /**
     * The only constructur for the BackupTask.
     * @param server The server where the Task is running on
     * @param pSystem This must be a loaded PropertiesSystem
     */
    public PrepareBackupTask (Server server, PropertiesSystem pSystem) {
        this.server = server;
        this.pSystem = pSystem;
    }

    @Override
    public void run () {
        boolean backupOnlyWithPlayer = pSystem.getBooleanProperty(BOOL_BACKUP_ONLY_PLAYER);
        if ((backupOnlyWithPlayer && server.getOnlinePlayers().length > 0)
                || !backupOnlyWithPlayer
                || isManuelBackup
                || backupName != null)
            prepareBackup();
        else
            System.out.println("[BACKUP] Scheduled backup was aborted due to lack of players. Next backup attempt in " + pSystem.getIntProperty(INT_BACKUP_INTERVALL) / 1200 + " minutes.");
    }

    protected void prepareBackup() {

        // start broadcast informing the players about the backup
        String startBackupMessage = pSystem.getStringProperty(STRING_START_BACKUP_MESSAGE);
        if (startBackupMessage != null && !startBackupMessage.trim().isEmpty()) {
            System.out.println(startBackupMessage);
            server.broadcastMessage(startBackupMessage);
        }

        // a hack like methode to send the console command for disabling every world save
        ConsoleCommandSender ccs = new ConsoleCommandSender(server);
        server.dispatchCommand(ccs, "save-all");
        server.dispatchCommand(ccs, "save-off");

        // the Player Position are getting stored
        server.savePlayers();

        // get the names of the worlds which shall not backuped
        String[] ignoredWorlds = getToIgnoreWorlds();
        LinkedList<String> worldsToBackup = new LinkedList<String>();

        // shall the backups stored and compress via ZIP?
        boolean hasToZIP = pSystem.getBooleanProperty(BOOL_ZIP);
            if (!hasToZIP)
                // send a hint, because this shall not be the normal case!
                System.out.println("[BACKUP] Backup compression is disabled.");

        // iterate through all worlds and filter the one, that shall get backuped!
        outer:
        for (World world : server.getWorlds()) {
            String worldName = world.getName();
            for (String ignoredWorldName : ignoredWorlds) {
                if (ignoredWorldName.equalsIgnoreCase(worldName))
                    continue outer;
            }
            worldsToBackup.add(worldName);
            world.save();
        }
        server.getScheduler().scheduleAsyncDelayedTask(server.getPluginManager().getPlugin("Backup"), new BackupTask(pSystem,worldsToBackup,server,backupName));
        backupName = null;
        isManuelBackup = false;
    }

    private String[] getToIgnoreWorlds () {
        String[] worldNames = pSystem.getStringProperty(STRING_NO_BACKUP_WORLDNAMES).split(";");
        if (worldNames.length > 0 && !worldNames[0].isEmpty()) {
            System.out.println("[BACKUP] Backup is disabled for the following world(s):");
            System.out.println(Arrays.toString(worldNames));
        }
        return worldNames;
    }
    
    public void setBackupName (String backupName) {
        this.backupName = backupName;
    }

    public void setAsManuelBackup () {
        this.isManuelBackup = true;
    }
}
