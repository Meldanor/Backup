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
import io.DiscManagement;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import org.bukkit.craftbukkit.CraftServer;

/**
 * The BackupTask implements the Interface Runnable for getting executed by the
 * Server Scheduler. The implemented function run backups the system.
 * @author Kilian Gaertner
 */
public class BackupTask implements Runnable {

    // The server where the Task is running
    private Server server = null;

    // How many Backups can exists at one time
    private final int MAX_BACKUPS;

    /**
     * The only constructur for the BackupTask.
     * @param server The server where the Task is running on
     * @param pSystem This must be a loaded PropertiesSystem
     */
    public BackupTask(Server server,PropertiesSystem pSystem) {
        this.server = server;
        MAX_BACKUPS = pSystem.getIntProperty(PropertiesSystem.INT_MAX_BACKUPS);
    }

    /**
     * The implemented function. It starts the backup of the server
     */
    @Override
    public void run () {
        backup(null);
    }

    /**
     * Backups the complete server. At first messages were sent to the console
     * and to every player, so everyone know, a Backup is running.
     * After this it deactivates all world saves and then saves every player position.
     * Is this done, every world getting zipped and stored.
     * 
     */
    public void backup(String backupName) {

        // the messages
        System.out.println("Start backup");
        server.broadcastMessage("Start backup");
        // a hack like methode to send the console command for disabling every world save
        server.dispatchCommand(((CraftServer)server).getServer().console, "save-off");
        // the Player Position are getting stored
        server.savePlayers();

        // iterate through every world and zip every one
        for (World world : server.getWorlds()) {

            String backupDir = "backups".concat(DiscManagement.FILE_SEPARATOR).concat(world.getName());
            // save every information from the RAM into the HDD
            world.save();
            // make a temporary dir of the world
            DiscManagement.copyDirectory(world.getName(), new File("").getAbsolutePath(), backupDir);
            // zip the temporary dir
            String targetName = world.getName();
            String targetDir = "backups".concat(DiscManagement.FILE_SEPARATOR);

            if (backupName != null) {
                targetName = backupName;
                targetDir = targetDir.concat("custom").concat(DiscManagement.FILE_SEPARATOR);
            }

            DiscManagement.zipDirectory(backupDir, targetDir.concat(targetName).concat(getDate()));
            // delete the temporary dir
            DiscManagement.deleteDirectory(backupDir);
        }
        // enable the world save
        server.dispatchCommand(((CraftServer)server).getServer().console, "save-on");
        // the messages
        server.broadcastMessage("Finished backup");
        System.out.println("Finished backup");
        // check whether there are old backups to delete
        deleteOldBackups();
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
}
