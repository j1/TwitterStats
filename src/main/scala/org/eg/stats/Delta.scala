package org.eg.stats

import java.net.URI
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

import io.circe.Decoder.Result
import io.circe.Json
import twitter4s.entities.{HashTag, Tweet}

/** Tweet stats delta from one Tweet */
case class Delta1
(
  created_at: String,

  /** The following may contain duplicates of emojis, hashtags etc.*/
  emojis: Seq[Char],
  hashtags: Seq[String],
  urls: Seq[URI]
) {
  def hasEmoji: Boolean = ???
  def hasUrl: Boolean = ???
  def hasPhotoUrl: Boolean = ???
}

object Delta1 {
  def isPhotoUrl(url: URI): Boolean = ???
  def domainOf(url: URI): String = ???

  private val TwitterFmt = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss ZZZ yyyy")

  def str2instant(str: String): Instant = ZonedDateTime.parse(str, TwitterFmt).toInstant
  def instant2Str(t: Instant): String = ZonedDateTime.ofInstant(t, ZoneId.of("+0000"))
    .format(TwitterFmt)

  val empty = Delta1(created_at = instant2Str(Instant.EPOCH),
    emojis = Seq.empty[Char],
    hashtags = Seq.empty[String],
    urls = Seq.empty[URI])

  def fromTweet(json: Json): Result[Delta1] = {
    import io.circe.generic.auto._, io.circe.syntax._
    val tweetResult: Result[Tweet] = json.as[Tweet]
    tweetResult.map {tweet =>
      empty.copy(created_at = tweet.created_at)
    }
  }
}

/** sum of deltas from N tweets */
case class DeltaN()

