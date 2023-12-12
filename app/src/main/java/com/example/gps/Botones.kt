package com.example.gps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class botones : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user : FirebaseUser
    private lateinit var logoutbtn: Button
    private lateinit var adminbtn: Button
    private lateinit var textview: TextView
    private lateinit var btn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_botones)
        auth = Firebase.auth
        logoutbtn = findViewById(R.id.logout)
        adminbtn = findViewById(R.id.adminv)
        user = auth.currentUser!!
        textview = findViewById(R.id.texto)
        btn = findViewById(R.id.gps)

        if (user == null){
            val intent = Intent(this, pantallaprincipal::class.java)
            startActivity(intent)
            finish()
        }else textview.text = user.email

        logoutbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, pantallaprincipal::class.java)
            startActivity(intent)
            finish()
        }

        adminbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Vistaadmin::class.java)
            startActivity(intent)
            finish()
        }

        btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}