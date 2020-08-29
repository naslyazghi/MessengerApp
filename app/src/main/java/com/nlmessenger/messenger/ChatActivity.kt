package com.nlmessenger.messenger

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nlmessenger.messenger.glide.GlideApp
import com.nlmessenger.messenger.model.ImageMessage
import com.nlmessenger.messenger.model.Message
import com.nlmessenger.messenger.model.MessageType
import com.nlmessenger.messenger.model.TextMessage
import com.nlmessenger.messenger.recyclerview.RecipientTextMessageItem
import com.nlmessenger.messenger.recyclerview.SenderImageMessageItem
import com.nlmessenger.messenger.recyclerview.SenderTextMessageItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

class ChatActivity : AppCompatActivity() {



    // -------------------------------
    // G L O B A L   V A R I A B L E S
    // -------------------------------

    private val fireStoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()

    private val currentImageRef = storageInstance.reference

    private val chatChannelCollectionRef = fireStoreInstance.collection("chatChannels")

    private lateinit var ref: StorageReference
    private lateinit var  mCurrentChatChannelID: String

    private val currentTime = Calendar.getInstance().time
    private var recipientUID = ""
    private val senderUID = FirebaseAuth.getInstance().currentUser!!.uid
    private val messageAdapter = GroupAdapter<GroupieViewHolder>()




    // -----------------
    // O N   C R E A T E
    // -----------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Set the status bar color depending on the SDK VERSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        else
        {
            window.statusBarColor = Color.WHITE
        }


        // Receive the UserName, profile image, and the uid
        val contactUserName = intent.getStringExtra("contactUserName")
        val contactProfileImage = intent.getStringExtra("contactProfileImage")
        recipientUID = intent.getStringExtra("contactUID")

        // Set the userName and profile image
        tv_username_chatActivity.text = contactUserName
        if (contactProfileImage.isNotEmpty())
        {
            GlideApp.with(this)
                .load(storageInstance.getReference((contactProfileImage)))
                .into(iv_profilePicture_chatActivity)
        }
        else
        {
            iv_profilePicture_chatActivity.setImageResource(R.drawable.ic_account_circle)
        }



//        iv_pictures_chatActivity.setOnClickListener{
//            val myIntentImage = Intent().apply{
//                type = "image/*" // All types of images
//                action = Intent.ACTION_GET_CONTENT
//                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
//            }
//            startActivityForResult(Intent.createChooser(myIntentImage, "Select Image"), 2)
//        }

        // Show the photo selector
        iv_pictures_chatActivity.setOnClickListener{
            val intent = Intent(ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 2)
        }



        // Create the chat chanel between two users
        createChatChanel {chanelId ->
            mCurrentChatChannelID = chanelId
            getMessages(chanelId)
            // Send the message
            iv_send_chatActivity.setOnClickListener{
                val text = et_message_chatActivity.text.toString()
                // If sender doesn't compose a message, don't send an empty text
                if (text.isNotEmpty())
                {
                    val textMessage = TextMessage(text, senderUID, recipientUID, currentTime)
                    sendMessage(chanelId, textMessage)
                    // Empty the text box after send a message
                    et_message_chatActivity.setText("")
                }
            }
        }

        // Initialize the recycler view
        rv_chatActivity.apply{
            // LayoutManager already defined in the xml layout
            //layoutManager = LinearLayoutManager (this@ChatActivity)
            adapter = messageAdapter
        }

        // Go back to previous activity
        iv_back_chatActivity.setOnClickListener{
            finish()
        }
    }




    // -------------------------------------------------------------
    // C R E A T E   C H A T   C H A N E L   **CUSTOMIZED FUNCTION**
    // -------------------------------------------------------------

    private fun createChatChanel(onComplete:(chanelId: String) -> Unit) {
        fireStoreInstance.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("chatChannel").document(recipientUID).get()
            .addOnSuccessListener {
                // Conversation between the 2 users already exists
                if (it.exists()) {
                    onComplete(it["channelID"] as String)
                    return@addOnSuccessListener
                }

                // The conversation doesn't exists// Create a new chat chanel
                // Chanel shared between both users
                val newChatChannel = fireStoreInstance.collection("users").document()
                fireStoreInstance.collection("users").document(recipientUID)
                    .collection("chatChannel").document(senderUID)
                    .set(mapOf("channelID" to newChatChannel.id))

                fireStoreInstance.collection("users").document(senderUID)
                    .collection("chatChannel").document(recipientUID)
                    .set(mapOf("channelID" to newChatChannel.id))

                onComplete(newChatChannel.id)
            }
    }




    private fun sendMessage(chanelId: String, message: Message) {
        // Create a document with chanel ID inside the chat channel collection
        // Create a message collection inside that chanel Id document
        // store the text message inside the collection
        chatChannelCollectionRef.document(chanelId).collection("message").add(message)
    }




    private fun getMessages(channelID: String){
        val query = chatChannelCollectionRef.document(channelID).collection("message").orderBy("date", Query.Direction.DESCENDING)
        query.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
            // clear adapter to show only the new messages
            messageAdapter.clear()
            querySnapshot!!.documents.forEach {
                // Message type is a text
                if (it["type"] == MessageType.TEXT)
                {
                    val textMessage = it.toObject(TextMessage::class.java)
                    if (textMessage!!.senderID == senderUID)
                    {
                        messageAdapter.add(SenderTextMessageItem(it.toObject(TextMessage::class.java)!!, it.id, this))
                    }
                    else
                    {
                        messageAdapter.add(RecipientTextMessageItem(it.toObject(TextMessage::class.java)!!, it.id, this))
                    }
                }
                // Message type is an image
                else
                {
                    val imageMessage = it.toObject(ImageMessage::class.java)
                    messageAdapter.add(SenderImageMessageItem(it.toObject(ImageMessage::class.java)!!, it.id, this))
                }


            }
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null && data.data!= null)
        {
            // Proceed and check what the selected image was
            // Get the location of the image on the device
            val imageURI = data.data
            // convert to BitMap
            val imageBitMap = MediaStore.Images.Media.getBitmap(contentResolver, imageURI)
            // Compress the size of the image
            val outputStream = ByteArrayOutputStream()
            imageBitMap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
            // Get an image of a byteArray form to control the size of it
            val imageBytes = outputStream.toByteArray()
            // Upload the image to the storage of FireBase
            uploadImage(imageBytes)

            val imageMessage = ImageMessage(ref.path, senderUID, recipientUID, currentTime)
            //chatChannelCollectionRef.document(mCurrentChatChannelID).collection("messages").add(imageMessage)
            sendMessage(mCurrentChatChannelID, imageMessage)
            Log.d("ChatActivity", "On activity result end")
        }
    }





    private fun uploadImage(imageBytes: ByteArray) {
        val fileName = UUID.nameUUIDFromBytes(imageBytes)

        ref = currentImageRef.child("${senderUID}/images/$fileName")
        // Using putBytes instead of putFile to be able to compress the image
        ref.putBytes(imageBytes).addOnCompleteListener{
            if (it.isSuccessful)
            {
                Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
            }
        }


    }

}
