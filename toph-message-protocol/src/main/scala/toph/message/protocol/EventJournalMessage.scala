package toph.message.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveAllCodecs

object EventJournalMessage:

  given Codec[EventJournalMessage] = deriveAllCodecs

sealed trait EventJournalMessage

case class NewEvent(key: String) extends EventJournalMessage
