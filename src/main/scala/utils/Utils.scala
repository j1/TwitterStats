package utils

import java.net.URI
import java.time.Instant

import com.google.common.net.InternetDomainName

import scala.util.Try

object Utils {
  def laterOf(x: Instant, y: Instant): Instant = if (x.isAfter(y)) x else y

  def percentOf(x: Long, total: Long): Float = if (x == 0) 0 else {
    x.toDouble / total
  }.toFloat

  def countIf(condition: Boolean): Int = if (condition) 1 else 0

  /**
    * NOTE: as explained in
    * [[https://github.com/google/guava/wiki/InternetDomainNameExplained]]
    * TLDs, registry suffixes, and public suffixes are not the same thing.
    *
    * We want the "domain", which is the domain registered under "registry suffix".
    * As an example:
    * host = foo.blogspot.com
    * topPrivateDomain (under public suffix) = foo.blogspot.com
    * topDomainUnderRegistry (under registry suffix) = blogspot.com
    */
  def domainOf(url: URI): String =  {
    val host = url.getHost

    Try(InternetDomainName.from(host)
      .topDomainUnderRegistrySuffix()
      .toString
    ).getOrElse(
      domainOf(host)
        .getOrElse(InvalidDomain))
  }

  val InvalidDomain = "null.invalid"

  private [utils] def domainOf(host: String): Option[String] = {
    val Dot = '.'
    val lastDotPos = host.lastIndexOf(Dot)
    val secondLastDotPos = host.lastIndexWhere(_==Dot, lastDotPos-1)
    val domainStartPos = if (secondLastDotPos < 0) 0 else secondLastDotPos + 1
    if(lastDotPos >= 0) Some(host.substring(domainStartPos, host.length)) else None
  }
}