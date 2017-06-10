package context_lib0.propagation.di;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import context_lib0.propagation.CtxHolder;
import context_lib0.propagation.StaticCtxtHolder;
import context_lib0.propagation.CtxHolderThreadLocal;

class CtxHolderThreadLocalModule extends AbstractModule {
    @Override
    public void configure(){
    }

    @Provides
    @Singleton
    public CtxHolder ctxHolder(){
        StaticCtxtHolder.instance = new CtxHolderThreadLocal();
        return StaticCtxtHolder.instance;
    }

}
