package com.example.credentials

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.credentials.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileActivity : AppCompatActivity() {


    //viewBinding
    private lateinit var binding: ActivityProfileBinding

    //ActionBar
    private lateinit var actionBar: ActionBar

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth


    private lateinit var btnsave: Button
    private lateinit var editappsite: EditText
    private lateinit var editusername: EditText
    private lateinit var editpass: EditText
   // private lateinit var showbutton: Button
    private lateinit var outputdata: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //configure ActionBar
        actionBar = supportActionBar!!
        actionBar.title = "Profile"

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //handle click, logout
        binding.logouBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }


        btnsave = findViewById(R.id.savetbtn1)
        editappsite = findViewById(R.id.appsiteEt)
        editusername = findViewById(R.id.emailusernameEt)
        editpass = findViewById(R.id.passEt)
       // showbutton = findViewById(R.id.showbtn1)
        outputdata = findViewById(R.id.outputdata)





        editusername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(editusername.text.toString()).matches())
                    btnsave.isEnabled = true
                else {

                    btnsave.isEnabled = false
                    editusername.setError("Invalid Email")

                }

            }

        })


        btnsave.setOnClickListener {

            startActivity(Intent(this, credentialForm::class.java)
                    .putExtra("Apps and Sites", editappsite.text.toString())
                    //.putExtra("Email/ Username", editusername.text.toString().trim())
                    //.putExtra("Password", editpass.text.toString().trim())
                    .putExtra("output", outputdata.text.toString())
            )

            var Username = editusername.text.toString().trim()
            var Password = editpass.text.toString().trim()


            if (Username.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (Password.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter a Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

          Username = encodeData(Username)
            Password = encodeData(Password)

            //shared preferences init
            val sharedPreferences = getSharedPreferences("SharedPref", 0)

            //getting the editor to edit data
            val editor = sharedPreferences.edit()

            //putting the data into the editor
            editor.putString("editusername", Username)
            editor.putString("editpass", Password)

            //apply changes
            editor.apply()

            //commit the changes
            editor.commit()





            val nameEncoded = sharedPreferences.getString("editusername", null)
            val passEncoded = sharedPreferences.getString("editpass", null)

            val name = decodeData(nameEncoded)
            val pass = decodeData(passEncoded)

            //show data
            outputdata.text = "Decoded username: $name \nDecoded password: $pass "

            var enusername = nameEncoded.toString()
            var enpass = passEncoded.toString()

            saveFireStore(enusername, enpass)


        }






    }

    //function to decode data
    private fun decodeData(name: String?): String {
        val encoded = Base64.decode(name?.toByteArray(), Base64.DEFAULT)
        return String(encoded)


    }

    //function to encode   data
    private fun encodeData(name: String): String{
        val encoded = Base64.encode(name.toByteArray(), Base64.DEFAULT)
        return String(encoded)
    }









    fun saveFireStore(username: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val user: MutableMap<String, Any> = HashMap()
        user["Username"] = username
        user["Password"] = password


        db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(this@ProfileActivity, "record added succesfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@ProfileActivity, "record failed to add", Toast.LENGTH_SHORT).show()

                }


    }








    private fun checkUser() {
        //check user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            //user not null, user is logged in, get user info
            val email = firebaseUser.email
            //set to text view
            binding.emailTv.text = email
        }
        else{
            //user is null, user is not logged in, go to login activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }



}