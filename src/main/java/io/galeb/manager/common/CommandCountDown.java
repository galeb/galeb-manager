package io.galeb.manager.common;

import io.galeb.manager.engine.util.CounterDownLatch;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tiago.machado on 19/07/16.
 */
public enum CommandCountDown {

    SEND_TO_QUEUE {
        public boolean applied(String[] apis) {
            final AtomicBoolean applied = new AtomicBoolean(true);
            Arrays.stream(apis).forEach(api -> {
                Integer counter = CounterDownLatch.refreshAndGet(api);
                applied.set(applied.get() && counter == null);
            });
            return applied.get();
        }
    },
    STILL_SYNCHRONIZING {
        public boolean applied(String[] apis) {
            final AtomicBoolean applied = new AtomicBoolean(false);
            Arrays.stream(apis).forEach(api -> {
                Integer counter = CounterDownLatch.refreshAndGet(api);
                applied.set(applied.get() || (counter != null && (counter > 0 || counter == -1)));
            });
            return applied.get();
        }
    },
    RELEASE {
        public boolean applied(String[] apis) {
            final AtomicBoolean applied = new AtomicBoolean(true);
            Arrays.stream(apis).forEach(api -> {
                Integer counter = CounterDownLatch.refreshAndGet(api);
                applied.set(applied.get() && (counter != null && counter == 0));
            });
            return applied.get();
        }
    },
    NONE {
        @Override
        public boolean applied(String[] apis) {
            return false;
        }
    };
    public abstract boolean applied(String[] apis);

    public static CommandCountDown getCommandApplied(String[] apis) {
        for (CommandCountDown comm : values()) {
            if (comm.applied(apis)) {
                return comm;
            }
        }
        return NONE;
    }

}
