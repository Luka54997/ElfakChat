package com.example.elfakchat

import android.app.ProgressDialog
import android.content.Intent
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var RegisterButton: Button? = null
    private var username_register: EditText? = null
    private var password_register: EditText? = null
    private var alreadyHaveAccount: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressDialog? = null
    private var user: FirebaseUser? = null
    private var ref: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        mAuth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference()

     //   BindViews()

        link_signin.setOnClickListener { SendUserToLoginActivity() }


        btn_register.setOnClickListener { RegisterAccount() }
    }


    private fun RegisterAccount() {

        //  String username = username_register.getText().toString();
        //  String password = password_register.getText().toString();

        if (TextUtils.isEmpty(input_register_email!!.text.toString())) {
            Toast.makeText(this, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
            return

        }
        if (!input_register_email!!.text.toString().contains("elfak.rs")) {
            Toast.makeText(this, "Only Elfak students can register", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(input_register_password!!.text.toString())) {
            Toast.makeText(this, "Password field cannot be empty", Toast.LENGTH_SHORT).show()
            return

        }
        if (input_register_password!!.text.toString().length < 8) {
            Toast.makeText(this, "Password length must be at least 8 character long", Toast.LENGTH_SHORT).show()
            return
        } else {

            progressBar = ProgressDialog(this)
            val username = input_register_email!!.text.toString()
            val password = input_register_password!!.text.toString()
            progressBar?.setTitle("Registering New Account")
            progressBar?.setMessage("New Account is being registered")
            progressBar?.setCanceledOnTouchOutside(true)
            progressBar?.show()

            mAuth!!.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val deviceToken = FirebaseInstanceId.getInstance().token

                            val currentUserId = mAuth?.currentUser?.uid


                            VerifyUserEmail()
                            val userID = mAuth?.currentUser?.uid
                            if (userID != null) {
                                ref?.child("Users")?.child(userID)?.setValue("")

                                ref?.child("Users")?.child(currentUserId)?.child("device_token")?.setValue(deviceToken)
                            }
                            SendUserToLoginActivity()
                            Toast.makeText(this@RegisterActivity, "User account successfully created", Toast.LENGTH_LONG).show()
                            progressBar!!.dismiss()
                        } else {
                            val message = task.exception!!.toString()
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                            progressBar!!.dismiss()
                        }
                    }
        }

    }

  //  private fun BindViews() {
  //
  //      RegisterButton = findViewById(R.id.btn_register)
  //      username_register = findViewById(R.id.input_register_email)
  //      password_register = findViewById(R.id.input_register_password)
  //      alreadyHaveAccount = findViewById(R.id.link_signin)
  //      progressBar = ProgressDialog(this)

  //  }


    private fun SendUserToLoginActivity() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun VerifyUserEmail() {

        user = mAuth?.currentUser
        user?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this@RegisterActivity, "Verification email sent to " + user!!.email!!, Toast.LENGTH_SHORT).show()
                    } else {

                        Toast.makeText(this@RegisterActivity, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
