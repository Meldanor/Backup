
package io;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DiscManagement {

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");
    private final static int NULLSIZE = 89;

    private DiscManagement () {
    }

    public static boolean deleteDirectory (String path) {
        boolean retValue = false;
        StringBuilder delName = new StringBuilder(NULLSIZE);
        try {
            File dir = new File(path);
            if (dir.isFile())
                throw new IOException("The given path is a file!");
            if (!dir.exists())
                throw new IOException("The given path doesn't exist!");
            int depth = dir.list().length;
            for (int i = 0 ; i < depth ; i++) {
                delName.append(path);
                delName.append(File.separator);
                delName.append(dir.list()[0]);
                if (dir.listFiles()[0].isDirectory())
                    retValue = deleteDirectory(delName.toString());
                else
                    retValue = deleteFile(delName.toString());
                if (!retValue)
                    throw new IOException("Error deleting the directory!");
                delName.delete(0, delName.length());
            }
            retValue = dir.delete();
        }
        catch (IOException ex) {
            ex.printStackTrace(System.out);
            retValue = false;
        }
        return retValue;
    }

    private static boolean deleteFile (String path) {
        boolean retVal = false;
        try {
            File dir = new File(path);
            if (dir.isDirectory())
                throw new IOException("The given path is a file!");
            if (!dir.exists())
                throw new IOException("The given path doesn't exist!");
            retVal = dir.delete();
        }
        catch (IOException ex) {
            ex.printStackTrace(System.out);
            retVal = false;
        }
        return retVal;
    }

    public static void zipDirectory (String directoryName, String targetName) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetName + ".zip"));
            DiscManagement.zipDir(directoryName, zos);
            zos.close();

        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static void zipDir (String dir2zip, ZipOutputStream zos) {
        try {
            File zipDir = new File(dir2zip);

            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;
            for (int i = 0 ; i < dirList.length ; i++) {
                File f = new File(zipDir, dirList[i]);
                if (f.isDirectory()) {
                    zipDir(f.getPath(), zos);
                    continue;
                }
                FileInputStream fis = new FileInputStream(f);
                ZipEntry anEntry = new ZipEntry(f.getPath().substring(8));
                zos.putNextEntry(anEntry);
                while ((bytesIn = fis.read(readBuffer)) != -1)
                    zos.write(readBuffer, 0, bytesIn);
                fis.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void copyDirectory (File srcPath, File dstPath) {
        try {
            if (srcPath.isDirectory()) {
                if (!dstPath.exists())
                    dstPath.mkdir();
                String files[] = srcPath.list();
                for (int i = 0 ; i < files.length ; i++) {
                    copyDirectory(new File(srcPath, files[i]),
                            new File(dstPath, files[i]));
                }
            }
            else if (!srcPath.exists()) {
            }
            else {

                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

                in.close();
                out.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }
}
