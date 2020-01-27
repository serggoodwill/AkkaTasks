package graph

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Framing, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, IOResult}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

object RunnableGraph extends App {

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

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(flow).to(sink)

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
  implicit val frontend: ActorSystem = ActorSystem("frontend", config)
  implicit val ec: ExecutionContextExecutor = frontend.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  runnableGraph.run().foreach {
    result =>
      println(s"${result.status}, ${result.count} bytes read.")
      frontend.terminate()
  }

}