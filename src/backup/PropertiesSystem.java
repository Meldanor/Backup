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

import org.bukkit.plugin.Plugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import static io.FileUtils.FILE_SEPARATOR;

/**
 *
 * @author Kilian Gaertner
 */
public class PropertiesSystem implements PropertyConstants {

    /** How big is the int value array*/
    private final int INT_VALUES_SIZE       = 2;
    private final int BOOL_VALUES_SIZE      = 5;
    private final int STRING_VALUES_SIZE    = 4;
    /** Stores every int property*/
    private int[] intValues = new int[INT_VALUES_SIZE];
    /** Stores every bool property*/
    private boolean[] boolValues = new boolean[BOOL_VALUES_SIZE];
    /** Stores every string property */
    private String[] stringValues = new String[STRING_VALUES_SIZE];

    /**
     * When constructed all properties are loaded. When no config.ini exists, the
     * default values are used
     */
    public PropertiesSystem (Plugin plugin) {
        StringBuilder sBuilder = new StringBuilder("plugins");
        sBuilder.append(FILE_SEPARATOR);
        sBuilder.append("Backup");
        sBuilder.append(FILE_SEPARATOR);
        sBuilder.append("config.ini");
        File configFile = new File(sBuilder.toString());
        if (!configFile.exists()) {
            System.out.println("[Backup] couldn't find the config, create a default one!");
            createDefaultSettings(configFile);
        }
        loadProperties(configFile, plugin);
    }

    /**
     * Load the default configs from the config.ini , stored in the jar
     * @param configFile The configFile, but not in the jar
     */
    private void createDefaultSettings (File configFile) {
        BufferedReader bReader = null;
        BufferedWriter bWriter = null;
        try {
            // open a stream to the config.ini in the jar, because we can only accecs
            // over the class loader
            bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/res/config.ini")));
            String line = "";
            bWriter = new BufferedWriter(new FileWriter(configFile));
            // copy the content
            while ((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        } // so we can be sure, that the streams are really closed
        finally {
            try {
                if (bReader != null)
                    bReader.close();
                if (bWriter != null)
                    bWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Load the properties from the config.ini
     * @param configFile The config.ini in the servers dir
     */
    private void loadProperties (File configFile, Plugin plugin) {
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new FileReader(configFile));
            String line = "";
            String version = null;
            while ((line = bReader.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                String[] split = line.split("=");
                //------------------------------------------------------------//
                if (split[0].equals("BackupIntervall"))
                    intValues[INT_BACKUP_INTERVALL] = Integer.parseInt(split[1]) * 20 * 60;
                else if (split[0].equals("MaximumBackups"))
                    intValues[INT_MAX_BACKUPS] = Integer.parseInt(split[1]);
                else if (split[0].equals("OnlyOps"))
                //------------------------------------------------------------//
                    boolValues[BOOL_ONLY_OPS] = Boolean.parseBoolean(split[1]);
                else if (split[0].equals("BackupOnlyWithPlayer"))
                    boolValues[BOOL_BACKUP_ONLY_PLAYER] = Boolean.parseBoolean(split[1]);                
                else if (split[0].equals("ZIPBackup"))
                    boolValues[BOOL_ZIP] = Boolean.parseBoolean(split[1]);
                else if (split[0].equals("EnableAutoSave"))
                    boolValues[BOOL_ACTIVATE_AUTOSAVE] = Boolean.parseBoolean(split[1]);
                else if (split[0].equals("BackupPluginDIR"))
                    boolValues[BOOL_BACKUP_PLUGINS] = Boolean.parseBoolean(split[1]);
                //------------------------------------------------------------//
                else if (split[0].equals("MessageStartBackup"))
                    stringValues[STRING_START_BACKUP_MESSAGE] = split[1];
                else if (split[0].equals("MessageFinishBackup"))
                    stringValues[STRING_FINISH_BACKUP_MESSAGE] = split[1];
                else if (split[0].equals("DontBackupWorlds")) {
                    stringValues[STRING_NO_BACKUP_WORLDNAMES] = "";
                    if (split.length == 2)
                        stringValues[STRING_NO_BACKUP_WORLDNAMES] = split[1];
                }
                else if (split[0].equals("CustomDateFormat"))
                    stringValues[STRING_CUSTOM_DATE_FORMAT] = split[1];
//----------------------------------------------------------------------------//
                else if (split[0].equals("Version"))
                    version = split[1];
            }
            if (version == null || !version.equals(plugin.getDescription().getVersion()))
                System.out.println("[BACKUP] Your config file is outdated! Please delete your config.ini and the newest will be created!");
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        } // so we can be sure, that the streams are really closed
        finally {
            try {
                if (bReader != null)
                    bReader.close();
            }
            catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Get a value of the integer stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public int getIntProperty (int property) {
        return intValues[property];
    }

    /**
     * Get a value of the boolean stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public boolean getBooleanProperty (int property) {
        return boolValues[property];
    }

    /**
     * Get a value of the string stored properties
     * @param property see the constants of PropertiesSystem
     * @return The value of the propertie
     */
    public String getStringProperty (int property) {
        return stringValues[property];
    }
}
