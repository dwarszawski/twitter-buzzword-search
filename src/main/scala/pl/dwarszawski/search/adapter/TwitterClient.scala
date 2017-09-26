package pl.dwarszawski.search.adapter

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import pl.dwarszawski.search.adapter.TwitterClientCommands.{GenerateToken, SearchTweet}
import pl.dwarszawski.search.adapter.TwitterClientEvents.SearchTweetResult
import pl.dwarszawski.search.{BearerToken, Tweet, TwitterResponse}


object TwitterClient {
  def props(implicit actorSystem: ActorSystem, materializer: ActorMaterializer): Props = Props(new TwitterClient)
}

class TwitterClient(implicit actorSystem: ActorSystem, materializer: ActorMaterializer) extends Actor with ActorLogging {

  import actorSystem.dispatcher
  import akka.pattern.pipe
  import pl.dwarszawski.search.MarshallableImplicits._
  import pl.dwarszawski.search.HttpResponseImplicits._

  def receive = {
    case GenerateToken(key, secret) =>
      authorize(key, secret).pipeTo(sender())

    case SearchTweet(phrase, token) =>
      search(phrase, token).map(tr => SearchTweetResult(phrase, tr.statuses)).pipeTo(sender())
  }

  private def authorize(key: String, secret: String) = {
    log.debug(s"Generating bearer token using $key:$secret)")
    for {
      response <- Http().singleRequest(
        HttpRequest(uri = "https://api.twitter.com/oauth2/token")
          .withMethod(HttpMethods.POST)
          .withEntity(FormData("grant_type" -> "client_credentials").toEntity)
          .withHeaders(Authorization(BasicHttpCredentials(key, secret)))
      )
      body <- response.toUtf8String
    } yield body.fromJson[BearerToken]
  }

  private def search(phrase: String, token: String) = {
    log.debug(s"Searching twitter for $phrase")
    for {
      response <- Http().singleRequest(
        HttpRequest(uri = s"https://api.twitter.com/1.1/search/tweets.json?q=$phrase")
          .withHeaders(Authorization(OAuth2BearerToken(token)))
      )
      body <- response.toUtf8String
    } yield body.fromJson[TwitterResponse]
  }
}

object TwitterClientCommands {

  case class GenerateToken(key: String, secret: String)

  case class SearchTweet(phrase: String, token: String)

}

object TwitterClientEvents {

  case class BearerToken(token: String)

  case class SearchTweetResult(phrase: String, tweets: Seq[Tweet])

}