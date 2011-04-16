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
import org.bukkit.World;
import io.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import org.bukkit.command.ConsoleCommandSender;

import static io.FileUtils.FILE_SEPARATOR;

/**
 * The BackupTask implements the Interface Runnable for getting executed by the
 * Server Scheduler. The implemented function run backups the system.
 * @author Kilian Gaertner
 */
public class BackupTask implements Runnable, PropertyConstants {

    // The server where the Task is running
    private Server server = null;

    // How many Backups can exists at one time
    private final int MAX_BACKUPS;

    private final PropertiesSystem pSystem;

    private String backupName;

    private boolean isManuelBackup;

    /**
     * The only constructur for the BackupTask.
     * @param server The server where the Task is running on
     * @param pSystem This must be a loaded PropertiesSystem
     */
    public BackupTask(Server server,PropertiesSystem pSystem) {
        this.server = server;
        this.pSystem = pSystem;
        MAX_BACKUPS = pSystem.getIntProperty(INT_MAX_BACKUPS);
    }

    /**
     * The implemented function. It starts the backup of the server
     */
    @Override
    public void run () {
        boolean backupOnlyWithPlayer = pSystem.getBooleanProperty(BOOL_BACKUP_ONLY_PLAYER);
        if ((backupOnlyWithPlayer && server.getOnlinePlayers().length > 0) ||
            !backupOnlyWithPlayer ||
            isManuelBackup ||
            backupName != null)
            backup();
        else {
            System.out.println("[BACKUP] The server skip backup, because no player are online!");
        }
    }

    /**
     * Backups the complete server. At first messages were sent to the console
     * and to every player, so everyone know, a Backup is running.
     * After this it deactivates all world saves and then saves every player position.
     * Is this done, every world getting zipped and stored.
     * 
     */
    protected void backup() {

        // the messages
        String startBackupMessage = pSystem.getStringProperty(STRING_START_BACKUP_MESSAGE);
        System.out.println(startBackupMessage);
        server.broadcastMessage(startBackupMessage);
        // a hack like methode to send the console command for disabling every world save
        ConsoleCommandSender ccs = new ConsoleCommandSender(server);
        server.dispatchCommand(ccs, "save-all");
        server.dispatchCommand(ccs, "save-off");
        // the Player Position are getting stored
        server.savePlayers();

        String[] worldNames = pSystem.getStringProperty(STRING_NO_BACKUP_WORLDNAMES).split(";");
        if (worldNames.length > 0 && !worldNames[0].isEmpty()) {
            System.out.println("[BACKUP] Skip the followning worlds :");
            System.out.println(Arrays.toString(worldNames));
        }
        try {
            // iterate through every world and zip every one
            boolean hasToZIP = pSystem.getBooleanProperty(BOOL_ZIP);
            if (hasToZIP)
                System.out.println("[BACKUP] Zipping backup is disabled!");
            outter :
            for (World world : server.getWorlds()) {
                inner :
                for(String worldName : worldNames)
                    if (worldName.equalsIgnoreCase(world.getName()))
                        continue outter;
                String backupDir = "backups".concat(FILE_SEPARATOR).concat(world.getName());
                if (!hasToZIP)
                    backupDir = backupDir.concat(this.getDate());
                // save every information from the RAM into the HDD
                world.save();
                // make a temporary dir of the world
                FileUtils.copyDirectory(new File(world.getName()), new File(backupDir));
                // zip the temporary dir
                String targetName = world.getName();
                String targetDir = "backups".concat(FILE_SEPARATOR);

                if (backupName != null) {
                    targetName = backupName;
                    targetDir = targetDir.concat("custom").concat(FILE_SEPARATOR);
                }
                if (hasToZIP) {
                    FileUtils.zipDirectory(backupDir, targetDir.concat(targetName).concat(getDate()));
                    // delete the temporary dir
                     FileUtils.deleteDirectory(new File(backupDir));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        // enable the world save
        server.dispatchCommand(ccs, "save-on");
        // the messages
        String completedBackupMessage = pSystem.getStringProperty(STRING_FINISH_BACKUP_MESSAGE);
        server.broadcastMessage(completedBackupMessage);
        System.out.println(completedBackupMessage);
        // check whether there are old backups to delete
        deleteOldBackups();
        backupName = null;
        isManuelBackup = false;
    }

    /**
     * @return String representing the current Date in the format
     * <br> DAY MONTH YEAR-HOUR MINUTE SECOND
     */
    private String getDate() {
        StringBuilder sBuilder = new StringBuilder();
        Calendar cal = Calendar.getInstance();
        sBuilder.append(cal.get(Calendar.DAY_OF_MONTH));

        int month = cal.get(Calendar.MONTH) + 1;
        if (month < 10)
            sBuilder.append("0");
        sBuilder.append(month);

        sBuilder.append(cal.get(Calendar.YEAR));
        sBuilder.append("-");

        int hours = cal.get(Calendar.HOUR_OF_DAY);
        if (hours < 10)
            sBuilder.append("0");
        sBuilder.append(hours);
        int minutes = cal.get(Calendar.MINUTE);
        if (minutes < 10)
            sBuilder.append("0");
        sBuilder.append(minutes);
        int seconds = cal.get(Calendar.SECOND);
        if (seconds < 10)
            sBuilder.append("0");
        sBuilder.append(seconds);
        return sBuilder.toString();
    }

    /**
     * Check whethere there are more backups as allowed to store. When this case
     * is true, it deletes oldest ones
     */
    private void deleteOldBackups () {
        try {
            //
            File backupDir = new File("backups");
            // get every zip file in the backup Dir
            File[] tempArray = backupDir.listFiles();
            // when are more backups existing as allowed as to store
            if (tempArray.length > MAX_BACKUPS) {
                System.out.println("Delete old backups");

                // Store the to delete backups in a list
                ArrayList<File> backups = new ArrayList<File>(tempArray.length);
                // For this add all backups in the list and remove later the newest ones
                backups.addAll(Arrays.asList(tempArray));

                // the current index of the newest backup
                int maxModifiedIndex;
                // the current time of the newest backup
                long maxModified;

                //remove all newest backups from the list to delete
                for(int i = 0 ; i < MAX_BACKUPS ; ++i) {
                    maxModifiedIndex = 0;
                    maxModified = backups.get(0).lastModified();
                    for(int j = 1 ; j < backups.size(); ++j) {
                        File currentFile = backups.get(j);
                        if (currentFile.lastModified() > maxModified) {
                            maxModified = currentFile.lastModified();
                            maxModifiedIndex = j;
                        }
                    }
                    backups.remove(maxModifiedIndex);
                }
                // this are the oldest backups, so delete them
                for(File backupToDelete : backups)
                    backupToDelete.delete();
            }
        }
        catch(Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void setBackupName(String backupName) {
        this.backupName = backupName;
    }

    public void setAsManuelBackup() {
        this.isManuelBackup = true;
    }
}
