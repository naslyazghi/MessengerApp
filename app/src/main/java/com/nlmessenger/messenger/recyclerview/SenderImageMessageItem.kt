package com.nlmessenger.messenger.recyclerview

import android.content.Context
import android.text.format.DateFormat
import com.google.firebase.storage.FirebaseStorage
import com.nlmessenger.messenger.R
import com.nlmessenger.messenger.glide.GlideApp
import com.nlmessenger.messenger.model.ImageMessage
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.rv_item_image_message_sender.*
import kotlinx.android.synthetic.main.rv_item_text_message_sender.*


class SenderImageMessageItem(internal val imageMessage: ImageMessage, val messageId: String, val context: Context) : Item (){

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.tv_item_message_sender.text = DateFormat.format("hh:mm a", imageMessage.date).toString()
        if (imageMessage.imagePath.isNotEmpty())
        {
            GlideApp.with(context)
                .load(storageInstance.getReference(imageMessage.imagePath))
                .placeholder(R.drawable.ic_account_circle)
                .into(viewHolder.iv_item_image_sender)
        }
    }


    override fun getLayout(): Int {
        return R.layout.rv_item_image_message_sender
    }
    // Another way to write the function
    //override fun getLayout() = R.layout.rv_item_text_message_sender

}