package com.nlmessenger.messenger.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SectionIndexer
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nlmessenger.messenger.ChatActivity
import com.nlmessenger.messenger.ProfileActivity
import com.nlmessenger.messenger.R
import com.nlmessenger.messenger.model.UserInfo
import com.nlmessenger.messenger.recyclerview.ChatItems
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat.*


/////////////////////////////////////////
// C H A T   F R A G M E N T   C L A S S
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

class ChatFragment : Fragment() {

    // -------------------------------
    // G L O B A L   V A R I A B L E S
    // -------------------------------

    val firestoreInstance = Firebase.firestore
//    private val firestoreInstance: FirebaseFirestore by lazy {
//        FirebaseFirestore.getInstance()
//    }

//    private lateinit var chatSection: Section




    // -----------------
    // O N   C R E A T E
    // -----------------

    // @Input: "inflater" The LayoutInflater object that can be used to inflate any views in the fragment
    // Input: "container" If non-null, this is the parent view that the fragment's UI should be attached to.
    // Input: "savedInstanceState" If non-null, this fragment is being re-constructed from a previous saved state as given here.
    // Output: Return the View for the fragment's UI, or null
    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // Bring the circular image view from the main activity
        val circleImageViewProfileImage = activity?.findViewById(R.id.civ_mainactivity) as ImageView

        // Set on click listener for the circular image view to change the profile image
        // Intent from the chat fragment to profile activity
        circleImageViewProfileImage.setOnClickListener{
            startActivity(Intent(activity, ProfileActivity::class.java))
        }

        // Call the customized higher order function "addChatListener" with the parameter "initRecyclerView" function
        // Show the recycler view that contains that chat
        addChatListener(::initRecyclerView)

        // Inflate the layout for this fragment and return it
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }



    // --------------------------------------------------
    // C H A T   L I S T E N E R  **CUSTOMIZED FUNCTION**
    // --------------------------------------------------

    // Input: Function that takes list of items and returns void
    // Output: ListenerRegistration
    // Get all the Users with their data and store them in a list called "items"
    private fun addChatListener(initializeRV : (List<Item>) -> Unit) : ListenerRegistration {
        // Access the "users" collection in the database
        return firestoreInstance.collection("users").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            // If there is an exception then return back
            if (firebaseFirestoreException != null){
                return@addSnapshotListener
            }
            // If there was no exception
            // Create a list of items
            val items = mutableListOf<Item>()
            // Retrieve documents from firebase
            querySnapshot!!.documents.forEach {
                // Create the chat Items
                val ct: ChatItems
                ct = ChatItems(it.id, it.toObject(UserInfo::class.java)!!, activity!!)
                // Add the chat Items to the list
                items.add(ct)
                //items.add(ChatItems(it.toObject(UserInfo::class.java)!!, activity!!))
            }

            // Call the customized function "initRecyclerView" with parameter "items"
            // Initialize the recycler view with the list of items created
            initializeRV(items)
        }
    }



    // ------------------------------------------------------------------------
    // I N I T I A L I Z E   R E C Y C L E R   V I E W  **CUSTOMIZED FUNCTION**
    // ------------------------------------------------------------------------

    // Input: List of Items
    // Output: Void
    private fun initRecyclerView(item: List<Item>){
        //  Obtain a handle to the object, connect it to a layout manager, and attach an adapter for the data to be displayed:
        rv_chatfragment.apply {
            // Use a linear layout manager
            layoutManager = LinearLayoutManager(activity)
            // Create an adapter for the recycler view using Groupie Library
            adapter = GroupAdapter<GroupieViewHolder>().apply {
                // Add a group
                // Section : A group which has a list of contents and an optional header and footer.
                add(Section(item))
//                chatSection = Section(item)
//                add(chatSection)
                setOnItemClickListener(onItemClick)
            }
        }
    }


    // ------------------------------------------------------------------------
    // O N    I T E M    C L I C K    L I S T E N E R   **CUSTOMIZED FUNCTION**
    // ------------------------------------------------------------------------

    val onItemClick = OnItemClickListener { item, view ->
        if (item is ChatItems){
            // Get the userName, profile image, and uid
            var name = item.user.name
            var image = item.user.profileImage
            var UID = item.uid
            // Create the intent
            val intentChatActivity = Intent(activity, ChatActivity::class.java)
            // Transfer the userName and profile image to Chat Activity
            intentChatActivity.putExtra("contactUserName", name)
            intentChatActivity.putExtra("contactProfileImage", image)
            intentChatActivity.putExtra("contactUID", UID)
            activity!!.startActivity(intentChatActivity)
        }

    }




}
