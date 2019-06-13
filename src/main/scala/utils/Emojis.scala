package utils

import scala.io.Source

object Emojis {

  case class Emoji(shortName: Option[String], codepoints:Seq[Int]) {
    def append(y: Emoji): Emoji = {
     Emoji(shortName.orElse(y.shortName),
       codepoints ++ y.codepoints)
    }
  }
  object Emoji {
    val empty = Emoji(None, Seq.empty)
  }

  private def parseEmojiJson(): Seq[Emoji] = {
    import ParserState._; import Signal._; import State._
    val OpenRegEx =
      "    {\\s*"
    val EmojiCodePointRegEx =
      s"""[ ]*"unified": "(\w+[-"])+.*""".r
    val ShortNameRegEx =
      """[ ]*"short_name": "(\w+)".*""".r
    val CloseRegEx =
      "    }[\\s,]*"

    val in = Source.fromResource("emoji_pretty.json", Emojis.getClass.getClassLoader)
    in.getLines().foldLeft(Empty: State, Seq.empty[Emoji]) {
      case ((state, output), line) => line match {
        case OpenRegEx =>
          val (nextState, out) = transition(state, Open)
          (nextState, output ++ out)
        //case EmojiCodePointRegEx(str) =>
          // Some(Integer.parseInt(str, 0x10))
        //case _ => None
      }
    }._2
  }

  private object ParserState {

    sealed trait Signal
    object Signal {
      case object Open extends Signal
      case class Accumulate(emoji: Emoji) extends Signal
      case object Close extends Signal
    }

    sealed trait State
    object State {
      case object Empty extends State
      case object Opened extends State
      case class Accumulating(emoji: Emoji) extends State
    }

    def transition(current: State, signal: Signal): (State, Option[Emoji]) = {
      import Signal._; import State._
      (current, signal) match {
        // State Transitions:
        // (currentState, signal) => (nextState, output)
        case (Empty, Open) => (Opened, None)
        case (Opened, Accumulate(emoji)) => (Accumulating(emoji), None)
        case (Accumulating(emoji1), Accumulate(emoji2)) => (Accumulating(emoji1.append(emoji2)), None)
        case (Accumulating(emoji), Close) => (Empty, Some(emoji))
        case (state, _) =>
          // stay in the same state
          (state, None)
      }
    }
  }
}
