package graph

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

object RunnableGraph extends App {

  val inputFile = Paths.get("~/test.txt")
  val outputFile = Paths.get("~/out_test.txt")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)

}

object StreamingCopy extends App {
  val inputFile = Paths.get("/home/user/test.m4a")
  val outputFile = Paths.get("/home/user/out_test.m4a")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)
  val conf: String =
    s"""
akka {
actor.provider = "akka.remote.RemoteActorRefProvider"
remote {
enabled-transports = ["akka.remote.netty.tcp"]
netty.tcp {
hostname = "192.168.2.175"
port = 2552
}
}
}
"""
  val config = ConfigFactory.parseString(conf)
  val backend: ActorSystem = ActorSystem("backend", config)
  implicit val ec: ExecutionContextExecutor = backend.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  runnableGraph.run().foreach {
    result =>
      println(s"${result.status}, ${result.count} bytes read.")
      backend.terminate()
  }

}