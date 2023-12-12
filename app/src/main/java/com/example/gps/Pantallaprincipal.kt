package com.example.gps

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class pantallaprincipal : AppCompatActivity() {


    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    private lateinit var mLoginBtn: Button
    private lateinit var mRecuperarBtn: Button
    private lateinit var mRegistroBtn: Button
    private lateinit var mTextview: TextView
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, botones::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantallaprincipal)
        mUsername = findViewById(R.id.usuario)
        mPassword = findViewById(R.id.contraseña)
        mLoginBtn = findViewById(R.id.btn_ingresar)
        mTextview = findViewById(R.id.titulo)
        mRecuperarBtn = findViewById(R.id.recuperar)
        mRegistroBtn = findViewById(R.id.registrar)
        auth = Firebase.auth

        val fuente = Typeface.createFromAsset(assets, "fonts/Facon.ttf")
        mTextview.typeface = fuente

        mRecuperarBtn.setOnClickListener {
            val intent = Intent(this, recuperar::class.java)
            startActivity(intent)
        }

        mRegistroBtn.setOnClickListener {
            val intent = Intent(this, Registrar::class.java)
            startActivity(intent)
        }


        mLoginBtn.setOnClickListener(View.OnClickListener {
            val username = mUsername.text.toString()
            val password = mPassword.text.toString()


            if (mUsername.text.toString().isEmpty() && mPassword.text.toString().isEmpty()) {
                Toast.makeText(this, "Los campos están vacíos", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(baseContext, "Login succesful.", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, botones::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        })




    }

}