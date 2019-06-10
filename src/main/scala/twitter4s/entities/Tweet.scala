package twitter4s.entities

import java.time.Instant

import twitter4s.entities.streaming.CommonStreamingMessage
import twitter4s.entities.streaming.CommonStreamingMessage

final case class Tweet(contributors: Seq[Contributor] = Seq.empty,
                       coordinates: Option[Coordinates] = None,
                       created_at: Instant,
                       current_user_retweet: Option[TweetId] = None,
                       entities: Option[Entities] = None,
                       extended_entities: Option[Entities] = None,
                       extended_tweet: Option[ExtendedTweet] = None,
                       favorite_count: Int = 0,
                       favorited: Boolean = false,
                       filter_level: Option[String] = None,
                       geo: Option[Geo] = None,
                       id: Long,
                       id_str: String,
                       in_reply_to_screen_name: Option[String] = None,
                       in_reply_to_status_id: Option[Long] = None,
                       in_reply_to_status_id_str: Option[String] = None,
                       in_reply_to_user_id: Option[Long] = None,
                       in_reply_to_user_id_str: Option[String] = None,
                       is_quote_status: Boolean = false,
                       lang: Option[String] = None,
                       place: Option[GeoPlace] = None,
                       possibly_sensitive: Boolean = false,
                       quoted_status_id: Option[Long] = None,
                       quoted_status_id_str: Option[String] = None,
                       quoted_status: Option[Tweet] = None,
                       scopes: Map[String, Boolean] = Map.empty,
                       retweet_count: Long = 0,
                       retweeted: Boolean = false,
                       retweeted_status: Option[Tweet] = None,
                       source: String,
                       text: String,
                       truncated: Boolean = false,
                       display_text_range: Option[Seq[Int]] = None,
                       user: Option[User] = None,
                       withheld_copyright: Boolean = false,
                       withheld_in_countries: Seq[String] = Seq.empty,
                       withheld_scope: Option[String] = None,
                       metadata: Option[StatusMetadata] = None)
    extends CommonStreamingMessage
