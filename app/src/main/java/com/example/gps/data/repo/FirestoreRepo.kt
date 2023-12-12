package com.example.gps.data.repo

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepo {

    val db = FirebaseFirestore.getInstance()

    fun setUserData(nombre:String,correo: String,contraseña:String){

        val userHashMap = hashMapOf(
            "nombre" to nombre,
            "correo" to correo,
            "contraseña" to contraseña
        )

        db.collection("usuarios")
            .add(userHashMap).addOnCompleteListener{
                if(it.isSuccessful){

                }else{

                }
            }
    }

}