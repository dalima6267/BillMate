package com.example.billmate.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.billmate.databinding.ActivityLogInBinding
import com.example.billmate.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.widget.Toast

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("appPreferences", MODE_PRIVATE)

        // Check if the app has been launched before
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        if (!isFirstRun) {
            // If not the first run, open DashboardActivity directly
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        } else {
            // Set first run to false after the initial run
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }

        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            if (binding.layoutEmail.editText?.text.toString().isEmpty() ||
                binding.layoutPassword.editText?.text.toString().isEmpty()
            ) {
                Toast.makeText(this@LogInActivity, "Please fill the above details", Toast.LENGTH_SHORT).show()
            } else {
                val user = User(
                    binding.layoutEmail.editText?.text.toString(),
                    binding.layoutPassword.editText?.text.toString()
                )
                Firebase.auth.signInWithEmailAndPassword(user.email!!, user.password!!).addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this@LogInActivity, DashboardActivity::class.java))
                        finish()
                    } else {
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
