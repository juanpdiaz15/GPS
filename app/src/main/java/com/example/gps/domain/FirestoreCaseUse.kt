package com.example.gps.domain

import com.example.gps.data.repo.FirestoreRepo

class FirestoreCaseUse {

    val repo = FirestoreRepo()

    fun setearUsuarioFirestore(nombre:String, correo:String, contraseña:String){

        repo.setUserData(nombre,correo, contraseña)

    }

}