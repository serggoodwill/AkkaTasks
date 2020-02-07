package ecobikes

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, Graph, IOResult}
import akka.util.ByteString

import scala.concurrent.{ExecutionContextExecutor, Future}

object EcoBikes extends App {

  type FlowLike = Graph[FlowShape[String, ByteString], NotUsed]

  def processBikes(): FlowLike = {
    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val bsFlow: Flow[String, ByteString, NotUsed] =
          Flow[String].map(s => ByteString(s))

        val format: Flow[String, String, NotUsed] =
          Flow[String].map(s => s + '\n')

        val bcast = builder.add(Broadcast[String](4))
        val bs = builder.add(bsFlow)
        val electric = Flow[String].filter(_.contains("E-BIKE"))
        val folding = Flow[String].filter(_.contains("FOLDING BIKE"))
        val speedelec = Flow[String].filter(_.contains("SPEEDELEC"))

        bcast ~> bs.in
        bcast ~> electric ~> format ~> bsFlow ~> sinkBike("E-BIKE")
        bcast ~> folding ~> format ~> bsFlow ~> sinkBike("FOLDING BIKE")
        bcast ~> speedelec ~> bsFlow ~> sinkBike("SPEEDELEC")

        FlowShape(bcast.in, bs.out)
      }
    )
  }


  val inputFile = Paths.get("/home/goodwill/Downloads/akka/ecobike.txt")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)

  val frame: Flow[ByteString, String, NotUsed] =
    Framing
      .delimiter(ByteString("\n"), 1024)
      .map(_.decodeString("UTF8"))

  def sinkBike(bikeType: String): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(Paths.get(s"/home/goodwill/Downloads/akka/out_$bikeType.txt"), Set(CREATE, WRITE, APPEND))

  val outputFile: Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(Paths.get("/home/goodwill/Downloads/akka/out_full.txt"), Set(CREATE, WRITE, APPEND))

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  source
    .via(frame)
    .via(processBikes())
    .toMat(outputFile)(Keep.right)
    .run()
}
