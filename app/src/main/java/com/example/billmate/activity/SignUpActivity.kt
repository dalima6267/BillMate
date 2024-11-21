package com.example.billmate.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.billmate.databinding.ActivitySignUpBinding
import com.example.billmate.models.User
import com.example.billmate.utils.Constants.USER_NODE


import com.example.billmate.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SignUpActivity : AppCompatActivity() {
    lateinit var user: User
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(uri, com.example.billmate.utils.Constants.USER_PROFILE_FOLDER) { imageUrl ->
                if (imageUrl == null) {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                } else {
                    user.image = imageUrl
                    binding.imgProfile.setImageURI(uri)
                }
            }
        }
    }

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = User()

        if (intent.hasExtra("MODE")) {
            if (intent.getIntExtra("MODE", -1) == 1) {
                binding.btnRegister.text = "Update Profile"
                Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        user = documentSnapshot.toObject<User>()!!
                        if (!user.image.isNullOrEmpty()) {
                            Picasso.get().load(user.image).into(binding.imgProfile)
                        }
                        binding.layoutName.editText?.setText(user.name)
                        binding.layoutEmail.editText?.setText(user.email)
                        binding.layoutPassword.editText?.setText(user.password)
                    }
            }
        }

        binding.btnRegister.setOnClickListener {
            if (intent.hasExtra("MODE")) {
                if (intent.getIntExtra("MODE", -1) == 1) {
                    Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to update profile: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                if (binding.layoutName.editText?.text.toString().isEmpty() ||
                    binding.layoutEmail.editText?.text.toString().isEmpty() ||
                    binding.layoutPassword.editText?.text.toString().isEmpty()
                ) {
                    Toast.makeText(this, "Please fill in all the details", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        binding.layoutEmail.editText?.text.toString(),
                        binding.layoutPassword.editText?.text.toString()
                    ).addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            user.name = binding.layoutName.editText?.text.toString()
                            user.email = binding.layoutEmail.editText?.text.toString()
                            user.password = binding.layoutPassword.editText?.text.toString()
                            Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).set(user)
                                .addOnSuccessListener {
                                    startActivity(Intent(this@SignUpActivity, DashboardActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to save user data: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this@SignUpActivity, result.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.imgPlus.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.btnCancel.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LogInActivity::class.java))
        }
    }
}
