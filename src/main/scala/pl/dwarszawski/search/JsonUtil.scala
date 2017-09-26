package pl.dwarszawski.search

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scala.concurrent.Future

object JsonUtil {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.enable(SerializationFeature.INDENT_OUTPUT)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }
}

object MarshallableImplicits {

  implicit class Unmarshallable(json: String) {
    def fromJson[T]()(implicit m: Manifest[T]): T = JsonUtil.fromJson[T](json)
  }

  implicit class Marshallable[T](obj: T) {
    def toJson: String = JsonUtil.toJson(obj)
  }

}

object HttpResponseImplicits {

  implicit class ReadHttpResposne(httpResponse: HttpResponse)(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) {

    import actorSystem.dispatcher

    def toUtf8String: Future[String] = httpResponse.entity.dataBytes.runFold(ByteString.empty) { case (acc, b) => acc ++ b }.map(_.utf8String)
  }

}

