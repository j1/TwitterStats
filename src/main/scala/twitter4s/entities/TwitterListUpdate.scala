package twitter4s.entities

import twitter4s.entities.enums.Mode.Mode

final case class TwitterListUpdate(description: Option[String] = None,
                                   mode: Option[Mode] = None,
                                   name: Option[String] = None)
