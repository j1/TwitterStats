package twitter4s.entities

import twitter4s.entities.streaming.UserStreamingMessage
import twitter4s.entities.streaming.UserStreamingMessage

final case class DirectMessageEventList(events: List[DirectMessageEvent],
                                        apps: Map[String, Apps] = Map.empty,
                                        next_cursor: Option[String])
    extends UserStreamingMessage
