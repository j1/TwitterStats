package utils

import java.time.Instant

object Utils {
  def laterOf(x: Instant, y: Instant): Instant = if (x.isAfter(y)) x else y

  def percentOf(x: Long, total: Long): Float = if (x == 0) 0 else {
    x.toDouble / total
  }.toFloat

  def countIf(condition: Boolean): Int = if (condition) 1 else 0
}