package twitter4s.entities

final case class Tweet(created_at: String,
                       entities: Option[Entities] = None,
                       extended_tweet: Option[ExtendedTweet] = None,
                       favorite_count: Int = 0,
                       favorited: Boolean = false,
                       id: Long,
                       lang: Option[String] = None,
                       retweet_count: Long = 0,
                       retweeted: Boolean = false,
                       text: String,
                       truncated: Boolean = false
                      )
{
  def textAndEntities: (String, Option[Entities]) = {
    // if it is truncated, use the extended_tweet
    // per https://developer.twitter.com/en/docs/tweets/tweet-updates
    if(truncated) extended_tweet.fold(
      ifEmpty = (text, entities)
    )(ext => (ext.full_text, ext.entities))
    else (text, entities)
  }
}
