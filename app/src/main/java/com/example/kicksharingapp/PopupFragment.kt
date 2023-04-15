package com.example.kicksharingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class PopupFragment : Fragment() {

    private lateinit var signOutButton: TextView
    private lateinit var welcomeTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var showEmailTextView: TextView
    private lateinit var signInTextView: TextView
    private lateinit var signUptextView: TextView
    private lateinit var dataBase: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        signOutButton = view.findViewById(R.id.signOutTextView)
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        showEmailTextView = view.findViewById(R.id.showEmailTextView)
        roleTextView = view.findViewById(R.id.roleTextView)
        signInTextView = view.findViewById(R.id.signInTextView)
        signUptextView = view.findViewById(R.id.signUpTextView)
        dataBase = Firebase.database("https://kicksharingapp-default-rtdb.europe-west1.firebasedatabase.app").reference


        checkVisibilty()

        signOutButton.setOnClickListener {
            if(auth.currentUser != null){
                Firebase.auth.signOut()
                checkVisibilty()
                val intent = Intent(this.context, MapsActivity::class.java)
                startActivity(intent)
            }
        }

        signInTextView.setOnClickListener {
            val intent = Intent(this.context, LoginActivity::class.java)
            startActivity(intent)
        }

        signUptextView.setOnClickListener {
            val intent = Intent(this.context, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkVisibilty(){
        if(auth.currentUser != null){
            val userRef = dataBase.child("User")
                .child(auth.currentUser!!.uid)

            val userDataListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        welcomeTextView.text = "Здравствуйте, " + user.userName
                        roleTextView.text = "Ваша роль: " + user.userRole
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                }
            }
            userRef.addValueEventListener(userDataListener)



            signInTextView.visibility = View.GONE
            signUptextView.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            welcomeTextView.visibility = View.VISIBLE
            showEmailTextView.visibility =View.VISIBLE
            roleTextView.visibility = View.VISIBLE
            showEmailTextView.text = "Ваша почта: " + auth.currentUser!!.email
        }
        else {
            signInTextView.visibility = View.VISIBLE
            signUptextView.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
            welcomeTextView.visibility = View.GONE
            showEmailTextView.visibility =View.GONE
            roleTextView.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PopupFragment()
            }
    }