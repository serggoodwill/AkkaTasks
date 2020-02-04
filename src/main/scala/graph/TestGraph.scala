package graph

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

object TestGraph extends App {

  val inputFile = Paths.get("~/test.txt")
  val outputFile = Paths.get("~/out_test.txt")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))


}

object StreamingCopy extends App {
  val inputFile = Paths.get("/home/goodwill/test.txt")
  val outputFile = Paths.get("/home/goodwill/out_test.txt")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val maxLine = 1024 * 8

  val flow: Flow[ByteString, ByteString, NotUsed] =
    Framing
      .delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8"))
      .map(s => ByteString(s + "*"))

  val conf: String =
    s"""
akka {
actor.provider = "akka.remote.RemoteActorRefProvider"
remote {
enabled-transports = ["akka.remote.netty.tcp"]
netty.tcp {
hostname = "192.168.2.199"
port = 2552
}
}
}
"""

  val config = ConfigFactory.parseString(conf)
  implicit val frontend: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = frontend.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //  ----------------------------  Keep.left
  //  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(flow).to(sink)
  //  runnableGraph.run().foreach {
  //    result =>
  //      println(s"${result.status}, ${result.count} bytes read.")
  //      frontend.terminate()
  //  }
  //  ----------------------------  Keep.right
  //    val runnableGraph: RunnableGraph[Future[IOResult]]= source.via(flow).toMat(sink)(Keep.right)
  //      runnableGraph.run().foreach{
  //        r => println(s"${r.status} ,  ${r.count} bytes.")
  //          actorSystem.terminate()
  //      }
  val runnableGraph: RunnableGraph[(Future[IOResult], Future[IOResult])] = source.via(flow).toMat(sink)(Keep.both)
  val (sourceFuture, sinkFuture) = runnableGraph.run()
  sinkFuture.foreach(s => {
    println(s"Sink ${s.status}   ${s.count} bytes")
    if (sourceFuture.isCompleted) frontend.terminate()
  })
  sourceFuture.foreach(s => {
    println(s"Source ${s.status}   ${s.count} bytes")
    if (sinkFuture.isCompleted) frontend.terminate()
  })
}