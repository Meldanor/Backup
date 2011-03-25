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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 *
 * @author Kilian Gaertner
 */
public class PropertiesSystem {

    /** the index for the intervall for save intervall */
    public final static int BACKUP_INTERVALL = 0;
    /** the index for the maximum count of backups */
    public final static int MAX_BACKUPS      = 1;

    /** How big is the int value array*/
    private final int INT_VALUES_SIZE        = 2;

    /** Stores every property*/
    private int[] intValues = new int[INT_VALUES_SIZE];

    /**
     * When constructed all properties are loaded. When no config.ini exists, the
     * default values are used
     */
    public PropertiesSystem() {
        File configFile = new File("plugins/Backup/config.ini");
        if (!configFile.exists()) {
            System.out.println("[Backup] couldn't find the config, create a default one!");
            createDefaultSettings(configFile);
        }
        loadProperties(configFile);
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
            while((line = bReader.readLine()) != null) {
                bWriter.write(line);
                bWriter.newLine();
            }
        }
        catch(Exception e) {
            e.printStackTrace(System.out);
        }
        // so we can be sure, that the streams are really closed
        finally{
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
    private void loadProperties(File configFile) {
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new FileReader(configFile));
            String line = "";
            while((line = bReader.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                if (line.startsWith("BackupIntervall"))
                    // 20 ticks on a server are one second and this crossed with 60 are minutes
                    intValues[BACKUP_INTERVALL] = Integer.parseInt(line.substring(16)) * 20 * 60;
                else if (line.startsWith("MaximumBackups"))
                    intValues[MAX_BACKUPS] = Integer.parseInt(line.substring(15));
                
            }
        }
        catch(Exception e) {
            e.printStackTrace(System.out);
        }
        // so we can be sure, that the streams are really closed
        finally{
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
    public int getProperty(int property) {
        return intValues[property];
    }
}
