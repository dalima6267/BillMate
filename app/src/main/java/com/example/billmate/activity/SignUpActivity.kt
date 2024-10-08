package com.example.billmate.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.billmate.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSubmit.setOnClickListener {
            if(binding.layoutName.editText?.text.toString().equals("") ||
                binding.layoutEmail.editText?.text.toString().equals("") ||
                binding.layoutPassword.editText?.text.toString().equals("")){
                Toast.makeText(this, "please fill the above details", Toast.LENGTH_SHORT).show()
            }
            else{
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }
        binding.btnCancel.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

    }
}