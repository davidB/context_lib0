package context_lib0.propagation;

public interface CtxAware {
    /**
     * Getter of the context hold by the object (callable, message,...)
     * @return the context to use or null
     */
    Object ctx();
}
