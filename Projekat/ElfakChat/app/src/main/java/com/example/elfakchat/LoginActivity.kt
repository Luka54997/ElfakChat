package com.example.elfakchat

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null
  //  private var loginButton: Button? = null
   // private var username_login: EditText? = null
   // private var password_login: EditText? = null
   // private var dontHaveAccount: TextView? = null
   // private var forgotPassword: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressDialog? = null
    private var userRef : DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth?.currentUser

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");



        link_signup.setOnClickListener { SendUserToRegisterActivity() }

        btn_login.setOnClickListener { LoginAccount() }
        link_forgotPassword.setOnClickListener{ SendUserToPasswordResetActivity() }

    }

    private fun SendUserToPasswordResetActivity() {

        val intent = Intent(this@LoginActivity,PasswordResetActivity::class.java)
        startActivity(intent)

    }

    private fun LoginAccount() {

        if (currentUser != null) {
            currentUser!!.reload()
            if (!currentUser!!.isEmailVerified) {

                Toast.makeText(this, "Email address not verified", Toast.LENGTH_SHORT).show()
                return

            }

        }
        if (TextUtils.isEmpty(input_email!!.text.toString())) {
            Toast.makeText(this, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(input_password!!.text.toString())) {
            Toast.makeText(this, "Pasword field cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (input_password!!.text.toString().length < 8) {
            Toast.makeText(this, "Password length must be at least 8 character long", Toast.LENGTH_SHORT).show()
            return
        } else {
            progressBar = ProgressDialog(this)

            val username = input_email!!.text.toString()
            val password = input_password!!.text.toString()
            progressBar?.setTitle("Logging New Account")
            progressBar?.setMessage("New Account is being logged in")
            progressBar?.setCanceledOnTouchOutside(true)
            progressBar?.show()
            mAuth!!.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val currentUserid = mAuth?.currentUser?.uid
                            val deviceToken = FirebaseInstanceId.getInstance().token

                            userRef?.child(currentUserid)?.child("device_token")?.setValue(deviceToken)
                                    ?.addOnCompleteListener{
                                        if(it.isSuccessful){

                                            SendUserToMainActivity()
                                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_LONG).show()
                                            progressBar?.dismiss()
                                        }
                                    }



                        } else {
                            val message = task.exception!!.toString()

                            Toast.makeText(this@LoginActivity, message.substring(message.lastIndexOf(":") + 1), Toast.LENGTH_SHORT).show()
                            progressBar?.dismiss()

                        }
                    }

        }

    }


 //   private fun BindViews() {

  //      loginButton = findViewById(R.id.btn_login)
  //      username_login = findViewById(R.id.input_email)
   //     password_login = findViewById(R.id.input_password)
   //     dontHaveAccount = findViewById(R.id.link_signup)
    //    forgotPassword = findViewById(R.id.link_forgotPassword)
   //     progressBar = ProgressDialog(this)
  //  }


    private fun SendUserToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun SendUserToRegisterActivity() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
        finish()

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }


}
