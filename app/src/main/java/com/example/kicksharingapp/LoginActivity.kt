package com.example.kicksharingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    //Инициализация объектов на экране
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var loginButton: TextView
    private lateinit var errorTextView: TextView
    private lateinit var checkDataTextView: TextView
    private lateinit var auth: FirebaseAuth;

    private fun init () {
        edEmail = findViewById(R.id.editTextTextEmail)
        edPassword = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.loginButton)
        errorTextView = findViewById(R.id.errorTextView)
        checkDataTextView = findViewById(R.id.checkDataTextView)
        auth = Firebase.auth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Пользователя не существует", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Пользователь существует", Toast.LENGTH_SHORT).show()

        }
    }

    private fun userSignIn() {
        if (!TextUtils.isEmpty(edEmail.text.toString()) && !TextUtils.isEmpty(edPassword.text.toString())) {
            auth.signInWithEmailAndPassword(edEmail.text.toString(), edPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(
                            baseContext, "Пользователь вошел",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, MapsActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            baseContext, "Ошибка входа.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    errorTextView.visibility = View.VISIBLE
                    checkDataTextView.visibility = View.VISIBLE
                }
        } else {
            if (TextUtils.isEmpty(edEmail.text.toString()) || TextUtils.isEmpty(edPassword.text.toString())) {
                errorTextView.visibility = View.VISIBLE
                checkDataTextView.visibility = View.VISIBLE
            }
        }
    }

    fun OnClickSignIn(view: View) {
        userSignIn()
    }

    fun OnClickGoToSignUp(view: View){
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}