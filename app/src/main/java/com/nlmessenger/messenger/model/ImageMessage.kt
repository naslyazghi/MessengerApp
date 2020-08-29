package com.nlmessenger.messenger.model
import java.util.*

class ImageMessage (val imagePath: String,
                    override val senderID: String,
                    override val recipientID: String,
                    override val date: Date,
                    override val messageType: String = MessageType.IMAGE) : Message {

    constructor(): this("", "", "", Date(0))

}