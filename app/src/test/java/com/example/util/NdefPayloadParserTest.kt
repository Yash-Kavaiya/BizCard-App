package com.example.util

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NdefPayloadParserTest {

  private val vCard = "BEGIN:VCARD\nVERSION:3.0\nFN:Test User\nEND:VCARD"

  @Test
  fun vCardMimeRecord_isParsedAsVCard() {
    val message = NdefMessage(NdefRecord.createMime("text/vcard", vCard.toByteArray()))

    assertEquals(NdefPayload.VCard(vCard), NdefPayloadParser.parse(message))
  }

  @Test
  fun vCardInTextRecord_isParsedAsVCard() {
    val message = NdefMessage(NdefRecord.createTextRecord("en", vCard))

    assertEquals(NdefPayload.VCard(vCard), NdefPayloadParser.parse(message))
  }

  @Test
  fun uriRecord_isDecodedWithPrefix() {
    val message = NdefMessage(NdefRecord.createUri("https://example.com/card/jane"))

    assertEquals(NdefPayload.Url("https://example.com/card/jane"), NdefPayloadParser.parse(message))
  }

  @Test
  fun bareWwwText_isNormalisedToHttps() {
    val message = NdefMessage(NdefRecord.createTextRecord("en", "www.example.com"))

    assertEquals(NdefPayload.Url("https://www.example.com"), NdefPayloadParser.parse(message))
  }

  @Test
  fun vCardWinsOverEarlierUrlRecord() {
    val message =
      NdefMessage(
        NdefRecord.createUri("https://example.com"),
        NdefRecord.createMime("text/vcard", vCard.toByteArray()),
      )

    assertEquals(NdefPayload.VCard(vCard), NdefPayloadParser.parse(message))
  }

  @Test
  fun nonWebUri_isRejected() {
    val message = NdefMessage(NdefRecord.createUri("tel:+15550100"))

    assertEquals(NdefPayload.Unsupported, NdefPayloadParser.parse(message))
  }

  @Test
  fun oversizedMessage_isRejected() {
    val payload = ("BEGIN:VCARD\nNOTE:" + "A".repeat(NdefPayloadParser.MAX_MESSAGE_BYTES)).toByteArray()
    val message = NdefMessage(NdefRecord.createMime("text/vcard", payload))

    assertEquals(NdefPayload.Unsupported, NdefPayloadParser.parse(message))
  }
}
