/**
 *
 */
package semano.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Nadejda Nikitina
 */
public class FileAndDownloadUtil {

    private static Logger logger = Logger.getLogger(FileAndDownloadUtil.class);


    public static void writeStringToFile(String filename,
                                         String content, boolean overwrite) {
        File destFile = new File(filename);
        try {
            if (destFile.exists()) {
                if (overwrite)
                    destFile.delete();
                else
                    return;
            }
            destFile.createNewFile();
            DataOutputStream outputStream = new DataOutputStream(
                    new FileOutputStream(destFile));
            outputStream.writeBytes(content);
            outputStream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param filename
     * @param strings
     * @param overwrite
     */
    public static void writeStringsToFile(String filename,
                                          Collection<String> strings, boolean overwrite) {
        File destFile = new File(filename);
        try {
            if (destFile.exists()) {
                if (overwrite)
                    destFile.delete();
                else
                    return;
            }
            destFile.createNewFile();
            DataOutputStream outputStream = new DataOutputStream(
                    new FileOutputStream(destFile));
            outputStream.writeBytes("\n");
            Iterator<String> it = strings.iterator();
            while (it.hasNext()) {
                String string = it.next() + "\n";
                outputStream.writeBytes(string);
            }
            outputStream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param filename
     * @return
     */
    public static ArrayList<String> readStringsFromFile(String filename) {
        File file = new File(filename);

        return readStringsFromFile(file);
    }

    public static ArrayList<String> readStringsFromFile(File file) {
        ArrayList<String> strings = new ArrayList<String>();
        try {
            if (file.exists()) {
                DataInputStream stream = new DataInputStream(
                        new FileInputStream(file));
                String line = stream.readLine();
                while (line != null) {
                    if (line.length() != 0)
                        strings.add(line);
                    line = stream.readLine();
                }
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
        return strings;
    }

    /**
     * @param filename
     * @return
     */
    public static String readFile(String filename) {
        String output = "";
        File file = new File(filename);
        try {
            if (file.exists()) {
                DataInputStream stream = new DataInputStream(
                        new FileInputStream(file));
                String line = stream.readLine();
                while (line != null) {
                    System.out.println(line);
                    if (line.length() != 0)
                        output += line;
                    line = stream.readLine();
                }
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("processed");
        return output;
    }

    /**
     * @param url
     * @param filename
     * @param overwrite
     * @return
     */
    public static boolean download(String url, String filename,
                                   boolean overwrite) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File destFile = new File(filename);
            // destFile.deleteOnExit();
            if (destFile.exists()) {
                if (overwrite)
                    destFile.delete();
                else
                    return true;
            }
            URL urlObject = new URL(url);
            URLConnection urlc = urlObject.openConnection();
            bis = new BufferedInputStream(urlc.getInputStream());
            destFile.createNewFile();
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            int i;
            while ((i = bis.read()) != -1) {
                bos.write(i);
            }
            if (bis != null)
                bis.close();

            if (bos != null)
                bos.close();
            return true;

        } catch (FileNotFoundException e) {
            logger.debug(e.getMessage());
            return false;

        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            System.out.println("\n" + filename);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            System.out.println(e.getClass().getSimpleName());
        }
        return false;
    }

    /**
     * @param map
     * @param filename
     * @param overwrite
     * @param separator
     */
    public static void writeMapToFile(LinkedHashMap<String, Integer> map,
                                      String filename, boolean overwrite, String separator) {
        File destFile = new File(filename);
        System.out.print("WRITING MAP TO FILE " + destFile.getAbsolutePath()
                + "\n");
        logger.debug("WRITING MAP TO FILE " + destFile.getAbsolutePath());
        try {
            if (destFile.exists()) {
                if (overwrite)
                    destFile.delete();
                else
                    return;
            }
            destFile.createNewFile();
            DataOutputStream outputStream = new DataOutputStream(
                    new FileOutputStream(destFile));
            Set<Entry<String, Integer>> s = map.entrySet();
            Iterator<Entry<String, Integer>> it = s.iterator();
            while (it.hasNext()) {
                Entry<String, Integer> entry = it.next();
                outputStream.writeBytes(entry.getKey() + separator
                        + entry.getValue() + "\n");
            }

            outputStream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * @param passedMap
     * @param ascending
     * @return
     */
    public static LinkedHashMap sortHashMapByValues(HashMap passedMap,
                                                    boolean ascending) {

        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        if (!ascending)
            Collections.reverse(mapValues);

        LinkedHashMap someMap = new LinkedHashMap();
        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                if (passedMap.get(key).toString().equals(val.toString())) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    someMap.put(key, val);
                    break;
                }
            }
        }
        return someMap;
    }


    /**
     * @param in
     * @param out
     * @throws Exception
     */
    public static void copyFile(File in, File out) throws Exception {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fis != null)
                fis.close();
            if (fos != null)
                fos.close();
        }
    }

    /**
     * Writes a Collection of <tt>strings</tt> at the end of the file represented by <tt>filename</tt>
     *
     * @param strings String to be written
     * @param filename File into which the <tt>string</tt> will be wrote
     */
    public static void appendStringsToFile(Collection<String> strings,
                                           String filename) {

        File destFile = new File(filename);
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileOutputStream appendedFile = new FileOutputStream(destFile, true);
            DataOutputStream outputStream = new DataOutputStream(appendedFile);
            outputStream.writeBytes("\n");
            Iterator<String> it = strings.iterator();
            while (it.hasNext()) {
                String string = it.next();
                if (!string.equals("")) {
                    outputStream.writeBytes(string + "\n");
                }
            }

            appendedFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Writes <tt>string</tt> at the end of the file represented by <tt>filename</tt>
     *
     * @param string String to be written
     * @param filename File into which the <tt>string</tt> will be wrote
     */
    public static void appendStringToFile(String string, String filename) {
        if (string.equals("")) {
            return;
        }
        File destFile = new File(filename);
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileOutputStream appendedFile = new FileOutputStream(destFile, true);
            DataOutputStream outputStream = new DataOutputStream(appendedFile);
            outputStream.writeBytes("\n");
            outputStream.writeBytes(string);
            appendedFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a directory with the specified <tt>directoryName</tt>, overwrites an exisiting directory with the same name
     * if <tt>override</tt> is true.
     *
     * @param directoryName string representing the name of the directory to be created.
     * @param override set true if an existing directory with the name <tt>direcoryName</tt> should be overwritten, false otherwise.
     * @return true if the a directory with name <tt>directoryName</tt> was created, false otherwise.
     */
    public static boolean createDirectory(String directoryName, boolean override) {
        if (override)
            deleteDirectory(directoryName);
        File f = new File(directoryName);
        boolean created = false;
        if (!f.exists())
            created = f.mkdirs();
        return created;
    }

    /**
     * Deletes the directory (and any files) represented by <tt>directoryName</tt>, if directory
     * doesn't exits does nothing.
     * @param directoryName the string representing the directory to be deleted.
     */
    public static void deleteDirectory(String directoryName) {
        File directory = new File(directoryName);
        if (directory.exists()) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory())
                            deleteDirectory(file.getAbsolutePath());
                        file.delete();
                    }
                }
            }
            directory.delete();
        }

    }

    /**
     * joins a string array connecting the strings by separator
     *
     * @param strings
     * @param separator
     * @return string containing the strings in <tt>strings</tt>, separated by <tt>separator</tt>.
     */
    public static String join(String[] strings, String separator) {
        String result = "";
        for (String arg : strings) {
            result = result + arg + separator;
        }
        return result;
    }

    /**
     * returns a set of lines, no redundant lines are there
     *
     * @param filename
     */
    public static Set<String> readStringsFromFileAsSet(String filename) {
        ArrayList<String> lines = readStringsFromFile(filename);
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < lines.size(); i++) {
            result.add(lines.get(i));
        }
        return result;
    }

    /**
     * Given a string representing a file determines whether the file exists.
     * @param filename the string representing the path of the file.
     * @return tru if the file exists, false otherwise.
     */
    public static boolean exists(String filename) {
        File f = new File(filename);
        return f.exists();
    }

    public static String eliminateCDDotDot(String absf) {
        while (absf.contains("/../")) {
            absf = absf.replaceAll("/[^/]*/../", "/");
        }
        return absf;
    }


}
