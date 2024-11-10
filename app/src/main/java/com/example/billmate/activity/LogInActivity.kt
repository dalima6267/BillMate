package com.example.billmate.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.billmate.databinding.ActivityLogInBinding
import com.example.billmate.models.User
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLogInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            if ( binding.layoutEmail.editText?.text.toString().equals("") or
             binding.layoutPassword.editText?.text.toString().equals("")) {
                Toast.makeText(this, "Please fill the above details", Toast.LENGTH_SHORT).show()
            } else {
                var user=User(binding.layoutEmail.editText?.text.toString(),
                    binding.layoutPassword.editText?.text.toString())
                Firebase.auth.signInWithEmailAndPassword(user.email!!,user.password!!).addOnCompleteListener {
                    if (it.isSuccessful){
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                    else{
                        Toast.makeText(this@LogInActivity, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }


        binding.txtSignIn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }


    }
}