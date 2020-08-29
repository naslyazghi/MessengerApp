package com.nlmessenger.messenger

import android.app.Activity
import android.content.Intent
import android.content.pm.SigningInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nlmessenger.messenger.glide.GlideApp
import com.nlmessenger.messenger.model.UserInfo
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    // ------------------------------------
    // 1 - G L O B A L   V A R I A B L E S
    // ------------------------------------
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val currentUserStorageRef: StorageReference
        get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser?.uid.toString())

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(("users/${FirebaseAuth.getInstance().currentUser?.uid.toString()}"))

    companion object {
        val RC_select_image = 2
    }

    private lateinit var userName:String






    // ----------------------
    // 2 - O N   C R E A T E
    // ----------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set the status bar color depending on the SDK VERSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        else {
            window.statusBarColor = Color.WHITE
        }

        // Sign out from the app
        btn_signout_profile.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        // Show the app name as a title of the action bad
        setSupportActionBar(tb_profileactivity)
        // Modify the action bar title
        supportActionBar?.title = "Me"
        // Add the home button to the action bar
        supportActionBar?.setHomeButtonEnabled(true)
        // Display the home button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserInfo {user ->
            userName = user.name
            // 1 - Set the username on the profile activity
            tv_username_profileactivity.text = user.name
            //Toast.makeText(this, user.profileImage, Toast.LENGTH_LONG).show()
            // 2 - Upload the user profile photo to the profile activity
            // If the user has no photo, put a placeholder
            if (user.profileImage.isNotEmpty())
            {
                GlideApp.with(this)
                    .load(storageInstance.getReference(user.profileImage))
                    .placeholder(R.drawable.ic_account_circle)
                    .into(iv_profile_profileactivity)
            }
        }

        // Listener to change the profile picture
        iv_profile_profileactivity.setOnClickListener{
            val myIntentImage = Intent().apply{
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(myIntentImage, "Select the Image"), RC_select_image)
        }
    }




    // ------------------------------------------
    // 3 - O N   A C T I V I T Y   R E S U L T S
    // ------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_select_image && resultCode == Activity.RESULT_OK
            && data != null && data.data != null){
            // Show the progress bar for loading the image
            pb_profile.visibility = View.VISIBLE

            iv_profile_profileactivity.setImageURI(data.data)

            // Reduce the size of the image
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            // Upload the profile image to Firebase
            uploadProfileImage(selectedImageBytes) {path ->
                val userFieldMap = mutableMapOf<String, Any>()
                userFieldMap["name"] = userName
                userFieldMap["profileImage"] = path
                currentUserDocRef.update(userFieldMap)
            }
        }
    }




    // --------------------------------------------
    // 4 - U P L O A D   P R O F I L E   I M A G E
    // --------------------------------------------


    // Function to upload an image to DataBase (Customized)
    private fun uploadProfileImage(selectedImageBytes: ByteArray, onSuccess:(imagePath: String) -> Unit) {
        val ref = currentUserStorageRef.child("profilePictures/${UUID.nameUUIDFromBytes(selectedImageBytes)}")
        ref.putBytes(selectedImageBytes).addOnCompleteListener{
            if (it.isSuccessful)
            {
                onSuccess(ref.path)
                // Finish the progress bar for loading the image
                pb_profile.visibility = View.GONE
            }
            else
            {
                Toast.makeText(this, "Error : ${it.exception?.message.toString()}", Toast.LENGTH_LONG).show()
            }
        }
    }




    // ------------------------------
    // 5 - G E T   U S E R   I N F O
    // ------------------------------

    // Function to get the user Name before updating
    private fun getUserInfo(onComplete:(UserInfo) -> Unit) {
      currentUserDocRef.get().addOnSuccessListener {
          onComplete(it.toObject(UserInfo::class.java)!!)
      }
    }




    // ---------------------------------------------------
    // 6 - O N   O P T I O N   I T E M    S E L E C T E D
    // ---------------------------------------------------

    // Return to the previous activity after the home button is selected
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}
