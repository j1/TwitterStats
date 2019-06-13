package org.eg.stats

import java.awt.dnd.InvalidDnDOperationException
import java.net.URI
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

import io.circe.Decoder.Result
import io.circe.Json
import twitter4s.entities.{HashTag, Tweet}
import utils.Emojis

import scala.util.Try

/** Tweet stats delta from one Tweet */
case class Delta1
(
  created_at: String,

  /** The following may contain duplicates of emojis, hashtags etc.*/
  emojis: Seq[Int], // unicode codepoints
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
  def domainOf(url: URI): String = domainOf(url.getHost)
    .getOrElse(InvalidDomain)

  val InvalidDomain = "null.invalid"

  private [stats] def domainOf(host: String): Option[String] = {
    val Dot = '.'
    val lastDotPos = host.lastIndexOf(Dot)
    val secondLastDotPos = host.lastIndexWhere(_==Dot, lastDotPos-1)
    val domainStartPos = if (secondLastDotPos < 0) 0 else secondLastDotPos + 1
    if(lastDotPos >= 0) Some(host.substring(domainStartPos, host.length)) else None
  }

  private val TwitterFmt = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss ZZZ yyyy")

  def str2instant(str: String): Try[Instant] = Try(ZonedDateTime.parse(str, TwitterFmt).toInstant)
  def instant2Str(t: Instant): String = ZonedDateTime.ofInstant(t, ZoneId.of("+0000"))
    .format(TwitterFmt)

  val empty = Delta1(created_at = instant2Str(Instant.EPOCH),
    emojis = Seq.empty[Int],
    hashtags = Seq.empty[String],
    urls = Seq.empty[URI])

  def fromTweet(json: Json): Result[Delta1] = {
    import io.circe.generic.auto._, io.circe.syntax._
    val tweet: Result[Tweet] = json.as[Tweet]
    val emojis = Seq()//Emojis.get
    tweet.map {t =>
      val (text, entities) = t.textAndEntities
      val emojis = Seq()
      Delta1(
        created_at = t.created_at,
        emojis = emojis,
        hashtags = entities.fold(
          ifEmpty = Seq.empty[String])(
          _.hashtags.map(_.text)),
        urls = entities.fold(
          ifEmpty = Seq.empty[URI])(
          _.urls.map(x => new URI(x.url)))
      )
    }
  }
}

/** sum of deltas from N tweets */
case class DeltaN()

