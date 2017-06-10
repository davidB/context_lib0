package context_lib0.scalatools

import javax.inject.Inject

import context_lib0.propagation.CtxHolder

import scala.concurrent.Future

class CtxTools[Ctx] @Inject()(val holder: CtxHolder, val factory: CtxFactory[Ctx]) {

    def newCtx(name: String): Ctx = factory.newCtx(name)

    def startCtx(ctx: Ctx): Ctx = {
        ctx
    }
    def finishCtx(ctx: Ctx): Ctx = {
        //ctx.span.deactivate()
        ctx
    }

    def withCurrentCtx[T](ctx: Ctx)(f: Ctx => T): T = {
        val previousCtx = holder.get()
        try {
            holder.set(ctx)
            f(ctx)
        } finally {
            holder.set(previousCtx)
        }
    }

    /*
     * start + set current -> "schedule" f + finish on complete + restore previous current
     */
    def inAsyncCtx[T](ctx: Ctx)(f: Ctx => Future[T]): Future[T] = {
        val startedCtx = startCtx(ctx)
        withCurrentCtx(startedCtx) { implicit ctx =>
            val res = f(ctx)
            res.onComplete(_ => finishCtx(startedCtx))(SameThreadExecutionContext)
            res
        }
    }

    def inCtx[T](ctx: Ctx)(f: Ctx => T): T = {
        val startedCtx = startCtx(ctx)
        withCurrentCtx(startedCtx) { implicit ctx =>
            try {
                f(ctx)
            } finally {
                finishCtx(startedCtx)
            }
        }
    }
}
