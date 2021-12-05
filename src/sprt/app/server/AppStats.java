/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server;

import n4m.serialization.ApplicationEntry;
import n4m.serialization.ECException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static n4m.serialization.N4MResponse.dateToTimestamp;

/**
 * Thread-safe tracker for app usage statistics
 */
public class AppStats {
    // Tracks usages of each app
    private final Map<ServerApp, Integer> accessCts = new HashMap<>();
    // Timestamp last app was run
    private long lastAppTimestamp = 0;

    /**
     * Records that the given app was run
     * @param app the app that ran
     */
    public void appWasRun(ServerApp app) {
        safeIncrementCount(app);
        lastAppTimestamp = dateToTimestamp(new Date());
    }

    private void safeIncrementCount(ServerApp app) {
        synchronized (accessCts) {
            if (accessCts.containsKey(app)) {
                int accessCt = accessCts.get(app);
                if (accessCt < ApplicationEntry.MAX_ACCESS_COUNT)
                    ++accessCt;
                accessCts.put(app, accessCt);
            }
            else {
                accessCts.put(app, 1);
            }
        }
    }

    /**
     * Return timestamp of last time an app was run
     * @return timestamp (seconds since 1970-01-01)
     */
    public long getLastAppTimestamp() {
        return lastAppTimestamp;
    }

    /**
     * Gets number of usages for each app run
     * @return application entries
     */
    public List<ApplicationEntry> getEntries() {
        synchronized (accessCts) {
            return accessCts.entrySet().stream()
                    .map(this::mapEntryToApplicationEntry)
                    .collect(Collectors.toList());
        }
    }

    private ApplicationEntry mapEntryToApplicationEntry(Map.Entry<ServerApp, Integer> entry) {
        // App name is the name of the class of the app (e.g. sprt.app.server.apps.Poll.Poll becomes "Poll")
        var name = entry.getKey().getClass().getSimpleName();
        int accessCt = entry.getValue();
        try {
            return new ApplicationEntry(name, accessCt);
        } catch (ECException e) {
            throw new RuntimeException("SPRT server returned bad data, should never happen");
        }
    }
}
