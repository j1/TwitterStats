package twitter4s.entities

// TODO - conversion to clean this representation? see url vs urls
// unfortunately Twitter is not consistent when representing this entity...

final case class Entities(hashtags: Seq[HashTag] = Seq.empty,
                          urls: Seq[UrlDetails] = Seq.empty)

final case class UrlDetails(url: String, expanded_url: String, display_url: String, indices: Seq[Int])
