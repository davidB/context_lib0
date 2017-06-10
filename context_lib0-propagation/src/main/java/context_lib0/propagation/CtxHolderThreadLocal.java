package context_lib0.propagation;

public class CtxHolderThreadLocal implements CtxHolder {
    private final ThreadLocal<Object> holder = new ThreadLocal<>();

    @Override
    public Object get() {
        return holder.get();
    }

    @Override
    public void set(Object ctx) {
        holder.set(ctx);
    }
}
