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

/**
 *
 * @author Kilian Gaertner
 */
public interface PropertyConstants {

    /** the index for the intervall for save intervall */
    public final int INT_BACKUP_INTERVALL       = 0;
    /** the index for the maximum count of backups */
    public final int INT_MAX_BACKUPS            = 1;
//----------------------------------------------------------------------------//
    /** the index for the only ops can run manuell backups property*/
    public final int BOOL_ONLY_OPS              = 0;
    /** the index for the only run a backup when player are online property*/
    public final int BOOL_BACKUP_ONLY_PLAYER    = 1;
    /** the index whether the server has to zip or just store property */
    public final int BOOL_ZIP                   = 2;
    /** Enable the autosave function after the backup.*/
    public final int BOOL_ACTIVATE_AUTOSAVE     = 3;
    /** Store the plugin folder also in the backup */
    public final int BOOL_BACKUP_PLUGINS        = 4;
    /** Summarice all worlds in one archive/folder or in seperate one */
    public final int BOOL_SUMMARIZE_CONTENT     = 5;
//----------------------------------------------------------------------------//
    /** the index for the starting backup message */
    public final int STRING_START_BACKUP_MESSAGE    = 0;
    /** the index for the starting backup message */
    public final int STRING_FINISH_BACKUP_MESSAGE   = 1;
    /** the index for the world that shouldn't get backuped.
     *  This is one line and each world name is seperated by a ; , so you have
     * to split them firstly.
     */
    public final int STRING_NO_BACKUP_WORLDNAMES    = 2;
    /** the index for the custom date format string (optional) */
    public final int STRING_CUSTOM_DATE_FORMAT      = 3;
    /** the folder where the backups are stored */
    public final int STRING_BACKUP_FOLDER           = 4;
}
