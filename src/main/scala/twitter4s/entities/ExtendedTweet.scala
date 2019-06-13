package twitter4s.entities

final case class ExtendedTweet(full_text: String,
                               entities: Option[Entities] = None)
