package com.nlmessenger.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : AppCompatActivity(), TextWatcher {

    // 1 - G L O B A L   V A R I A B L E S
    // Create a firebase Authentication variable and initialize it in the same time
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    // 2 - O N   C R E A T E
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Listener when the user starts typing in the text box
        et_signin_email.addTextChangedListener(this)
        et_signin_password.addTextChangedListener (this)

        // Listener when the user clicks on the button sign in
        btn_signin_signin.setOnClickListener{
            val email = et_signin_email.text.toString()
            val password = et_signin_password.text.toString()

            // Check the validity of the user input and print the error messages
            if (email.isEmpty()) {
                et_signin_email.error = "Email is required"
                et_signin_email.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_signin_email.error = "Please Enter a valid Email"
                et_signup_email.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6){
                et_signin_password.error = "Password must be at least 6 characters"
                et_signin_password.requestFocus()
                return@setOnClickListener
            }
            // Sign in the user
            signIn(email, password)
        }

        btn_signin_createaccount.setOnClickListener{
            val createNewAccountIntent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(createNewAccountIntent)
        }
    }

    // 3 - O N   S T A R T
    override fun onStart() {
        super.onStart()
        // In android manifest, the intent filter was set up to start with the sign in activity
        // However, if the user has already signed in, there is no point to start the app with
        // a sign in window if the user closes the app and opens it again
        // Therefore, inside the sign in activity, override the onStart methode and direct
        // the user directly to main activity after checking if he signed in
        if(mAuth.currentUser?.uid != null){
            val intentMainActivity = Intent (this, MainActivity::class.java)
            startActivity(intentMainActivity)
        }
    }


    // 4 - S I G N   I N
    private fun signIn(email: String, password: String) {
        // Show the progress bas before moving to the next activity
        pb_signin.visibility = View.VISIBLE
        // Sign in
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                pb_signin.visibility = View.INVISIBLE
                val intentMainActivity = Intent (this, MainActivity::class.java)
                // Kill the actual Activity so the user can't go back to it and move to the new one
                intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intentMainActivity)
            }
            else {
                pb_signin.visibility = View.INVISIBLE
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    // 5 - T E X T   W A T C H E R   I M P L E M E N T A T I O N
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    // Enable the signin button when the 2 text boxes are not empty
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        btn_signin_signin.isEnabled = et_signin_email.text.toString().trim().isNotEmpty()
                                    && et_signin_password.text.toString().trim().isNotEmpty()
    }
}
