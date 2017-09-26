package pl.dwarszawski.search.adapter

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, _}
import akka.stream.ActorMaterializer
import pl.dwarszawski.search.adapter.GithubClientCommands.SearchProject
import pl.dwarszawski.search.adapter.GithubClientEvents.SearchProjectResult
import pl.dwarszawski.search.{GitHubResponse, Repository}

import scala.concurrent.Future

object GithubClient {
  def props(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): Props = Props(new GithubClient)
}

class GithubClient(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) extends Actor with ActorLogging {

  import actorSystem.dispatcher
  import akka.pattern.pipe
  import pl.dwarszawski.search.MarshallableImplicits._
  import pl.dwarszawski.search.HttpResponseImplicits._

  def receive = {
    case SearchProject(phrase, rownum) => searchProject(phrase, rownum).pipeTo(sender())
  }

  def searchProject(phrase: String, rownum: Int): Future[SearchProjectResult] = {

    val targetUri = s"https://api.github.com/search/repositories?q=$phrase"
    log.debug("Target uri: " + targetUri)

    val projects = for {
      response <- Http().singleRequest(HttpRequest(uri = targetUri))
      body <- response.toUtf8String
    } yield body.fromJson[GitHubResponse]

    projects.map(v => SearchProjectResult(v.items.take(rownum)))
  }
}

object GithubClientCommands {

  case class SearchProject(phrase: String, rownum: Int)

}

object GithubClientEvents {

  case class SearchProjectResult(projects: Seq[Repository])

}