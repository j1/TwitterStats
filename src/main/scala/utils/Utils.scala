package utils

import java.net.URI
import java.time.Instant

object Utils {
  def laterOf(x: Instant, y: Instant): Instant = if (x.isAfter(y)) x else y

  def percentOf(x: Long, total: Long): Float = if (x == 0) 0 else {
    x.toDouble / total
  }.toFloat

  def countIf(condition: Boolean): Int = if (condition) 1 else 0

  def domainOf(url: URI): String = domainOf(url.getHost)
    .getOrElse(InvalidDomain)

  val InvalidDomain = "null.invalid"

  private [utils] def domainOf(host: String): Option[String] = {
    val Dot = '.'
    val lastDotPos = host.lastIndexOf(Dot)
    val secondLastDotPos = host.lastIndexWhere(_==Dot, lastDotPos-1)
    val domainStartPos = if (secondLastDotPos < 0) 0 else secondLastDotPos + 1
    if(lastDotPos >= 0) Some(host.substring(domainStartPos, host.length)) else None
  }
}