package context_lib0.scalatools

import akka.actor.{Actor, ActorSystem, Props}
import context_lib0.propagation.{CtxHolder, CtxHolderNoop, CtxHolderThreadLocal, StaticCtxtHolder}
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

case class Ctx(name: String)

class PropagationSpec extends FlatSpec {
    StaticCtxtHolder.instance = new CtxHolderThreadLocal()
    val currentCtx = StaticCtxtHolder.instance
    //val ctxFactory = new CtxFactorySpanOnly(NoopTracerFactory.create())
    val ctxFactory = new CtxFactory[Ctx] {
        override def newCtx(name: String): Ctx = Ctx(name)
    }
    val ctxTools = new CtxTools(currentCtx, ctxFactory)
    val pcol = new PropagationCollector(currentCtx)

    "Implicit Propagation" should "work as explicit for sync (no future) chaining with sub ctx" in {
        val service = new FakeService(ctxTools, pcol)
        pcol.report(null)
        ctxTools.inCtx(ctxTools.newCtx("0")) { implicit ctx0 =>
            pcol.report(ctx0)
            service.doJobInNewCtx("job1")
            pcol.report(ctx0)
            service.doJobNoReturn()
            pcol.report(ctx0)
            service.doJobInNewCtx("job2")
            pcol.report(ctx0)
        }
        pcol.report(null)

        pcol.checkCollected()
    }

    it should "work as explicit for simple future chaining" in {
        val service = new FakeService(ctxTools, pcol)
        pcol.report(null)
        ctxTools.inCtx(ctxTools.newCtx("0")) { implicit ctx0 =>
            pcol.report(ctx0)

            val m0 = "m0"
            val chain = service.doJobAsync(m0).map(v => service.doJobInNewCtx(v)).flatMap(v => service.doJobAsyncInNewCtx(v))
            Await.ready(chain, 3.seconds)
        }
        pcol.report(null)
        pcol.checkCollected()
    }

    it should "work as explicit for simple actor" in {
        val system = ActorSystem("test")
        val actor = system.actorOf(Props(new FakeActor(ctxTools, pcol)))
        pcol.report(null)
        ctxTools.inCtx(ctxTools.newCtx("0")) { implicit ctx0 =>
            pcol.report(ctx0)
            actor ! MyMessage("m0", 3)
            Thread.sleep(1000)
        }
        pcol.report(null)
        pcol.checkCollected()
    }

    //TODO test with ask

    //TODO test with several concurrent message + recursive call

//    "PropagationCollector.checkCollected" should "failed on invalid" in {
//        val pcol = new PropagationCollector(() => Some("ok"))
//        pcol.report("ok")
//        pcol.report("ko")
//        pcol.report("ok")
//        pcol.checkCollected()
//    }

    "PropagationCollector.checkCollected" should "pass on valid" in {
        val ctx = ctxTools.newCtx("ok")
        val currentCtx = new CtxHolderNoop {
            override def get(): AnyRef = ctx
        }
        val pCol0 = new PropagationCollector(currentCtx)
        pCol0.report(ctx)
        pCol0.report(ctx)
        pCol0.report(ctx)
        pCol0.checkCollected()
    }
}

class PropagationCollector(currentCtx: CtxHolder) {
    case class Entry(expl: AnyRef, impl: AnyRef, stackTraceElements: Array[StackTraceElement])
    val collected = new java.util.concurrent.ConcurrentLinkedQueue[Entry]()
    def report(expl: AnyRef): Unit = {
        val stack = Thread.currentThread().getStackTrace
        collected.add(Entry(expl, currentCtx.get(), stack))
    }

    def checkCollected(): Unit = {
        var e = collected.poll()
        while (e != null) {
            assert(e.impl === e.expl, "| implicit vs explicit mismatch at: " + e.stackTraceElements.mkString("\n\t"))
            e = collected.poll()
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------
// Fake Application Fragment

class FakeService(ctxTools: CtxTools[Ctx], pcol: PropagationCollector) {
    def doJobNoReturn()(implicit ctx: Ctx = null): Unit = {
        pcol.report(ctx)
    }

    def doJobIdentity[T](input: T)(implicit ctx: Ctx): T = {
        pcol.report(ctx)
        input
    }
    def doJobInNewCtx[T](input: T)(implicit ctx: Ctx): T = {
        ctxTools.inCtx(ctxTools.newCtx("doJobInNewCtx")) { implicit ctx =>
            doJobIdentity(input)
        }
    }
    def doJobAsync[T](input: T)(implicit ctx: Ctx): Future[String] = {
        pcol.report(ctx)
        Future{
            doJobIdentity("doJobAsync")
        }
    }
    def doJobAsyncInNewCtx[T](input: T)(implicit ctx: Ctx): Future[String] = {
        ctxTools.inCtx(ctxTools.newCtx("doJobAsyncInNewCtx")) { implicit ctx =>
            pcol.report(ctx)
            Future{
                doJobIdentity("doJobAsyncInNewCtx")
            }
        }
    }
    def doJobAsyncSameT[T](input: T)(implicit ctx: Ctx): Future[String] = {
        pcol.report(ctx)
        Future.successful(
            doJobIdentity("doJobAsyncSameT")
        )
    }
    def doJobAsyncSameTInNewCtx[T](input: T)(implicit ctx: Ctx): Future[String] = {
        ctxTools.inCtx(ctxTools.newCtx("doJobAsyncSameTInNewCtx")) { implicit ctx =>
            pcol.report(ctx)
            Future.successful("doJobAsyncInNewCtx")
        }
    }
    def doJobAsyncFailedSameT[T](input: T)(implicit ctx: Ctx): Future[T] = {
        pcol.report(ctx)
        Future.failed(new Exception("sample error"))
    }
    def doJobAsyncFailedSameTInNewCtx[T](input: T)(implicit ctx: Ctx): Future[T] = {
        ctxTools.inCtx(ctxTools.newCtx("doJobAsyncFailedSameTInNewCtx")) { implicit ctx =>
            pcol.report(ctx)
            Future.failed(new Exception("sample error"))
        }
    }
}

case class MyMessage(txt: String, cnt: Int)(implicit val ctx: Ctx = null)
class FakeActor(ctxTools: CtxTools[Ctx], pcol: PropagationCollector) extends Actor {
    override def receive = {
        case m: MyMessage =>
            implicit val ctx = m.ctx
            pcol.report(m.ctx)
            if (m.cnt > 0) {
                if (m.cnt % 2 == 0) {
                    ctxTools.inCtx(ctxTools.newCtx("newMessage" + m.cnt)) {implicit ctx =>
                        self ! MyMessage(m.txt + "x", m.cnt - 1)
                    }
                } else {
                    self ! MyMessage(m.txt + "x", m.cnt - 1)
                }
            }
    }

}


