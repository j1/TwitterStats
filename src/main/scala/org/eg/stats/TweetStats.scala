package org.eg.stats

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference

import cats.Applicative
import cats.effect.Sync
import utils.Emojis.Emoji
import utils.Ring

import scala.collection.{SortedMap, SortedSet}
import scala.util.Try

case class TweetStatsPresentation(
  totalCount: Long,
  startedAt: Instant,
  lastTweetAt: Instant,
  currentRate: TweetRate,
  averageRate: TweetRate,
  topEmojis: Seq[(Emoji, Int)],
  percentWithEmojis: Float,
  topHashTags: Seq[(String, Int)],
  percentWithUrl: Float,
  percentWithPhoto: Float,
  topDomains: Seq[(String, Int)]
)

final case class TweetStats (
                              totalCount: Long,
                              latestTweetAt: Instant,
                              recentCounts: RecentCounts,
                              topEmojis: TopCounts[Emoji],
                              totalWithEmoji: Long,
                              topHashTags: TopCounts[String],
                              totalWithUrl: Long,
                              totalWithPhotoUrl: Long,
                              topDomains: TopCounts[String]
) {
  import TweetStats._
  import utils.Utils._

  def add(delta: Delta1): TweetStats = {
    val createdAt = delta.created_instant.getOrElse(Instant.now)
    TweetStats(
      totalCount = totalCount + 1,
      latestTweetAt = delta.created_instant
        .fold(_ => latestTweetAt, x => laterOf(latestTweetAt, x)),
      recentCounts = recentCounts.countThis(delta.created_instant),
      topEmojis = topEmojis.add(delta.emojis, tweetedAt = createdAt),
      totalWithEmoji = totalWithEmoji + countIf(delta.hasEmoji),
      topHashTags = topHashTags.add(delta.hashtags, tweetedAt = createdAt),
      totalWithUrl = totalWithPhotoUrl + countIf(delta.hasUrl),
      totalWithPhotoUrl = totalWithPhotoUrl + countIf(delta.hasPhotoUrl),
      topDomains = topDomains.add(delta.urls.map(domainOf), tweetedAt = createdAt)
    )
  }

  def present: TweetStatsPresentation = TweetStatsPresentation(
    totalCount = totalCount,
    lastTweetAt = latestTweetAt,
    startedAt = TweetStats.startedAt.get.getOrElse(Instant.EPOCH),
    currentRate = recentCounts.rate,
    averageRate = averageRate,
    topEmojis = topEmojis.top,
    percentWithEmojis = percentOf(totalWithEmoji, totalCount),
    topHashTags = topHashTags.top,
    percentWithUrl = percentOf(totalWithUrl, totalCount),
    percentWithPhoto = percentOf(totalWithPhotoUrl, totalCount),
    topDomains = topDomains.top
  )

  private def averageRate: TweetRate = startedAt.get.fold(ifEmpty = TweetRate.empty) { start =>
    val duration = start.until(latestTweetAt, ChronoUnit.MILLIS)
    if (duration ==0) TweetRate.empty
    else TweetRate(
      perHour = (totalCount * 1000 * 3600 / duration).toInt,
      perMin = (totalCount * 1000 * 60 / duration).toInt,
      perSec = (totalCount * 1000 / duration).toInt
    )
  }
}

object TweetStats {

  val empty = TweetStats(totalCount = 0, latestTweetAt = Instant.EPOCH,
    recentCounts = RecentCounts.empty,
    topEmojis = TopCounts.empty,
    totalWithEmoji = 0,
    topHashTags = TopCounts.empty,
    totalWithUrl = 0,
    totalWithPhotoUrl = 0,
    topDomains = TopCounts.empty)

  private[stats] val currentStats = new AtomicReference[TweetStats](empty)

  val startedAt: AtomicReference[Option[Instant]] = new AtomicReference(None)

  /** @return updated tweet-stats */
  private[stats] def accumulate(delta: Delta1): TweetStats = {
    // record the arrival time of first tweet
    startedAt.compareAndSet(
      /* expect */ None,
      /* update */ Some(Instant.now))

    currentStats.accumulateAndGet(empty,
      (current: TweetStats, _) => current.add(delta)
    )}
}

/**
  * keeps a running window of cumulative counts for the past 1hour == 3600sec
  * @param count count of tweets in each tick, for 359 seconds
  * @param lastTick optional pair of:
  *                 instant when last tick started, in epoch seconds
  *                 count in the lastTick, which may not be filled up yet
  **/
case class RecentCounts(count: Ring[Int],
                        lastTick: Option[(Long, Int)])
{
  def countThis(created_instant: Try[Instant]): RecentCounts = created_instant.fold(
    _ => this.copy(lastTick = lastTick.map{ case (tick, countPerTick) => (tick, countPerTick + 1)}),
    t => {
      this.lastTick.map{ case (lastTick: Long, lastCount) =>
        if (t.getEpochSecond > lastTick) {
          // increment the lastTick
          ???
        } else {
          // push the lastTick into the ringh and start a new lastTick
          ???
        }
      }
      this
    }
  )

  def rate: TweetRate = TweetRate.empty

}
object RecentCounts {
  val empty = RecentCounts(count = Ring[Int](359)(), lastTick = None)
}

/**
  * @param countMap a sorted map of:
  *   item (e.g. hashtag) ==> (from, to, count)
  *   count = number of tweets between (from, to)
  * @param ordering a sorted set of the same elements as countMap. However,
  *                 these are sorted so that high priority elements are last. So,
  *                 ordering.head is the least priority element.
  */
case class TopCounts[T](countMap: SortedMap[T, (Instant, Instant, Int)],
                        ordering: SortedSet[TopCounts.Entry[T]]) {
  import utils.Utils.laterOf
  import TopCounts._

  /** item is either hashtag or domain */
  def add(items: Seq[T], tweetedAt: Instant): TopCounts[T] = items.foldLeft(this){ (out: TopCounts[T], item: T) =>
      val entry = out.countMap.get(item).map(item -> _)
      entry.fold{
        val newEntry = item -> (tweetedAt, tweetedAt, 1)
        val N = out.countMap.size
        assert(N <= KEEP_NO_MORETHAN && N == out.ordering.size)
        if (N < KEEP_NO_MORETHAN)
          out.copy(
            countMap = out.countMap + newEntry,
            ordering = out.ordering + newEntry)
        else {
          // TODO make room for newEntry in bulk...
          val rmEntry = out.ordering.head
          out.copy(
            countMap = out.countMap - rmEntry._1 + newEntry,
            ordering = out.ordering - rmEntry + newEntry)
        }
      }{ entry =>
        // increment the count of entry and update the timestamp
        assert(out.ordering.contains(entry))
        val (_, (from, to, count)) = entry
        val entryUpdated = entry._1 -> (from, laterOf(to, tweetedAt), count + 1)
        out.copy(
          countMap = out.countMap + entryUpdated,
          ordering = out.ordering - entry + entryUpdated)
      }
  }

  def top: Seq[(T, Int)] = {
    countMap.toSeq.map{ case (item, (_, _, count)) => (item, count) }
      .sortBy(- _._2) // descending order by count
      .take(TOP_COUNT)
  }
}
object TopCounts {
  /** item (e.g. hashtag) -->  (from, to, count)*/
  type Entry[T] = (T, (Instant, Instant, Int))

  def empty[T <: Comparable[T]]: TopCounts[T] = TopCounts[T](
    countMap = SortedMap.empty[T, (Instant, Instant, Int)],
    ordering = SortedSet.empty(new PriorityOrdering[T]))

  val KEEP_NO_MORETHAN = 1000
  val TOP_COUNT = 10

  class PriorityOrdering[T <: Comparable[T]] extends Ordering[Entry[T]] {
    // return positive if x is higher priority than y, for int this is x - y
    override def compare(x: Entry[T], y: Entry[T]): Int = {
      // TODO we want to remove stale entries that have been sitting for long time, but not changing their count
      //      if they are not in the top. The idea is to keep those items which have a "potential" to grow.
      //      Has to come up with better algorithm
      val (item1, (t1, _,n1)) = x
      val (item2, (t2, _,n2)) = y

      val dn = n1 - n2 // higher counts preferred
      if (dn == 0) {
        val dt = t1.compareTo(t2) // // order by the youth, i.e. when this item started counting. newer preferred
        if (dt == 0) item1.compareTo(item2) // need this for ordering the set without collisions
        else dt
      } else dn
    }
  }

}

case class TweetRate(perHour: Int, perMin: Int, perSec: Int)
object TweetRate {
  val empty = TweetRate(0, 0, 0)
}

object TweetStatsPresentation {

  import Delta1.{instant2Str, str2instant}
  import io.circe._
  import io.circe.generic.semiauto._
  import org.http4s.circe._
  import org.http4s.{EntityDecoder, EntityEncoder}
  val empty = TweetStatsPresentation(
    totalCount = 0, startedAt = Instant.EPOCH, lastTweetAt = Instant.EPOCH,
    currentRate = TweetRate.empty, averageRate = TweetRate.empty,
    topEmojis = Seq.empty, percentWithEmojis = 0,
    topHashTags = Seq.empty,
    percentWithPhoto = 0,
    percentWithUrl = 0,
    topDomains = Seq.empty
  )

  implicit val encodeInstant: Encoder[Instant] = Encoder.encodeString.contramap[Instant](instant2Str)
  implicit val decodeInstant: Decoder[Instant] = Decoder.decodeString.emap { str =>
    str2instant(str).toEither.left.map(_.toString)
  }

  implicit val rateDecoder: Decoder[TweetRate] = deriveDecoder
  implicit val rateEncoder: Encoder[TweetRate] = deriveEncoder

  implicit val emojiDecoder: Decoder[Emoji] = deriveDecoder
  implicit val emojiEncoder: Encoder[Emoji] = deriveEncoder

  implicit val tweetStatsDecoder: Decoder[TweetStatsPresentation] = deriveDecoder
  implicit def tweetStatsEntityDecoder[F[_]: Sync]: EntityDecoder[F, TweetStatsPresentation] =
    jsonOf
  implicit val tweetStatsEncoder: Encoder[TweetStatsPresentation] = deriveEncoder
  implicit def tweetStatsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, TweetStatsPresentation] =
    jsonEncoderOf
}
