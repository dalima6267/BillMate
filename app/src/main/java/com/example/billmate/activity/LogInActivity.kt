package com.example.billmate.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.billmate.databinding.ActivityLogInBinding

class LogInActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLogInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            val email = binding.layoutEmail.editText?.text.toString()
            val password = binding.layoutPassword.editText?.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill the above details", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish() // Only call finish if you're certain the activity needs to close.
            }
        }


        binding.txtSignIn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }


    }
}