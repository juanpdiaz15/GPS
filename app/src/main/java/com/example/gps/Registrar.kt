package com.example.gps

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gps.viewmodel.FirebaseViewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Registrar : AppCompatActivity() {

    private lateinit var mbtnregistrarusuario: Button
    private lateinit var mTextview: TextView
    private lateinit var mUsername: EditText
    private lateinit var mCorreo : EditText
    private lateinit var mPassword: EditText
    private lateinit var mconfpassword: EditText
    private lateinit var viewModel: FirebaseViewmodel
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
        setContentView(R.layout.activity_registrar)

        mTextview = findViewById(R.id.titulo2)
        mUsername = findViewById(R.id.nombreUsuario)
        mCorreo = findViewById(R.id.correo)
        mPassword = findViewById(R.id.contra)
        mconfpassword = findViewById(R.id.confcontra)
        mbtnregistrarusuario = findViewById(R.id.registrarusuario)
        auth = Firebase.auth
        //viewModel = ViewModelProviders.of(this).get(FirebaseViewmodel::class.java)
        crearUsuario()


        val fuente = Typeface.createFromAsset(assets, "fonts/Facon.ttf")
        mTextview.typeface = fuente

    }
    fun crearUsuario(){
        mbtnregistrarusuario.setOnClickListener {

            val preferences = getSharedPreferences("Reg", MODE_PRIVATE)
            val username = mUsername.text?.toString() ?:""
            val email = mCorreo?.text?.toString() ?: ""
            val password = mPassword?.text?.toString() ?: ""
            val confpassword = mconfpassword?.text?.toString() ?: ""

            if (username.isEmpty()) {
                Toast.makeText(this, "Ingrese un Nombre", Toast.LENGTH_SHORT).show()
            }else if (email.isEmpty()) {
                Toast.makeText(this, "Ingrese un Correo", Toast.LENGTH_SHORT).show()
            }else if (password.isEmpty()) {
                Toast.makeText(this, "Ingrese una Contraseña", Toast.LENGTH_SHORT).show()
            } else if (confpassword.isEmpty()) {
                Toast.makeText(this, "Confirme su Contraseña", Toast.LENGTH_SHORT).show()
            } else if (password == confpassword) {
                val editor = preferences.edit()
                editor.putString("Username", username)
                editor.putString("Email",email)
                editor.putString("Password", password)
                editor.putString("confpassword", confpassword)
                editor.apply()


                //viewModel.crearUsuario(username,email,password)

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Usuario creado con éxito", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, botones::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext, "Authentication failed.${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }




            } else {
                Toast.makeText(this, "No coinciden las contraseñas", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun showAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un errorautentificacndo al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}