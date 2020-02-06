package ecobikes

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, Sink, Source}
import akka.stream.{FlowShape, Graph, IOResult}
import akka.util.ByteString

import scala.concurrent.Future

object EcoBikes extends App {

  type FlowLike = Graph[FlowShape[ByteString, ByteString], NotUsed]

  def processBikes(): FlowLike = {
    val jsFlow = LogJson.jsonOutFlow
    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val delimiterFlow: Flow[ByteString, ByteString, NotUsed] =
          Framing
            .delimiter(ByteString("\n"), 1024)
            .map(_.decodeString("UTF8"))
            .map(s => ByteString(s))
        val bcast = builder.add(Broadcast[ByteString](3))
        val js = builder.add(jsFlow)
        val electric = Flow[ByteString].filter(_.contains("E-BIKE"))
        val folding = Flow[ByteString].filter(_.contains("FOLDING BIKE"))
        val speedelec = Flow[ByteString].filter(_.contains("SPEEDELEC"))

        source ~> delimiterFlow ~> bcast ~> electric ~> sink1
        bcast ~> folding ~> sink2
        bcast ~> speedelec ~> sink3

        FlowShape(bcast.in, js.out)
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
}

