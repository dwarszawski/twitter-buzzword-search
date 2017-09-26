package pl.dwarszawski

package object search {

  case class GitHubResponse(items: List[Repository])

  case class Repository(name: String, full_name: String, url: String, description: String)

  case class TwitterResponse(statuses: List[Tweet])

  case class Tweet(created_at: String, text: String, user: User)

  case class User(name: String, screen_name: String)

  case class BearerToken(token_type: String, access_token: String)
}
