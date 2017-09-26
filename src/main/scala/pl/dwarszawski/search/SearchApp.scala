package pl.dwarszawski.search

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import pl.dwarszawski.search.adapter.GithubClientCommands.SearchProject
import pl.dwarszawski.search.adapter.GithubClientEvents.SearchProjectResult
import pl.dwarszawski.search.adapter.TwitterClientCommands.{GenerateToken, SearchTweet}
import pl.dwarszawski.search.adapter.TwitterClientEvents.SearchTweetResult
import pl.dwarszawski.search.adapter.{GithubClient, TwitterClient}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object SearchApp extends App {
  val config = ConfigFactory.load()

  implicit val system = ActorSystem("BuzzWordsSearch")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  implicit val timeout: Timeout = config.getInt("timeout").seconds

  try {
    val githubClient = system.actorOf(GithubClient.props)
    val projects = (githubClient ? SearchProject("reactive", config.getInt("github.limit"))).mapTo[SearchProjectResult]

    val twitterClient = system.actorOf(TwitterClient.props)
    val token = (twitterClient ? GenerateToken(config.getString("twitter.key"), config.getString("twitter.secret"))).mapTo[BearerToken]

    val searchResult = for {
      p <- projects
      t <- token
      pr = p.projects
      ps <- Future.sequence(pr.map(proj => (twitterClient ? SearchTweet(proj.full_name, t.access_token)).mapTo[SearchTweetResult]))
    } yield ps

    import pl.dwarszawski.search.MarshallableImplicits._

    Await.result(searchResult, 10.seconds).sortBy(_.tweets.size).reverse.foreach { rs =>
      println(rs.toJson)
    }
  } finally {
    Http().shutdownAllConnectionPools().onComplete { _ =>
      materializer.shutdown()
      system.terminate() // produce harmless errors (https://github.com/akka/akka-http/issues/497)
    }
  }
}
