package ecobikes

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape, IOResult}
import akka.util.ByteString

import scala.concurrent.Future

object EcoBikesAlt extends App {

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get("ecobike.txt"))

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    def sinkBike(bikeType: String): Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(s"out_$bikeType.txt"), Set(CREATE, WRITE, TRUNCATE_EXISTING))

    val frame: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), 1024).map(_.decodeString("UTF8"))
    val bsFlow: Flow[String, ByteString, NotUsed] = Flow[String].map(s => ByteString(s))
    val format: Flow[String, String, NotUsed] = Flow[String].map(s => s + '\n')
    val bcast = builder.add(Broadcast[String](3))
    val electric = Flow[String].filter(_.contains("E-BIKE"))
    val folding = Flow[String].filter(_.contains("FOLDING BIKE"))
    val speedelec = Flow[String].filter(_.contains("SPEEDELEC"))

    source ~> frame ~> bcast ~> electric ~> format ~> bsFlow ~> sinkBike("E-BIKE")
    bcast ~> folding ~> format ~> bsFlow ~> sinkBike("FOLDING")
    bcast ~> speedelec ~> format ~> bsFlow ~> sinkBike("SPEEDELEC")
    ClosedShape
  })
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  g.run(materializer)
}
