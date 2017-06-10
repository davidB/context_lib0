package context_lib0.propagation;

public class CtxHolderNoop implements CtxHolder {
    @Override
    public Object get() {
        return null;
    }

    @Override
    public void set(Object ctx) {
    }
}
