
package erwins.jsample.current;

import java.util.*;
import java.util.concurrent.*;

/**
 * 스래드 종료시 강제종료된 내역을 리턴한다.
 */
public class ShutdownAndSeeForcedRun extends AbstractExecutorService {
    private final ExecutorService exec;
    private final Set<Runnable> tasksCancelledAtShutdown = Collections.synchronizedSet(new HashSet<Runnable>());

    public ShutdownAndSeeForcedRun(ExecutorService exec) {
        this.exec = exec;
    }

    public void shutdown() {
        exec.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return exec.shutdownNow();
    }

    public boolean isShutdown() {
        return exec.isShutdown();
    }

    public boolean isTerminated() {
        return exec.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return exec.awaitTermination(timeout, unit);
    }

    public List<Runnable> getCancelledTasks() {
        if (!exec.isTerminated()) throw new IllegalStateException(/* ... */);
        return new ArrayList<Runnable>(tasksCancelledAtShutdown);
    }

    public void execute(final Runnable runnable) {
        exec.execute(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                }
                finally {
                    if (isShutdown() && Thread.currentThread().isInterrupted()) tasksCancelledAtShutdown.add(runnable);
                }
            }
        });
    }
}
