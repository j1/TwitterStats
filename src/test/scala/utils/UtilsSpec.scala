package utils

import java.net.URI

import org.scalatest.{Matchers, WordSpec}
import Emojis.Emoji
import Utils.domainOf

/**
  * tests of various helper methods
  * Learn to laugh 😆 at yourself! Everybody is doing it anyway 🤣
  */
class UtilsSpec extends WordSpec with Matchers {
  "emojis in text" should {
    "parse emoji json" in {
      val bicyclistEmoji = Emojis.emojiMap.find{
          case (_, Emoji(shortName, _)) => shortName.contains("bicyclist") }.get._2
      bicyclistEmoji.codepoints shouldEqual Seq(0x1F6B4)

      val zeroEmoji = Emojis.emojiMap.find{
        case (_, Emoji(shortName, _)) => shortName.contains("zero") }.get._2
      zeroEmoji.codepoints shouldEqual  Seq(0x0030, 0xFE0F, 0x20E3)

      Emojis.emojiMap shouldEqual
        Emojis.parseEmojiJson()
    }

    "count emojis correctly" in {
      val with2emojis = "Learn to laugh 😆 at yourself! Everybody is doing it anyway 🤣"
      val with2Emojis2 = "Learn to laugh \uD83D\uDE06 at yourself! Everybody is doing it anyway \uD83E\uDD23"
      with2emojis shouldEqual with2Emojis2

      val noEmoji = "Life with neither joy nor sorrow is death"

      Emojis.emojisIn(noEmoji) shouldEqual Seq.empty
      Emojis.emojisIn(with2emojis).map(_.shortName.getOrElse("")) should contain theSameElementsAs
        Seq("laughing", "rolling_on_the_floor_laughing")

      val threeEmojis = "🔫🛎✂️🚁"
      Emojis.emojisIn(threeEmojis).map(_.shortName.getOrElse("")) should contain theSameElementsAs
        Seq("gun", "scissors", "helicopter")
    }
  }
  "domainOf method" should {
    "extract domain" in {
      domainOf(new URI("http://www.yahoo.com")) shouldEqual "yahoo.com"
      domainOf(new URI("http://t.co")) shouldEqual "t.co"
    }
    "handle invalid host" in {
      domainOf("ha ha") shouldEqual None
      domainOf("") shouldEqual None
    }
    "handle domains like x.co.uk" in {
      domainOf(new URI("http://www.bp.co.uk")) shouldEqual "bp.co.uk"
      domainOf(new URI("http://abcd.aws.amazon.com")) shouldEqual "amazon.com"
      domainOf(new URI("http://a.b.c.d")) shouldEqual "c.d"
    }
  }
}
