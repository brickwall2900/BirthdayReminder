package com.github.brickwall2900.birthdays;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * <p>
 * {@code InstanceLock} is a simple class designed to prevent multiple instances
 * from running simultaneously. It does this by locking a file in the
 * temp directory provided with the application ID.
 * </p>
 *
 * <p>
 * If another instance of the application tries to acquire the lock,
 * it will fail, indicating that another instance is already running.
 * </p>
 *
 * <p>The design is very human.</p>
 * <blockquote><pre>
 *     public static InstanceLock lock;
 *     public static void main(String[] args) {
 *         // acquire the lock on start up
 *         if (!lock.lock()) {
 *             // some other instance must be running
 *         }
 *
 *         // application logic
 *     }
 *
 *     // attempt to lock to this instance in form of a busy-loop
 *     // you should do this if your application allows
 *     // for multi-instance launching
 *     public static void someOtherThread() {
 *         while (true) {
 *             if (!lock.isLocked()) {
 *                 lock.lock();
 *                 break;
 *             }
 *         }
 *     }
 *
 *     // release the lock on application exit
 *     public static void shutdown() {
 *         lock.unlock();
 *     }
 * </pre></blockquote>
 * <p>Very easy to use.</p>
 *
 * @author brickwall2900
 */
public class InstanceLock {
    private final Path lockPath;
    private FileChannel fileChannel;
    private FileLock fileLock;
    private boolean isLocked;

    /**
     * @param appId a unique application ID
     */
    public InstanceLock(String appId) {
        lockPath = Paths.get(System.getProperty("java.io.tmpdir"), "InstanceLock_" + appId);
    }

    /**
     * Attempts to acquire the lock for this instance to be the owner of the lock.
     *
     * @return {@code true} if the lock is successfully acquired, {@code false} otherwise.
     */
    public boolean lock() throws LockOperationFailedException {
        try {
            fileChannel = FileChannel.open(lockPath, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            fileLock = fileChannel.tryLock();
            if (fileLock == null) {
                fileChannel.close();
                return false;
            }
        } catch (Exception e) {
            throw new LockOperationFailedException(LockOperationFailedException.LockState.LOCKING, e);
        }
        isLocked = true;
        return true;
    }

    /**
     * Releases the lock from this instance, allowing other instances of the application to acquire it.
     *
     * @return Returns {@code true} if the lock is successfully released, {@code false} otherwise.
     */
    public boolean unlock() throws LockOperationFailedException {
        try {
            if (fileLock != null) {
                fileLock.close();
            }
            fileChannel.close();
        } catch (Exception e) {
            throw new LockOperationFailedException(LockOperationFailedException.LockState.UNLOCKING, e);
        }
        isLocked = false;
        return true;
    }

    /**
     * @return {@code true} if this instance acquired the lock, {@code false} otherwise.
     */
    public boolean isLocked() {
        return isLocked;
    }

    /// This exception would get thrown if there's an exception thrown during a lock operation.
    public static class LockOperationFailedException extends IllegalStateException {
        public LockOperationFailedException(LockState state, Throwable e) {
            super("Lock operation failed on " + state + " state", e);
        }

        /// The lock state during the operation, whether during locking or unlocking.
        public enum LockState {
            LOCKING, UNLOCKING;
        }
    }
}
