package com.nlmessenger.messenger.recyclerview

import android.content.Context
import android.text.format.DateFormat
import com.nlmessenger.messenger.R
import com.nlmessenger.messenger.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.rv_item_text_message_sender.*


class SenderTextMessageItem(val textMessage: TextMessage, val messageId: String, val context: Context) : Item (){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.tv_message_itemTextMessage.text = textMessage.text
        viewHolder.tv_time_itemTextMessage.text = DateFormat.format("hh mm a", textMessage.date).toString()
    }

    override fun getLayout(): Int {
        return R.layout.rv_item_text_message_sender
    }
    // Another way to write the function
    //override fun getLayout() = R.layout.rv_item_text_message_sender

}