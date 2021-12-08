/* ***********************************************
 *
 * Author:      Kevin DeMars
 * Assignment:  SPRT
 * Class:       CSI 4321
 *
 * ***********************************************/

package sprt.app.server.loadtest;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadTestingLib {
    private static final Logger LOG = Logger.getLogger("LoadTestingLib");

    public static class PerfStats {
        int count = 0;
        Duration totalTime = Duration.ZERO, minTime = null, maxTime = null, avgTime = null;

        // Incorporate the given sample into the statistics (thread-safe)
        private void add(Duration time) {
            synchronized (this) {
                ++count;
                totalTime = totalTime.plus(time);
                if (minTime == null || time.compareTo(minTime) < 0)
                    minTime = time;
                if (maxTime == null || time.compareTo(maxTime) > 0)
                    maxTime = time;
                avgTime = totalTime.dividedBy(count);
            }
        }
    }

    /**
     * Does performance testing on a given method.
     * @param r method to run
     * @param callsPerSec amount to run r every second
     * @param testDurationSecs time to run tests for.
     * @return stats measured from testing
     */
    public static PerfStats measurePerformance(Callable<?> r, int callsPerSec, int testDurationSecs) {
        // Used for the functions being measured
        var threadPool = Executors.newCachedThreadPool();
        // Stores results
        var stats = new PerfStats();
        // nanoseconds between each request
        long period = 1_000_000_000 / callsPerSec;
        // Used to schedule new tests for single function call
        var scheduler = Executors.newSingleThreadScheduledExecutor();

        // Every X nanoseconds, create a new thread w/ the task
        scheduler.scheduleAtFixedRate(() -> threadPool.execute(() -> fireTask(stats, r)),
                0, period, TimeUnit.NANOSECONDS);

        // Shutdown
        try {
            // Wait X seconds for tests to run
            Thread.sleep(testDurationSecs * 1000L);
            // Start shutdown
            System.out.println("Waiting for tasks to finish...");
            // Stop scheduler; force quit after 2 secs
            scheduler.shutdown();
            boolean terminated = scheduler.awaitTermination(2, TimeUnit.SECONDS);
            if (!terminated) {
                scheduler.shutdownNow();
            }
            // Stop thread pool with tasks; force quit after 5 secs
            threadPool.shutdown();
            terminated = threadPool.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated) {
                var forceStopped = threadPool.shutdownNow();
                System.out.println("Forcefully shut down " + forceStopped.size() + " tasks");
            }
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Interrupted during testing", e);
            System.err.println("Testing was interrupted; exiting");
            System.exit(2);
        }
        return stats;
    }

    // What to do each time a new single test is supposed to be run
    private static void fireTask(PerfStats stats, Callable<?> r) {
        measureRuntime(r).ifPresent(stats::add);
    }

    // Measures runtime of the given callback, returns empty if an exception is thrown
    private static Optional<Duration> measureRuntime(Callable<?> r) {
        try {
            var start = Instant.now();
            r.call();
            var end = Instant.now();
            return Optional.of(Duration.between(start, end));
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }
}
