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

import java.io.File;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

import static io.FileUtils.FILE_SEPARATOR;

/**
 *
 * @author Kilian Gaertner
 */
public class Main extends JavaPlugin implements PropertyConstants {

    public static PermissionHandler Permissions;

    private BackupTask run;

    @Override
    public void onDisable () {
    }

    @Override
    public void onEnable () {

        setupPermissions();

        File backupDir = new File("plugins".concat(FILE_SEPARATOR).concat("Backup"));
        if (!backupDir.exists())
            backupDir.mkdirs();
        backupDir = new File ("backups");
        if (!backupDir.exists())
            backupDir.mkdirs();
        backupDir = new File("backups".concat(FILE_SEPARATOR).concat("custom"));
        if (!backupDir.exists())
            backupDir.mkdirs();
        
        // load the properties
        PropertiesSystem pSystem = new PropertiesSystem();

        Server server = getServer();
        PluginManager pm = server.getPluginManager();

        // the backupTask, which backups the system every X minutes
        run = new BackupTask(server,pSystem);

        // for manuell backups
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new CommandListener(run,pSystem) , Priority.Normal, this);
        // start the backupTask, which will starts after X minutes and backup after X minutes
        int intervall = pSystem.getIntProperty(INT_BACKUP_INTERVALL);
        server.getScheduler().scheduleAsyncRepeatingTask(this, run,intervall,intervall);
        System.out.println(this.getDescription().getFullName() + " was sucessfully loaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args != null && args.length == 1)
            run.backup(args[0]);
        else
            run.backup(null);
        return true;
    }

    private void setupPermissions() {
      Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

      if (Permissions == null) {
          if (test != null) {
              Permissions = ((Permissions)test).getHandler();
          } else {
              this.getServer().getLogger().info("[Backup] Permission system not detected, defaulting to OP");
          }
      }
  }
}
