package com.nlmessenger.messenger

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.nlmessenger.messenger.model.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nlmessenger.messenger.fragments.ChatFragment
import com.nlmessenger.messenger.fragments.MoreFragment
import com.nlmessenger.messenger.fragments.PeopleFragment
import com.nlmessenger.messenger.glide.GlideApp
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener{


    // ------------------------------------
    // 1 - G L O B A L   V A R I A B L E S
    // ------------------------------------

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestoreInstance :FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val mChatFragment = ChatFragment()
    private val mPeopleFragment = PeopleFragment()
    private val mMoreFragment = MoreFragment()





    // ----------------------
    // 2 - O N   C R E A T E
    // ----------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Upload the user profile image to the action bar
        firestoreInstance.collection("users")
            .document(mAuth.currentUser?.uid.toString())
            .get().addOnSuccessListener {
                // Convert the document snapshot to userinfo object to access its fields
                val user = it.toObject(UserInfo::class.java)!!
                // User has a profile image
                if (user.profileImage.isNotEmpty())
                {
                    // Upload the picture to the UI
                    GlideApp.with(this)
                        .load(storageInstance.getReference(user.profileImage))
                        .into(civ_mainactivity)
                }
                // User doesn't have a profile image
                else
                {
                    civ_mainactivity.setImageResource(R.drawable.ic_account_circle)
                }
            }

        // Ovveride the title of toolbar to an empty string
        // So we can show a customized title from xml
        setSupportActionBar(toolbar_main)
        supportActionBar?.title = ""

        // Set the status bar color depending on the SDK VERSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        else {
            window.statusBarColor = Color.WHITE
        }

        bottomNavigationView_main.setOnNavigationItemSelectedListener(this)
        setFragment(mChatFragment)

    }


    // ----------------------------------------------------------------------------------------
    // 3 - O N    N A V I G A T I O N    I T E M    S E L E C T E D   ** CUSTOMIZED FUNCTION **
    // ----------------------------------------------------------------------------------------
    // Bottom Navigation View Implementation
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navigation_chat_item -> {
                tv_main_title.text = "Chats"
                setFragment(mChatFragment)
                return true
            }
            R.id.navigation_people_item -> {
                tv_main_title.text = "People"
                setFragment(mPeopleFragment)
                return true
            }
            R.id.navigation_more_item -> {
                tv_main_title.text = "More"
                setFragment(mMoreFragment)
                return true
            }
            else -> return false
        }
    }


    // -----------------------------------------------------
    // 4 - S E T    F R A G M E N T   ** CUSTOMIZED FUNCTION **
    // -----------------------------------------------------
    // Set fragment function (Customized)
    private fun setFragment(fragment: Fragment) {
        val fr = supportFragmentManager.beginTransaction()
        fr.replace(R.id.coordinatorLayout_main, fragment)
        fr .commit()
    }
}
