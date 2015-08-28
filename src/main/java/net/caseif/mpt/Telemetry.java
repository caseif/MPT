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
package net.caseif.mpt;

import net.caseif.jtelemetry.JTelemetry;
import net.caseif.mpt.util.Config;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for telemetry functionality.
 */
public class Telemetry {

    private static final String SERVER_ADDRESS = "http://telemetry.caseif.net/mpt.php";

    private static boolean disabled = false;
    private static boolean dirty = true; // initially marked dirty so stats are submitted on plugin enable

    private static UUID id;

    @SuppressWarnings("unchecked")
    public static void submitData() {
        JTelemetry jt = new JTelemetry(SERVER_ADDRESS);
        JTelemetry.Payload payload = jt.createPayload();

        payload.addData("id", id.toString());
        payload.addData("ver", Main.plugin.getDescription().getVersion());

        Set<Map.Entry> entries = ((JSONObject) Main.repoStore.get("repositories")).entrySet();
        List<String> repos = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : entries) {
            repos.add(entry.getKey() + "$$$$" + entry.getValue().get("url"));
        }
        String[] repoArray = new String[repos.size()];
        repos.toArray(repoArray);
        payload.addData("repoCount", repoArray.length);

        int totalLength = 0;
        for (String str : repos) {
            totalLength += str.length() + 1;
        }
        if (totalLength - 1 <= 1024) {
            payload.addData("repos", repoArray);
        }

        Set<Map.Entry<String, JSONObject>> packageEntries = ((JSONObject) Main.packageStore.get("packages")).entrySet();
        payload.addData("packs", packageEntries.size());

        JTelemetry.HttpResponse response;
        try {
            response = payload.submit();
            if (response.getStatusCode() / 100 != 2) {
                Main.log.warning("Telemtry server responded with non-success status code");
                Main.log.warning("Response message: " + response.getStatusCode() + " " + response.getMessage());
            } else {
                dirty = false;
            }
        } catch (IOException ex) {
            Main.log.severe("Failed to submit telemetry data");
            ex.printStackTrace();
        }
    }

    public static void loadTelemetryId() {
        File idFile = new File(Main.plugin.getDataFolder(), "server_id");
        try {
            if (idFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(idFile));
                String str = reader.readLine();
                if (str != null) {
                    try {
                        id = UUID.fromString(str);
                        return;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            id = UUID.randomUUID();
            idFile.delete();
            idFile.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(idFile))) {
                writer.write(id.toString());
            }
        } catch (IOException ex) {
            disabled = true;
            Main.log.severe("Failed to save/load server identifier! Telemetry disabled.");
            ex.printStackTrace();
        }
    }

    public static void setDirty() {
        dirty = true;
    }

    public static void startTask() {
        if (Config.TELEMETRY) {
            loadTelemetryId();
            if (!disabled) {
                Bukkit.getScheduler().runTaskTimerAsynchronously(Main.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (dirty) {
                            submitData();
                        }
                    }
                }, 0L, (60L * 60L * 20L));
            }
        }
    }

}
