package context_lib0.propagation;

public class StaticCtxtHolder {
    public static CtxHolder instance = new CtxHolderNoop();

    public static Object get() {
        return instance.get();
    }

    public static void set(Object ctx) {
        instance.set(ctx);
    }
}
