package org.eg.stats

import java.awt.dnd.InvalidDnDOperationException
import java.net.URI
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

import io.circe.Decoder.Result
import io.circe.Json
import twitter4s.entities.{HashTag, Tweet}
import utils.Emojis
import utils.Emojis.Emoji

import scala.util.Try

/** Tweet stats delta from one Tweet */
case class Delta1
(
  created_at: String,

  /** The following may contain duplicates of emojis, hashtags etc.*/
  emojis: Seq[Emoji],
  hashtags: Seq[String],
  urls: Seq[URI]
) {
  import Delta1._

  lazy val created_instant: Try[Instant] = str2instant(created_at)

  def hasEmoji: Boolean = emojis.nonEmpty
  def hasUrl: Boolean = urls.nonEmpty
  def hasPhotoUrl: Boolean = urls.exists(isPhotoUrl)
}

object Delta1 {

  val KnownPhotoDomains: Seq[String] = Seq(
    "pic.twitter.com", "pbs.twimg.com", "instagram.com"
  )
  def isPhotoUrl(url: URI): Boolean = KnownPhotoDomains.exists(url.getHost.contains(_))

  private val TwitterFmt = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss ZZZ yyyy")

  def str2instant(str: String): Try[Instant] = Try(ZonedDateTime.parse(str, TwitterFmt).toInstant)
  def instant2Str(t: Instant): String = ZonedDateTime.ofInstant(t, ZoneId.of("+0000"))
    .format(TwitterFmt)

  val empty = Delta1(created_at = instant2Str(Instant.EPOCH),
    emojis = Seq.empty[Emoji],
    hashtags = Seq.empty[String],
    urls = Seq.empty[URI])

  def fromTweet(json: Json): Result[Delta1] = {
    import io.circe.generic.auto._, io.circe.syntax._
    val tweet: Result[Tweet] = json.as[Tweet]
    tweet.map {t =>
      val (text, entities) = t.textAndEntities
      val emojis = Seq()
      Delta1(
        created_at = t.created_at,
        emojis = Emojis.emojisIn(text),
        hashtags = entities.fold(
          ifEmpty = Seq.empty[String])(
          _.hashtags.map(_.text)),
        urls = entities.fold(
          ifEmpty = Seq.empty[URI])(
          //entities.urls.url is of the form "t.co/...", so use expanded_url to get the original URL
          _.urls.map(x => new URI(x.expanded_url)))
      )
    }
  }
}

/** sum of deltas from N tweets */
case class DeltaN()

