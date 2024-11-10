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
import com.example.billmate.utils.Constants.USER_PROFILE_FOLDER
import com.example.billmate.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    lateinit var user: User
    private val launcher=registerForActivityResult(ActivityResultContracts.GetContent()){
       uri->
        uri?.let {
            uploadImage(uri,USER_PROFILE_FOLDER){
                if(it==null){

                }else{
                    user.image=it
                    binding.imgProfile.setImageURI(uri)
                }
            }
        }
    }

    private lateinit var binding:ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSubmit.setOnClickListener {
            if(intent.hasExtra("MODE")){
                if(intent.getIntExtra("MODE",-1)==1){
                    Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        }
                }
            }
            if(binding.layoutName.editText?.text.toString().equals("") or
                binding.layoutEmail.editText?.text.toString().equals("") or
                binding.layoutPassword.editText?.text.toString().equals("")){
                Toast.makeText(this, "please fill the above details", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.layoutEmail.editText?.text.toString(),
                    binding.layoutPassword.editText?.text.toString()
                ).addOnCompleteListener {
                    result->
                    if(result.isSuccessful){
                        user.name=binding.layoutName.editText?.text.toString()
                        user.email=binding.layoutEmail.editText?.text.toString()
                        user.password= binding.layoutPassword.editText?.text.toString()
                        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).set(user)
                            .addOnSuccessListener {
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            }
                    }
                    else{
                        Toast.makeText(this@SignUpActivity,result.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

                }


            }
        }
        binding.imgProfile.setOnClickListener {
            launcher.launch("image/*")
        }
        binding.btnCancel.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

    }
}