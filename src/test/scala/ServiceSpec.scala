import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._

class ServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val ip1Info = IpInfo("8.8.8.8", Option("United States"), Option("Mountain View"), Option(37.386), Option(-122.0838))
  val ip2Info = IpInfo("8.8.4.4", Option("United States"), None, Option(38.0), Option(-97.0))
  val ipPairSummary = IpPairSummary(ip1Info, ip2Info)

  override lazy val ipApiConnectionFlow = Flow[HttpRequest].map { request =>
    if (request.uri.toString().endsWith(ip1Info.query))
      HttpResponse(status = OK, entity = marshal(ip1Info))
    else if(request.uri.toString().endsWith(ip2Info.query))
      HttpResponse(status = OK, entity = marshal(ip2Info))
    else
      HttpResponse(status = BadRequest, entity = marshal("Bad ip format"))
  }

  "Service" should "respond to single IP query" in {
    Get(s"/ip/${ip1Info.query}") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpInfo] shouldBe ip1Info
    }

    Get(s"/ip/${ip2Info.query}") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpInfo] shouldBe ip2Info
    }
  }
}
