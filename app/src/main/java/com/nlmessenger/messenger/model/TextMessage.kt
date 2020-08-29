package com.nlmessenger.messenger.model
import java.util.*

class TextMessage (val text: String,
                   override val senderID: String,
                   override val recipientID: String,
                   override val date: Date,
                   override val messageType: String = MessageType.TEXT) : Message{

    constructor() : this ("", "", "", Date())
}