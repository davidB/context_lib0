package context_lib0.scalatools

trait CtxFactory[Ctx] {
    def newCtx(name: String): Ctx
}


//class CtxFactorySpanOnly @Inject()(tracer: Tracer) extends CtxFactory {
//    def newCtx(name: String)(implicit from: Option[Ctx] = None, fromKind: String = References.CHILD_OF): Ctx = {
//        val bs = tracer.buildSpan(name)
//        if from .foreach { fromCtx =>
//            bs.addReference(fromKind, fromCtx.span.context())
//        }
//        val span = bs.startActive()
//        CtxSpanOnly(span)
//    }
//}
