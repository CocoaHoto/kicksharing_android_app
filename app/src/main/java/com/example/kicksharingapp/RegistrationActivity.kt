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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {
    //Инициализация объектов на экране
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var edName: EditText
    private lateinit var registerButton: TextView
    private lateinit var notificationTextView: TextView
    private lateinit var dataBase: DatabaseReference
    private lateinit var auth: FirebaseAuth;


    private fun init () {
        edEmail = findViewById(R.id.email_input)
        edPassword = findViewById(R.id.pass)
        registerButton = findViewById(R.id.registerButton)
        notificationTextView = findViewById(R.id.notificationTextView)
        edName = findViewById(R.id.username_input)
        auth = Firebase.auth
        dataBase = Firebase.database("https://kicksharingapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
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

    private fun signInAfterSignUp() {
        auth.signInWithEmailAndPassword(edEmail.text.toString(), edPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val user = User(uid, edName.text.toString(), "Клиент")

                    if (uid != null) {
                        dataBase.child("User")
                            .child(uid)
                            .setValue(user)
                    }
                    val intent =  Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, "Ошибка входа.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun userSignUp(){
        if (!TextUtils.isEmpty(edEmail.text.toString()) && !TextUtils.isEmpty(edPassword.text.toString())  && !TextUtils.isEmpty(edName.text.toString())) {
            auth.createUserWithEmailAndPassword(edEmail.text.toString(), edPassword.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Пользователь зарегистрирован",
                            Toast.LENGTH_SHORT).show()
                        signInAfterSignUp()

                    } else {
                        Toast.makeText(baseContext, "Ошибка регистрации",
                            Toast.LENGTH_SHORT).show()
                        notificationTextView.visibility = View.VISIBLE
                    }
                }

        } else {
            notificationTextView.visibility = View.VISIBLE
        }
    }

    fun OnClickRegister (view: View) {
        userSignUp()
    }

    fun OnClickGoToSignIn(view: View) {
        val intent =  Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}