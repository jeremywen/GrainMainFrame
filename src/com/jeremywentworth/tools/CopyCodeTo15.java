/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Jeremy Wentworth
 */
public class CopyCodeTo15 {

    public CopyCodeTo15() {
        try {

            //TODO copy build.xml



            /**********************
             * code
             **********************/
            File jeremywentworthFolder = new File("src/com/jeremywentworth");
            Collection<File> javaFiles = FileUtils.listFiles(jeremywentworthFolder, new String[]{"java"}, true);
            System.out.println("listFiles.size() = " + javaFiles.size());

            for (File javaFile : javaFiles) {

                if (isNotSvn(javaFile)
                        && !javaFile.getParentFile().getName().equals("vst")
                        && !javaFile.getName().equals("CopyCodeTo15.java")) {
                    System.out.println("read " + javaFile.length() + "bytes " + javaFile);

                    BufferedReader reader = new BufferedReader(new FileReader(javaFile));
                    File outFile = new File(javaFile.getAbsolutePath().replaceFirst("Frame", "Frame1.5"));
                    makeDirs(outFile.getParentFile());
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("@Override")
                                || line.contains("Ignore in Java 1.5 Version")) {
                            continue;

                        } else {
                            writer.write(line);
                            writer.write("\n");

                        }
                    }

                    reader.close();
                    writer.close();
                    System.out.println("write " + outFile.length() + "bytes " + outFile);

                }
            }

            /**********************
             * JARS
             **********************/
            File libFolderSource = new File("lib");
            Collection<File> jarFiles = FileUtils.listFiles(libFolderSource, new String[]{"jar"}, true);
            for (File jarFile : jarFiles) {
                File jarFileDest = new File(jarFile.getAbsolutePath().replaceFirst("Frame", "Frame1.5"));
                if (jarFileDest.exists()) {
                    jarFileDest.delete();
                }
                FileUtils.copyFile(jarFile, jarFileDest);
            }

            /**********************
             * help
             **********************/
            File helpFileSource = new File("Help.txt");
            File helpFileDest = new File(helpFileSource.getAbsolutePath().replaceFirst("Frame", "Frame1.5"));
            if (helpFileDest.exists()) {
                helpFileDest.delete();
            }
            FileUtils.copyFile(helpFileSource, helpFileDest);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void makeDirs(File file) {
        if (file.exists()) {
            return;
        }
        if (file.mkdirs()) {
            System.out.println("made dir = " + file);
        } else {
            System.out.println("ERROR making dir = " + file);
        }
    }

    private boolean isNotSvn(File file) {
        return !file.getPath().contains(".svn");
    }

    public static void main(String[] args) {
        new CopyCodeTo15();
    }
}
