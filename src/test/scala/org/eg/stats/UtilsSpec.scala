package org.eg.stats

import java.net.URI

import org.scalatest.{Matchers, WordSpec}
import utils.Emojis

import scala.io.Source

/**
  * tests of various helper methods
  */
class UtilsSpec extends WordSpec with Matchers {
  "emojis in text" should {
    "parse emoji json" in {
      val EmojiUnifiedRegEx =
        """[ ]*"unified": "(\w+)[-"].*""".r

      val in = Source.fromResource("emoji_pretty.json", Emojis.getClass.getClassLoader)
      in.getLines().foreach {
        case EmojiUnifiedRegEx(str) =>
          Integer.parseInt(str,0x10).toHexString.toLowerCase() shouldEqual str.dropWhile(_=='0').toLowerCase
        case _ => // ignore
      }
    }
  }
  "domainOf method" should {
    import Delta1._
    "extract domain" in {
      domainOf(new URI("http://www.yahoo.com")) shouldEqual "yahoo.com"
      domainOf(new URI("http://t.co")) shouldEqual "t.co"
    }
    "handle invalid host" in {
      domainOf("ha ha") shouldEqual None
      domainOf("") shouldEqual None
    }
  }
}
