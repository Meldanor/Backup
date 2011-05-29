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

import java.io.IOException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.Server;
import io.FileUtils;
import java.util.Calendar;
import backup.PropertiesSystem;
import backup.PropertyConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static io.FileUtils.FILE_SEPARATOR;

/**
 * The Task copies and backups the worlds and delete older backups. This task
 * is only runes once in backup and doing all the thread safe options.
 * The PrepareBackupTask and BackupTask are two threads to find a compromiss between
 * security and performance.
 * @author Kilian Gaertner
 */
public class BackupTask implements Runnable, PropertyConstants {

    private final PropertiesSystem pSystem;
    private final LinkedList<String> worldsToBackup;
    private final Server server;
    private final String backupName;

    private static long lastBackupTime = -1;

    public BackupTask (PropertiesSystem pSystem, LinkedList<String> worldsToBackup, Server server, String backupName) {
        this.pSystem = pSystem;
        this.worldsToBackup = worldsToBackup;
        this.server = server;
        this.backupName = backupName;
    }

    @Override
    public void run () {
        try {
            backup();
        }
        catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

    public void backup() throws Exception {

        if (pSystem.getBooleanProperty(BOOL_SUMMARIZE_CONTENT)) {
            String backupDirName = pSystem.getStringProperty(STRING_BACKUP_FOLDER).concat(FILE_SEPARATOR);
            if (backupName != null)
                backupDirName = backupDirName.concat("custom").concat(FILE_SEPARATOR).concat(backupName);
            else
                backupDirName = backupDirName.concat(getDate());
            File backupDir = new File(backupDirName);
            backupDir.mkdir();
            while (!worldsToBackup.isEmpty()) {
                String worldName = worldsToBackup.removeFirst();
                try {
                    FileUtils.copyDirectory(worldName, backupDirName.concat(FILE_SEPARATOR).concat(worldName));
                }
                catch (IOException e) {
                    System.out.println("[BACKUP] An error occurs while creating a temporary copy of world ".concat(worldName).concat(". Maybe the complete world isn' backuped, please take a look at it!"));
                    e.printStackTrace(System.out);
                    server.broadcastMessage("[BACKUP] An error occurs while backup. Please report an admin!");
                }
            }
            if (pSystem.getBooleanProperty(BOOL_BACKUP_PLUGINS))
                FileUtils.copyDirectory("plugins", backupDirName.concat(FILE_SEPARATOR).concat("plugins"));

            if (pSystem.getBooleanProperty(BOOL_ZIP)) {
                FileUtils.zipDir(backupDirName, backupDirName);
                FileUtils.deleteDirectory(backupDir);
            }
        }
        else {
            String backupDirName = pSystem.getStringProperty(STRING_BACKUP_FOLDER).concat(FILE_SEPARATOR);
            File backupDir = new File(backupDirName);
            backupDir.mkdir();
            boolean zip = pSystem.getBooleanProperty(BOOL_ZIP);
            while (!worldsToBackup.isEmpty()) {
                String worldName = worldsToBackup.removeFirst();
                String destDir = backupDirName.concat(FILE_SEPARATOR).concat(worldName).concat("-").concat(getDate());
                FileUtils.copyDirectory(worldName, destDir);
                if (zip) {
                    FileUtils.zipDir(destDir, destDir);
                    FileUtils.deleteDirectory(new File(destDir));
                }
            }
            if (pSystem.getBooleanProperty(BOOL_BACKUP_PLUGINS)) {
                String destDir = backupDirName.concat(FILE_SEPARATOR).concat("plugins").concat("-").concat(getDate());
                FileUtils.copyDirectory("plugins", destDir);
                if (zip) {
                    FileUtils.zipDir(destDir, destDir);
                    FileUtils.deleteDirectory(new File(destDir));
                }
            }
        }
        deleteOldBackups ();
        finish();
    }

    /**
     * @return String representing the current Date in configured format
     */
    private String getDate () {

        Calendar cal = Calendar.getInstance();
        String formattedDate = new String();
        // Java string (and date) formatting:
        // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#syntax
        try {
            formattedDate = String.format(pSystem.getStringProperty(STRING_CUSTOM_DATE_FORMAT),cal);
        }
        catch (Exception e) {
            System.out.println("[BACKUP] Error formatting date, bad format string! Formatting date with default format string...");
            formattedDate = String.format("%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS", cal);
            System.out.println(e);
        }
        return formattedDate;
    }

    /**
     * Check whethere there are more backups as allowed to store. When this case
     * is true, it deletes oldest ones
     */
    private void deleteOldBackups () {
        try {
            //
            File backupDir = new File(pSystem.getStringProperty(STRING_BACKUP_FOLDER));
            // get every zip file in the backup Dir
            File[] tempArray = backupDir.listFiles();
            final int maxBackups = pSystem.getIntProperty(INT_MAX_BACKUPS);
            // when are more backups existing as allowed as to store
            if (tempArray.length > maxBackups) {
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
                for (int i = 0 ; i < maxBackups ; ++i) {
                    maxModifiedIndex = 0;
                    maxModified = backups.get(0).lastModified();
                    for (int j = 1 ; j < backups.size() ; ++j) {
                        File currentFile = backups.get(j);
                        if (currentFile.lastModified() > maxModified) {
                            maxModified = currentFile.lastModified();
                            maxModifiedIndex = j;
                        }
                    }
                    backups.remove(maxModifiedIndex);
                }
                System.out.println("[BACKUP] Removing the following backups due to age:");
                System.out.println(Arrays.toString(backups.toArray()));
                // this are the oldest backups, so delete them
                for (File backupToDelete : backups)
                    backupToDelete.delete();
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Creates a temporary Runnable that is running on the main thread by the
     * sheduler to prevent thread problems.
     */
    private void finish() {
        Runnable run = new Runnable() {
            @Override
            public void run () {
                if (pSystem.getBooleanProperty(BOOL_ACTIVATE_AUTOSAVE))
                    server.dispatchCommand(new ConsoleCommandSender(server), "save-on");
                String completedBackupMessage = pSystem.getStringProperty(STRING_FINISH_BACKUP_MESSAGE);
                if (completedBackupMessage != null && !completedBackupMessage.trim().isEmpty()) {
                    server.broadcastMessage(completedBackupMessage);
                    System.out.println(completedBackupMessage);
                }
                lastBackupTime = System.currentTimeMillis();
            }
        };
        server.getScheduler().scheduleSyncDelayedTask(server.getPluginManager().getPlugin("Backup"), run);
    }

    public static long getLastBackup() {
        long time = System.currentTimeMillis() - lastBackupTime;
        return time;
    }

    public static void initateTimer() {
        if (lastBackupTime == -1)
            lastBackupTime = System.currentTimeMillis();
        else
            throw new RuntimeException("The timer was already initiallized!");
    }
}
