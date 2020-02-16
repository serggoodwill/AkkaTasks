package simple_graph

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

object StreamingCopy extends App {
  val inputFile = Paths.get("test.txt")
  val outputFile = Paths.get("out_test.txt")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, TRUNCATE_EXISTING))
  val maxLine = 1024 * 8
  val flow: Flow[ByteString, ByteString, NotUsed] =
    Framing
      .delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8"))
      .map(s => ByteString(s + "*\n"))


  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  //    ----------------------------  Keep.left
  val gLeft: RunnableGraph[Future[IOResult]] = source.via(flow).to(sink)
  gLeft.run().foreach {
    result =>
      println(s"${result.status}, ${result.count} bytes read.")
      actorSystem.terminate()
  }
  //    ----------------------------  Keep.right
  val gRight: RunnableGraph[Future[IOResult]] = source.via(flow).toMat(sink)(Keep.right)
  gRight.run().foreach {
    result =>
      println(s"${result.status} ,  ${result.count} bytes written.")
      actorSystem.terminate()
  }
  //    ----------------------------  Keep.both
  val gBoth: RunnableGraph[(Future[IOResult], Future[IOResult])] = source.via(flow).toMat(sink)(Keep.both)
  val (sourceFuture, sinkFuture) = gBoth.run()
  sourceFuture.foreach(s => {
    println(s"Source ${s.status}   ${s.count} bytes written")
    if (sinkFuture.isCompleted) actorSystem.terminate()
  })
  sinkFuture.foreach(s => {
    println(s"Sink ${s.status}   ${s.count} bytes read")
    if (sourceFuture.isCompleted) actorSystem.terminate()
  })
}