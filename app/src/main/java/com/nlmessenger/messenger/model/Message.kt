package com.nlmessenger.messenger.model
import java.util.*

// Interface to force both "ImageMessage" and "TextMessage" classes to implement it
// because they have both the same variables and the message type is the one that defines the type of message

interface Message {

    val senderID: String
    val recipientID: String
    val date: Date
    val messageType: String

}