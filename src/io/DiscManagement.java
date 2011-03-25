package io;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DiscManagement {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private final static String FILE_SEPARATOR = System.getProperty("file.separator");

    private final static int NULLSIZE = 89;

    private final static int FILE_BUFFER_SIZE = 100000;


    private DiscManagement() {
    }

    public static boolean copyDirectory(String from, String toPath, String toDirectory) {

        boolean retValue = false;

        StringBuilder fromBuf = new StringBuilder(NULLSIZE);
        StringBuilder toPathBuf = new StringBuilder(NULLSIZE);
        StringBuilder toBuf = new StringBuilder(NULLSIZE);

        try {
            File fdir = new File(from);
            if (!fdir.isDirectory()) {
                throw new IOException("The given Resource isn't a directory!" + LINE_SEPARATOR + from + LINE_SEPARATOR + toPath + LINE_SEPARATOR + toDirectory);
            }
            File dir = new File(toPath);
            if (!dir.isDirectory()) {
                throw new IOException("The toPath does not exist!" + from + LINE_SEPARATOR + toPath + LINE_SEPARATOR + toDirectory);
            }
            dir = new File(new StringBuffer().append(toPath).append(FILE_SEPARATOR).append(toDirectory).toString());
            if (dir.isFile()) {
                throw new IOException("The to directory already exists!" + LINE_SEPARATOR + from + LINE_SEPARATOR + toPath + LINE_SEPARATOR + toDirectory);
            }
            dir.mkdir();
            for (int i = 0; i < fdir.list().length; i++) {
                fromBuf.append(from);
                fromBuf.append(FILE_SEPARATOR);
                fromBuf.append(fdir.list()[i]);

                toPathBuf.append(toPath);
                toPathBuf.append(FILE_SEPARATOR);
                toPathBuf.append(toDirectory);

                toBuf.append(fdir.list()[i]);

                if (fdir.listFiles()[i].isDirectory()) {
                    retValue = copyDirectory(fromBuf.toString(), toPathBuf.toString(), toBuf.toString());
                } else {
                    retValue = copyFile(fromBuf.toString(), toPathBuf.toString(), toBuf.toString());
                }
                if (!retValue) {
                    throw new IOException("Error copying the Directory!" + LINE_SEPARATOR + from + LINE_SEPARATOR + toPath + LINE_SEPARATOR + toDirectory);
                }
                fromBuf.delete(0, fromBuf.length());
                toPathBuf.delete(0, toPathBuf.length());
                toBuf.delete(0, toBuf.length());
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            retValue = false;
        }
        return retValue;
    }

    public static boolean copyFile(String from, String toPath, String toFile) {
        FileOutputStream out = null;
        FileInputStream in = null;
        byte[] buffer = new byte[FILE_BUFFER_SIZE];
        boolean retVal = false;
        StringBuilder toFileBuf = new StringBuilder(NULLSIZE);
        try {
            File dir = new File(from);
            if (!dir.isFile()) {
                throw new IOException("The given Resource isn't a File!" + LINE_SEPARATOR + from + LINE_SEPARATOR + toPath + LINE_SEPARATOR + toFile);
            }
            dir = new File(toPath);
            if (!dir.isDirectory()) {
                throw new IOException("The toPath does not exist!" + LINE_SEPARATOR + from + LINE_SEPARATOR + toPath + LINE_SEPARATOR + toFile);
            }
            in = new FileInputStream(from);
            toFileBuf.append(toPath);
            toFileBuf.append(FILE_SEPARATOR);
            toFileBuf.append(toFile);
            out = new FileOutputStream(toFileBuf.toString());
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }
            retVal = true;
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
            retVal = false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
                retVal = false;
            }
        }
        return retVal;
    }

    public static boolean deleteDirectory(String path) {
        boolean retValue = false;
        StringBuilder delName = new StringBuilder(NULLSIZE);
        try {
            File dir = new File(path);
            if (dir.isFile()) {
                throw new IOException("The given path is a file!");
            }
            if (!dir.exists()) {
                throw new IOException("The given path doesn't exist!");
            }
            int depth = dir.list().length;
            for (int i = 0; i < depth; i++) {
                delName.append(path);
                delName.append(File.separator);
                delName.append(dir.list()[0]);
                if (dir.listFiles()[0].isDirectory()) {
                    retValue = deleteDirectory(delName.toString());
                } else {
                    retValue = deleteFile(delName.toString());
                }
                if (!retValue) {
                    throw new IOException("Error deleting the directory!");
                }
                delName.delete(0, delName.length());
            }
            retValue = dir.delete();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            retValue = false;
        }
        return retValue;
    }

    private static boolean deleteFile(String path) {
        boolean retVal = false;
        try {
            File dir = new File(path);
            if (dir.isDirectory()) {
                throw new IOException("The given path is a file!");
            }
            if (!dir.exists()) {
                throw new IOException("The given path doesn't exist!");
            }
            retVal = dir.delete();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            retVal = false;
        }
        return retVal;
    }

    public static void zipDirectory(String directoryName, String targetName) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetName+".zip"));
            DiscManagement.zipDir(directoryName, zos) ;
            zos.close();

        }
        catch(Exception e) {
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
}