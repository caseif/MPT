/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014-2015 Maxim Roncacé <me@caseif.net>
 *
 * The MIT License (MIT)
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all
 *     copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *     SOFTWARE.
 */
package net.caseif.mpt.util;

import net.caseif.mpt.Main;
import net.caseif.mpt.json.JSONPrettyPrinter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MiscUtil {

    /**
     * Convenience method allow CommandSender#sendMessage(String) to be called from async tasks.
     *
     * @param sender  the sender to send the message to
     * @param message the message to send
     */
    public static void threadSafeSendMessage(final CommandSender sender, final String message) {
        Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
            public void run() {
                sender.sendMessage(message);
            }
        });
    }

    /**
     * Attempts to lock the local stores and returns false if they are already locked.
     *
     * @throws MPTException if the store is already locked
     */
    public static void lockStores() throws MPTException {
        if (Main.LOCKED) {
            throw new MPTException(Config.ERROR_COLOR + "Failed to lock local stores! Perhaps a task is currently "
                    + "running or has uncleanly terminated?");
        }
        Main.LOCKED = true;
    }

    /**
     * Unlocks the local stores.
     */
    public static void unlockStores() {
        Main.LOCKED = false;
    }

    public static JSONObject getRemoteIndex(String path) throws MPTException {
        try {
            URL url = new URL(path + (!path.endsWith("/") ? "/" : "") + "mpt.json"); // get URL object for data file
            URLConnection conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection)conn; // cast the connection
                int response = http.getResponseCode(); // get the response
                if (response >= 200 && response <= 299) { // verify the remote isn't upset at us
                    InputStream is = http.getInputStream(); // open a stream to the URL
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is)); // get a reader
                    JSONParser parser = new JSONParser(); // get a new parser
                    String line;
                    StringBuilder content = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    JSONObject json = (JSONObject)parser.parse(content.toString()); // parse JSON object
                    // vefify remote config is valid
                    if (json.containsKey("packages") && json.get("packages") instanceof JSONObject) {
                        return json;
                    } else {
                        throw new MPTException(Config.ERROR_COLOR + "Index for repository at " + path
                                + "is missing required elements!");
                    }
                } else {
                    String error = Config.ERROR_COLOR + "Remote returned bad response code! (" + response + ")";
                    if (!http.getResponseMessage().isEmpty()) {
                        error += " The remote says: " + ChatColor.GRAY + ChatColor.ITALIC + http.getResponseMessage();
                    }
                    throw new MPTException(error);
                }
            } else {
                throw new MPTException(Config.ERROR_COLOR + "Bad protocol for URL!");
            }
        } catch (MalformedURLException ex) {
            throw new MPTException(Config.ERROR_COLOR + "Cannot parse URL!");
        } catch (IOException ex) {
            throw new MPTException(Config.ERROR_COLOR + "Cannot open connection to URL!");
        } catch (ParseException ex) {
            throw new MPTException(Config.ERROR_COLOR + "Repository index is not valid JSON!");
        }
    }

    /**
     * Calculates the SHA-1 hash for the file at the given path.
     *
     * @param path the location of the file to hash
     * @return the SHA-1 checksum for the file
     * @throws MPTException if a stream cannot be opened to the file
     */
    public static String sha1(String path) throws MPTException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            FileInputStream fis = new FileInputStream(path);
            byte[] dataBytes = new byte[1024];

            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            fis.close();

            byte[] mdbytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
                String hex = Integer.toHexString(0xff & mdbyte);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception ex) {
            if (Config.ENFORCE_CHECKSUM) {
                throw new MPTException(Config.ERROR_COLOR + "Failed to get checksum for local package "
                        + Config.ID_COLOR + path + Config.ERROR_COLOR + "!");
            }
        }
        return null;
    }

    public static int getFileSize(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("HEAD"); // joke's on you if the server doesn't specify
            conn.getInputStream();
            return conn.getContentLength();
        } catch (Exception e) {
            return -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static boolean unzip(ZipFile zip, File dest, List<String> files) throws MPTException {
        boolean returnValue = true;
        try {
            List<String> existingDirs = new ArrayList<>();
            Enumeration<? extends ZipEntry> en = zip.entries();
            entryLoop:
            while (en.hasMoreElements()) {
                ZipEntry entry = en.nextElement();
                String name = entry.getName().startsWith("./")
                        ? entry.getName().substring(2, entry.getName().length())
                        : entry.getName();
                File file = new File(dest, name);
                if (entry.isDirectory()) {
                    if (file.exists()) {
                        if (Config.DISALLOW_MERGE) {
                            existingDirs.add(name);
                            if (Config.VERBOSE) {
                                Main.log.warning("Refusing to extract directory " + name + ": already exists");
                            }
                        }
                    }
                } else {
                    files.add(name);
                    for (String dir : Config.DISALLOWED_DIRECTORIES) {
                        if (file.getPath().startsWith(dir)) {
                            if (Config.VERBOSE) {
                                Main.log.warning("Refusing to extract " + name + " from " + zip.getName()
                                        + ": parent directory \"" + dir + "\" is not allowed");
                            }
                            continue entryLoop;
                        }
                    }
                    if (Config.DISALLOW_MERGE) {
                        for (String dir : existingDirs) {
                            if (file.getPath()
                                    .substring(2, file.getPath().length())
                                    .replace(File.separator, "/")
                                    .startsWith(dir)) {
                                continue entryLoop;
                            }
                        }
                    }
                    if (!Config.DISALLOW_OVERWRITE || !file.exists()) {
                        file.getParentFile().mkdirs();
                        for (String ext : Config.DISALLOWED_EXTENSIONS) {
                            if (file.getName().endsWith(ext)) {
                                if (Config.VERBOSE) {
                                    Main.log.warning("Refusing to extract " + name + " from " + zip.getName()
                                            + ": extension \"" + ext + "\" is not allowed");
                                }
                                returnValue = false;
                                continue entryLoop;
                            }
                        }
                        BufferedInputStream bIs = new BufferedInputStream(zip.getInputStream(entry));
                        int b;
                        byte[] buffer = new byte[1024];
                        FileOutputStream fOs = new FileOutputStream(file);
                        BufferedOutputStream bOs = new BufferedOutputStream(fOs, 1024);
                        while ((b = bIs.read(buffer, 0, 1024)) != -1) {
                            bOs.write(buffer, 0, b);
                        }
                        bOs.flush();
                        bOs.close();
                        bIs.close();
                    } else {
                        if (Config.VERBOSE) {
                            Main.log.warning("Refusing to extract " + name + " from " + zip.getName()
                                    + ": already exists");
                        }
                        returnValue = false;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(); //TODO
            throw new MPTException(Config.ERROR_COLOR + "Failed to extract archive!");
        }
        return returnValue;
    }

    /**
     * Writes the repository store to disk. This method is <strong>not</strong> thread-safe; the stores much be locked
     * by the caller.
     *
     * @throws IOException if an exception occurs while writing data to the disk
     */
    public static void writeRepositoryStore() throws IOException {
        File file = new File(Main.plugin.getDataFolder(), "repositories.json");
        FileWriter writer = new FileWriter(file); // get a writer for the store file
        writer.write(JSONPrettyPrinter.toJSONString(Main.repoStore)); // write to disk
        writer.flush();
    }

    /**
     * Writes the package store to disk. This method is <strong>not</strong> thread-safe; the stores much be locked by
     * the caller.
     *
     * @throws IOException if an exception occurs while writing data to the disk
     */
    public static void writePackageStore() throws IOException {
        File file = new File(Main.plugin.getDataFolder(), "packages.json");
        FileWriter writer = new FileWriter(file); // get a writer for the store file
        writer.write(JSONPrettyPrinter.toJSONString(Main.packageStore)); // write to disk
        writer.flush();
    }

    /**
     * Compares two version strings.
     *
     * @param version1 the first version to compare
     * @param version2 the second version to compare
     * @return -1 if the first is more recent, 1 if the second is more recent, or 0 if the versions are equal
     */
    public static int compareVersions(String version1, String version2) {
        // separate main version from qualifier
        String vStr1 = version1.contains("-") ? version1.split("-")[0] : version1;
        String vStr2 = version2.contains("-") ? version2.split("-")[0] : version2;

        // compare major versions
        int major1;
        int major2;
        try {
            major1 = Integer.parseInt(vStr1.split("\\.")[0]);
            if (major1 < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version string \"" + vStr1 + "\" contains invalid major version!");
        }
        try {
            major2 = Integer.parseInt(vStr2.split("\\.")[0]);
            if (major2 < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version string \"" + vStr2 + "\" contains invalid major version!");
        }
        if (major2 > major1) {
            return 1;
        } else if (major2 < major1) {
            return -1;
        }
        // else major versions are equal

        // compare minor versions
        int minor1;
        int minor2;
        try {
            minor1 = vStr1.split("\\.").length > 1 ? Integer.parseInt(vStr1.split("\\.")[1]) : 0;
            if (minor1 < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version string \"" + vStr1 + "\" contains invalid minor version!");
        }
        try {
            minor2 = vStr2.split("\\.").length > 1 ? Integer.parseInt(vStr2.split("\\.")[1]) : 0;
            if (minor2 < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version string \"" + vStr2 + "\" contains invalid minor version!");
        }
        if (minor2 > minor1) {
            return 1;
        } else if (minor2 < minor1) {
            return -1;
        }
        // else minor versions are equal

        // compare incremental versions
        int inc1;
        int inc2;
        try {
            inc1 = vStr1.split("\\.").length > 2 ? Integer.parseInt(vStr1.split("\\.")[2]) : 0;
            if (inc1 < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version string \"" + vStr1 + "\" contains invalid minor version!");
        }
        try {
            inc2 = vStr2.split("\\.").length > 2 ? Integer.parseInt(vStr2.split("\\.")[2]) : 0;
            if (inc2 < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version string \"" + vStr2 + "\" contains invalid minor version!");
        }
        if (inc2 > inc1) {
            return 1;
        } else if (inc2 < inc1) {
            return -1;
        }
        // else incremental versions are equal

        String qual1 = version1.contains("-") ? version1.substring(version1.indexOf("-") + 1) : "";
        String qual2 = version2.contains("-") ? version2.substring(version2.indexOf("-") + 1) : "";
        int lex = qual1.compareTo(qual2);
        if (lex > 0) {
            return -1;
        } else if (lex < 0) {
            return 1;
        } else {
            return 0;
        }
    }

}
