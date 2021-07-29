package com.example.credentials

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.example.credentials.databinding.ActivityCredentialFormBinding
import com.example.credentials.databinding.ActivityProfileBinding

class credentialForm : AppCompatActivity() {

    private lateinit var binding: ActivityCredentialFormBinding

    private lateinit var textappsite: TextView
    //private lateinit var textusername: TextView
    //private lateinit var textpass: TextView
    private lateinit var textoutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCredentialFormBinding.inflate(layoutInflater)
        setContentView(binding.root)




        textappsite = findViewById(R.id.outputTv1)
        //textusername = findViewById(R.id.outputTv2)
        //textpass = findViewById(R.id.outputTv3)
        textoutput = findViewById(R.id.TvOutputofshowbtn)

        val appsite = intent.getStringExtra("Apps and Sites")
        //val username = intent.getStringExtra("Email/ Username")
       // val password = intent.getStringExtra("Password")
        val output = intent.getStringExtra("output")


        textappsite.text = "Apps and Sites: \n "+appsite
       // textusername.text = "Email/ Username: "+username
       // textpass.text = "Passwod: "+password
        textoutput.text ="\n\n" +output





    }
}