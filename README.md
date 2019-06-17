# Twitter Statistics

# Design Rationale
functional: streams and effects
actors (akka) and futures
kafka distrbuted messaging & spark streams

# Design

# Assumptions
## Scope
Subscribed to one  sample endpoint and statistics are derived asynchronously from the tweet-stream on one node (virtual machine).
The stats are in memory and are mutable variables, modified by tasks in a separate executor from the stream that consumes tweets.

## Deriving Satistics
Follows a natural map reduce of streams
Tweet -> Delta
Delta Stream -> Sum of Deltas -> Tweet Statistics

##Statistics calculation

###Average Tweet Rate
Average tweet rate that is asked in the problem description by dividing the total tweet with how much time elapsed from the beginning.

### Emoji Parsing
- Each emoji is designated by a sequence of unicode code points in the json-field "unified" of emoji_data.json
  * Emoji modifiers such as skin tone variations are considered to be the same emoji
  * Emoji ZWJ sequences are considered as separate emojis

# Further Work
