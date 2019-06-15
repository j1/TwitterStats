package utils

import scala.io.Source
import scala.util.matching.Regex

object Emojis {

  type UnicodeCodePoints = Seq[Int]
  case class Emoji(shortName: Option[String],
                   codepoints: UnicodeCodePoints)
  extends Comparable[Emoji]
  {
    private [Emojis] def append(y: Emoji): Emoji = {
      assert(codepoints.isEmpty || y.codepoints.isEmpty)
      Emoji(shortName.orElse(y.shortName),
        codepoints ++ y.codepoints)
    }

    /** when ordering Seq[Int] assume head has highest significance */
    override def compareTo(that: Emoji): Int = {
      def compare(x: Seq[Int], y: Seq[Int], len: Int): Int =
        if (len==1) x.head.compareTo(y.head)
        else {
          val xHead::xTail = x
          val yHead::yTail = y
          val d = xHead.compareTo(yHead)
          if (d == 0) compare(xTail, yTail, len - 1)
          else d
        }

      val d = this.codepoints.length - that.codepoints.length
      if (d == 0) compare(this.codepoints, that.codepoints, this.codepoints.length)
      else d
    }
  }

  object Emoji {
    val empty = Emoji(None, Seq.empty)
  }

  lazy val emojiMap: Map[UnicodeCodePoints, Emoji] = parseEmojiPrettyJson()

  lazy val emojiRegEx: Regex = emojiRegExFrom(emojiMap)

  def emojisIn(s: String): Seq[Emoji] = {
    emojiRegEx.findAllIn(s).map { matchStr =>
      val codePoints = matchStr.codePoints().toArray
      emojiMap(codePoints)
    }.toSeq
  }

  private def emojiRegExFrom(emojiMap: Map[UnicodeCodePoints, Emoji]): Regex =
    emojiMap.foldLeft{
      val newSb = new java.lang.StringBuilder()
      newSb.ensureCapacity(emojiMap.size * 3); newSb
    }{ case (sb, (codePoints, _)) =>
      // regex for string representation of each emoji
      codePoints.foldLeft(sb)((sb, cp) =>
        sb.append('[') // to protect meta chars like * etc.
          .appendCodePoint(cp)
          .append(']')
      )
      // OR between emojis
      .append("|")
    }.toString.stripSuffix("|").r

  /**
    * Emoji Data from emoji-data prlject (https://github.com/iamcal/emoji-data)
    * ==NOTE==
    * - Each emoji is designated by a sequence of unicode code points in the json-field "unified".
    * - Emoji modifiers such as skin tone variations are considered to be the same emoji
    * - Emoji ZWJ sequences are considered as separate emojis
    */
  val EmojiJsonData = "emoji_pretty.json"

  /** uses circe to parse emoji_data, used for testing */
  private [utils] def parseEmojiJson(): Map[UnicodeCodePoints, Emoji] = {
    import better.files._
    import io.circe.generic.auto._
    import io.circe.parser._

    case class EmojiEntry(unified: String, short_name: String)
    def toEmoji(entry: EmojiEntry) = {
      val codepoints = entry.unified.split('-').map(Integer.parseInt(_, 0x10))
      Emoji(Some(entry.short_name), codepoints)
    }

    val path = Resource.getUrl(EmojiJsonData)
    val file = File(path)
    decode[List[EmojiEntry]](file.contentAsString)
      .map(_.map(toEmoji).map(emoji => (emoji.codepoints, emoji)).toMap)
      .getOrElse(Map.empty)
  }

  /**
    * Parses the json as text assuming that it is in the exact same pretty format as the RegEx below indicate.
    * Just parsing Json with circe is easier as in [[parseEmojiJson]], but this is more fun!
    **/
  private def parseEmojiPrettyJson(): Map[UnicodeCodePoints, Emoji] = {
    import ParserState._
    import Signal._
    import State._
    val OpenRegEx =
      raw"^    \{\s*".r
    val EmojiCodePointRegEx =
      """^        "unified": "((?:\w+[-"])+).*""".r
    val ShortNameRegEx =
      """^        "short_name": "([^"]+)".*""".r
    val CloseRegEx =
      raw"^    \}[\s,]*".r

    val in = Source.fromResource(EmojiJsonData, Emojis.getClass.getClassLoader)
    in.getLines().foldLeft(Closed(List.empty[Emoji]): State) { case (state, line) =>
      line match {
        case OpenRegEx() => state.transition(Open)
        case EmojiCodePointRegEx(str) =>
          state.transition(
            Accumulate(Emoji(
              shortName = None,
              codepoints = str.split(Array('-', '"')).map(Integer.parseInt(_, 0x10)))))
        case ShortNameRegEx(str) =>
          state.transition(
            Accumulate(Emoji(
              shortName = Some(str),
              codepoints = Seq.empty)))
        case CloseRegEx() =>
          state.transition(Close)
        case _ =>
          // status quo
          state
      }
    }.parsed.map(emoji => (emoji.codepoints, emoji)).toMap
  }

  private object ParserState {

    sealed trait Signal
    object Signal {
      case object Open extends Signal
      case class Accumulate(emoji: Emoji) extends Signal
      case object Close extends Signal
    }

    sealed trait State {
      val parsed: List[Emoji]
      /** emoji now being parsed */
      val current: Emoji = Emoji.empty

      def transition(signal: Signal): State = {
        import Signal._
        import State._
        (this, signal) match {
          // State Transitions:
          // (currentState, signal) => nextState
          case (Closed(emojis), Open) => Opened(emojis, Emoji.empty)
          case (Opened(emojis, x), Accumulate(y)) => Opened(emojis, x append y)
          case (Opened(emojis, x), Close) => Closed(emojis :+ x)
          case _ =>
            // stay in the same state
            this
        }
      }
    }

    object State {
      case class Closed(parsed: List[Emoji]) extends State
      case class Opened(parsed: List[Emoji], override val current: Emoji) extends State
    }
  }
}
