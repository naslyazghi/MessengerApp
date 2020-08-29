package com.nlmessenger.messenger.recyclerview

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.nlmessenger.messenger.R
import com.nlmessenger.messenger.glide.GlideApp
import com.nlmessenger.messenger.model.UserInfo
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.rv_item_chat.*

// ChatItems class with a Constructor
// parameters: UserInfo
// Parameters: Context (Activity)
class ChatItems(val uid: String, val user: UserInfo, val context: Context): Item() {

    // ----------------------
    // 1 - V A R I A B L E S
    // ----------------------
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }


    // ------------------------------
    // 2 - B I N D   F U N C T I O N
    // ------------------------------
    // Input: viewHolder The ViewHolder to bind
    // Input: position The adapter position
    // Output: Void
    // Will be called in our list for each user object later on
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.tv_name_recyclerview.text = user.name
        viewHolder.tv_time_recyclerview.text = "Time"
        viewHolder.tv_message_recyclerview.text = "Last Message"

        // Upload the user image from firebase using Glide library
        if (user.profileImage.isNotEmpty()){
            GlideApp.with(context)
                .load(storageInstance.getReference(user.profileImage))
                .into(viewHolder.iv_recyclerview)
        }
        // Use the default user image
        else {
            viewHolder.iv_recyclerview.setImageResource(R.drawable.ic_account_circle)
        }
    }



    // ------------------------
    // 3 - G E T   L A Y O U T
    // ------------------------
    // Display the items of the recycler view
    override fun getLayout(): Int {
        return R.layout.rv_item_chat
    }
}
