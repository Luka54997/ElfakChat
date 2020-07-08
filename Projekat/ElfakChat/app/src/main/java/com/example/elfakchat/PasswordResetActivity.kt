package com.example.elfakchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_password_reset.*

class PasswordResetActivity : AppCompatActivity() {

    var mAuth:FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

         mAuth = FirebaseAuth.getInstance()

        btn_reset_password.setOnClickListener { ResetUserPassword() }
    }

    private fun ResetUserPassword() {

        val email = reset_email.text.toString()

        if(email.isEmpty()){
            Toast.makeText(this,"Please enter your email ",Toast.LENGTH_SHORT).show()
        }
        else{
            mAuth?.sendPasswordResetEmail(email)?.addOnCompleteListener {task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"Password reset email sent to :" + email,Toast.LENGTH_SHORT).show()
                    SendUserToLoginActivity()
                }
                else{
                    val message = task.exception.toString()
                    Toast.makeText(this,message.substring(message.lastIndexOf(":") + 1),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun SendUserToLoginActivity() {
        val intent = Intent(this@PasswordResetActivity,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
