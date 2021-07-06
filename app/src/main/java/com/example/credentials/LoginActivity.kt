package com.example.credentials

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import com.example.credentials.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    //viewBinding
private lateinit var binding: ActivityLoginBinding

    //google
private lateinit var googleSignInClient: GoogleSignInClient


    //constants
private companion object{
    private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
}

    //ActionBar
private lateinit var actionBar: ActionBar


    //ProgressDialog
private lateinit var progressDialog: ProgressDialog


    //FirebaseAuth
private lateinit var firebaseAuth: FirebaseAuth
private var email =  ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //configure google sign in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //will be resolved when build for first time
            .requestEmail()  //need only email from google account
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)



        //configure actionbar
        actionBar = supportActionBar!!
        actionBar.title = "Login"


        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Logging In..")
        progressDialog.setCanceledOnTouchOutside(false)

        //int firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        CheckUser()

        //handle click, to begin google SignIn
        binding.googleSignInBtn.setOnClickListener {
            //begin google signIn
            Log.d(TAG, "onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
            
        }

        //handle click, open SignUpActivity
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, SignUpActivity:: class.java))
        }

        //handle click, begin login
        binding.loginBtn.setOnClickListener {

            //before logging in, validate data
            validateData()
        }

    }



    private fun validateData() {
        //get data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(password)){
            //no password entered
            binding.passwordEt.error = "Please enter password"
        }
        else{
            //data is validated, begin login
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        //show progress
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login success
                progressDialog.dismiss()
                //get user info
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "Logged in as $email", Toast.LENGTH_SHORT).show()

                //open profileActivity
                startActivity(Intent(this,ProfileActivity::class.java))
                finish()

            }
            .addOnFailureListener {  e->
                //login failed
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun CheckUser() {
        //if user is already logged in go to profile activity
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            //user is already logged in
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...):
        if (requestCode == RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google SignIn success, now auth with firebase
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthwithGoogleAccount(account)
            }
            catch (e:Exception){
                //failed Google SignIN
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthwithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthwithGoogleAccount: begin firebase auth with google account")

        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                //login success
                    Log.d(TAG, "firebaseAuthwithGoogleAccount: LoggedIn")

                    //get loggedIn user
                    val firebaseUser = firebaseAuth.currentUser
                    //get user info
                    val uid = firebaseUser!!.uid
                    val email = firebaseUser.email

                    Log.d(TAG, "firebaseAuthwithGoogleAccount: Uid:$uid")
                    Log.d(TAG, "firebaseAuthwithGoogleAccount: Email:$email")

                    //check if user is new or existing
                    if (authResult.additionalUserInfo!!.isNewUser){
                        //user is new - Account created
                        Log.d(TAG, "firebaseAuthwithGoogleAccount: Account created.. \n$email")
                        Toast.makeText(this@LoginActivity ,"Account created.. \n$email", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        //existing iser - LoggedIn
                        Log.d(TAG, "firebaseAuthwithGoogleAccount: Existing user.. \n$email")
                        Toast.makeText(this@LoginActivity ,"LoggedIn.. \n$email", Toast.LENGTH_SHORT).show()

                    }

                    //start profile
                    //user is already logged in
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }

                .addOnFailureListener { e ->
                    //login failed
                    Log.d(TAG, "firebaseAuthwithGoogleAccount: LogIn Failed due to ${e.message}")
                    Toast.makeText(this@LoginActivity ,"ogIn Failed due to ${e.message}", Toast.LENGTH_SHORT).show()


                }
    }

}