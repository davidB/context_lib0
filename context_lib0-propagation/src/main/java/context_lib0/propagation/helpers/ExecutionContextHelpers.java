package context_lib0.propagation.helpers;

import context_lib0.propagation.StaticCtxtHolder;

/**
 * Helpers for instrumentation of ExecutionContext with byteman
 */
public class ExecutionContextHelpers {
    public Runnable wrapRunnableWithCtxHolder(Runnable runnable) {
        final Object ctx = StaticCtxtHolder.get();
        return new Runnable() {
            @Override
            public void run() {
                Object previous = StaticCtxtHolder.get();
                StaticCtxtHolder.set(ctx);
                try {
                    runnable.run();
                } finally {
                    StaticCtxtHolder.set(previous);
                }
            }
        };
    }
}
