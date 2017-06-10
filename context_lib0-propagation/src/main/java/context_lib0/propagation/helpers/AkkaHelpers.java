package context_lib0.propagation.helpers;

import context_lib0.propagation.CtxAware;
import context_lib0.propagation.StaticCtxtHolder;

/**
 * Helpers for instrumentation of Akka with byteman
 */
public class AkkaHelpers {
    public Object wrapEnvelope(Object m) {
        return (m instanceof CtxAware)
                ? m
                : new EnvelopeCtxAware(m, StaticCtxtHolder.get())
                ;
    }

    public Object unwrapEnvelope(Object m) {
        if (m instanceof EnvelopeCtxAware) {
            EnvelopeCtxAware m1 = (EnvelopeCtxAware) m;
            StaticCtxtHolder.set(m1.ctx);
            return m1.msg;
        } else if (m instanceof CtxAware) {
            CtxAware m1 = (CtxAware) m;
            StaticCtxtHolder.set(m1.ctx());
            return m;
        } else {
            return m;
        }
    }
}

class EnvelopeCtxAware implements CtxAware {
    public final Object msg;
    public final Object ctx;
    public EnvelopeCtxAware(Object msg, Object ctx) {
        this.msg = msg;
        this.ctx = ctx;
    }
    public Object ctx() {
        return ctx;
    }
}
