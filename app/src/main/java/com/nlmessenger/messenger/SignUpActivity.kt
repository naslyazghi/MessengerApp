package com.nlmessenger.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firestore.v1.FirestoreGrpc
import com.nlmessenger.messenger.model.UserInfo
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity(), TextWatcher {

    // 1 - G L O B A L   V A R I A B L E S
    // Create a firebase Authentication variable and initialize it in the same time
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Create a firebaseFirestore variable and initialize it in the same time
    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Create a collection in firestore for the users
    private val currentUserDocRef: DocumentReference
    get() = firestoreInstance.document("users/${mAuth.currentUser?.uid.toString()}")
    // The above line of code is equivalent to:
    // firestoreInstance.collection("Users").document(mAuth.currentUser?.uid.toString())



    // 2 - O N   C R E A T E
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Listener when the user starts typing in the text box
        et_signup_name.addTextChangedListener(this)
        et_signup_email.addTextChangedListener(this)
        et_signup_password.addTextChangedListener (this)

        // Listener when the user clicks on the button sign up
        btn_signup_signup.setOnClickListener{
            // Store the user input in the variables
            val name = et_signup_name.text.toString().trim()
            val email = et_signup_email.text.toString().trim()
            val password = et_signup_password.text.toString().trim()

            // Check the validity of the user input and print the error messages
            if (name.length < 2) {
                et_signup_name.error = "Name is required"
                et_signup_name.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                et_signup_email.error = "Email is required"
                et_signup_email.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                et_signup_email.error = "Please Enter a valid Email"
                et_signup_email.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6)
            {
                et_signup_password.error = "Password must be at least 6 characters"
                et_signup_password.requestFocus()
                return@setOnClickListener
            }
            // Store the account info in FireBase
            createNewAccount (name, email, password)
            }
        }



    // 3 - C R E A T E   N E W   A C C O U N T
    // A function to Store the user new account in the database then receive the feedback upon the completion
    private fun createNewAccount(name: String, email: String, password: String) {
        pb_signup.visibility = View.VISIBLE

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
            val newUser = UserInfo(name, "")
            currentUserDocRef.set(newUser)

            // User signing up is successful => Intent to main activity
            if (task.isSuccessful)
            {
                pb_signup.visibility = View.INVISIBLE
                val intentMainActivity = Intent (this, MainActivity::class.java)
                // Kill the actual Activity so the user can't go back to it and move to the new one
                intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intentMainActivity)
            }
            // User signing up failed => Print the error message
            else
            {
                pb_signup.visibility = View.INVISIBLE
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
    }
}


    // 4 - T E X T   W A T C H E R   I M P L E M E N T A T I O N
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    // Enable the signup button when the 3s text boxes are not empty
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        btn_signup_signup.isEnabled = et_signup_name.text.trim().isNotEmpty()
                                && et_signup_email.text.trim().isNotEmpty()
                                && et_signup_password.text.trim().isNotEmpty()
    }
}
