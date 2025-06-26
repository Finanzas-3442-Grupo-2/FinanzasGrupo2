package com.example.finanzas

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finanzas.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        binding.registrarse.setOnClickListener {
            val email = binding.crearCuentaCorreo.text.toString()
            val nombres = binding.crearCuentaNombres.text.toString()
            val password = binding.crearCuentaContrasenia.text.toString()
            val confirmPassword = binding.crearCuentaRepetirContrasenia.text.toString()

            //FORMULARIO Y REGISTRO EN LA BASE DE DATOS (FIRESTORE)
            if (email.isNotEmpty() && nombres.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {

                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        val userId = firebaseAuth.currentUser?.uid

                        //CREA UN MAPA CON LOS DATOS VISIBLES (NO CONTRASEÑA)
                        val userData = hashMapOf(
                            "nombres" to nombres,
                            "email" to email
                        )

                        //GUARDA LOS DATOS EN FIRESTORE
                        userId?.let {
                            db.collection("usuarios").document(it)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }

                        if (it.isSuccessful) {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                        }
                    }
                } else {
                    Toast.makeText(this, "La contraseña no coincide", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Se requieren más datos", Toast.LENGTH_SHORT).show()

            }
        }

        //ENLACE A LOGIN CON "YA TENGO UNA CUENTA"
        val signUpLink = findViewById<TextView>(R.id.yaTengoUnaCuenta)
        signUpLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}