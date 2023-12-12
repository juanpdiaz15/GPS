package com.example.gps.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gps.domain.FirestoreCaseUse

class FirebaseViewmodel: ViewModel() {

    val firestoreCaseUse = FirestoreCaseUse()

    fun crearUsuario(nombre:String,correo: String,contraseña:String){
        firestoreCaseUse.setearUsuarioFirestore(nombre, correo, contraseña)
    }
}