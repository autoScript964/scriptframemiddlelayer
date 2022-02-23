package com.script.utils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * The util class for compression and decompression.
 * Created by songql on 2015/7/24.
 *
 * Example code (need try/catch):
 *    Zip.zip(new File("D:\\outZipFileName.zip"), "abc", new File("D:\\srcFileName"), new File("D:\\srcDirectoryName"));
 *    Zip.zip(new File("D:\\outZipFileName.zip"), "/", new File("D:\\srcFileName"), new File("D:\\srcDirectoryName"));
 *    Zip.unzip(new File("D:\\srcZipFileName.zip"), new File("D:\\targetDirectory"));
 *    Zip.list(new File("D:\\srcZipFileName.zip"));
 */
public class Zip {

    /**
     * Compress the files.
     * @param zipFile the output zip file.
     * @param base the relative path of the zip file.
     * @param srcFiles the files/directories for compression.
     * @throws IOException
     */
    public static void zip(File zipFile, String base, File... srcFiles) throws IOException {
        if (base == null || base.equals("/")) {
            base = "";
        }
        else if (base.length() > 0 && !base.endsWith("/")) {
            base += "/";
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        zip(out, base, srcFiles);
        out.close();
    }

    /**
     * Compress all files in the directory without root directory.
     * @param zipFile the output zip file.
     * @param srcDirectory the directory for compression.
     * @throws IOException
     */
    public static void zip(File zipFile, File srcDirectory) throws IOException {
        if (!srcDirectory.exists()) {
            throw new IOException("The directory is not exists: " + srcDirectory.getName());
        }
        if (!srcDirectory.isDirectory()) {
            throw new IOException("The file is not a directory: " + srcDirectory.getName());
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        File[] files = srcDirectory.listFiles();
        for (File file : files) {
            zip(out, "", file);
        }
        out.close();
    }

    /**
     * The recursive method to compress the file and directory.
     * @param out the output stream of zip file.
     * @param base the relative path of the zip file.
     * @param srcFiles the files/directories for compression.
     * @throws IOException
     */
    private static void zip(ZipOutputStream out, String base, File... srcFiles) throws IOException {
        for (File srcFile : srcFiles) {
            if (!srcFile.exists()) {
                throw new IOException("The file is not exists: " + srcFile.getName());
            }
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                String srcPath = srcFile.getName();
                zip(out, base + srcPath + "/", files);
            }
            else {
                FileInputStream in = new FileInputStream(srcFile);
                out.putNextEntry(new ZipEntry(base + srcFile.getName()));
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        }
    }

    /**
     * Decompress the zip file.
     * @param zipFile the source zip file.
     * @param descDir the directory of decompress destination.
     * @throws IOException
     */
    public static void unzip(File zipFile, File descDir) throws IOException {
        if (!zipFile.exists()) {
            throw new IOException("The file is not exists: " + zipFile.getName());
        }
        if(!descDir.exists()){
            descDir.mkdirs();
        }
        if (!descDir.isDirectory()) {
            throw new IOException("The destination is exists and not a directory.");
        }
        ZipFile zip = new ZipFile(zipFile);
        for(Enumeration entries = zip.entries();entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            InputStream in = zip.getInputStream(entry);

            String outPath = (descDir.getAbsolutePath() + "/" + entry.getName());
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if(!file.exists()){
                file.mkdirs();
            }

            OutputStream out = new FileOutputStream(outPath);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            in.close();
            out.close();
        }
    }

    /**
     * Get the file list of zip file.
     * @param zipFile the source zip file.
     * @return the file list.
     * @throws IOException
     */
    public static String[] list(File zipFile) throws IOException {
        if (!zipFile.exists()) {
            throw new IOException("The file is not exists: " + zipFile.getName());
        }
        ZipFile zip = new ZipFile(zipFile);

        String[] list = new String[zip.size()];
        int i = 0;

        for(Enumeration entries = zip.entries();entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            list[i] = (entry.getName());
            i++;
        }
        return list;
    }
}