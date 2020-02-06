package ecobikes

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, Sink, Source}
import akka.stream.{FlowShape, Graph, IOResult}
import akka.util.ByteString

import scala.concurrent.Future

object EcoBikes extends App {

  type FlowLike = Graph[FlowShape[String, ByteString], NotUsed]

  def processBikes(): FlowLike = {

    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val bcast = builder.add(Broadcast[String](3))
        val electric = Flow[String].filter(_.contains("E-BIKE"))
        val folding = Flow[String].filter(_.contains("FOLDING BIKE"))
        val speedelec = Flow[String].filter(_.contains("SPEEDELEC"))

        bcast ~> electric
        bcast ~> folding
        bcast ~> speedelec

        FlowShape(bcast.in, source.out)
      }
    )
  }

  val inputFile = Paths.get("/home/goodwill/Downloads/akka/ecobike.txt")

  val outputFile1 = Paths.get("/home/goodwill/Downloads/akka/out_filter1.txt")

  val outputFile2 = Paths.get("/home/goodwill/Downloads/akka/out_filter2.txt")

  val outputFile3 = Paths.get("/home/goodwill/Downloads/akka/out_filter3.txt")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val sink1: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile1, Set(CREATE, WRITE, APPEND))
  val sink2: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile2, Set(CREATE, WRITE, APPEND))
  val sink3: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile3, Set(CREATE, WRITE, APPEND))

  class Bike

}

