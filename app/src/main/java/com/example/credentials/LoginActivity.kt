package com.example.credentials

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import com.example.credentials.databinding.ActivityLoginBinding
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityLoginBinding

    //google
    private lateinit var googleSignInClient: GoogleSignInClient


    //constants
    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    //ActionBar
    private lateinit var actionBar: ActionBar


    //ProgressDialog
    private lateinit var progressDialog: ProgressDialog


    //Declare FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password = ""

    //Declare facebook auth
    lateinit var callbackManager: CallbackManager
    private val EMAIL = "email"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //configure google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        //initialize firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        CheckUser()

        //configure actionbar
        actionBar = supportActionBar!!
        actionBar.title = "Login"


        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Logging In..")
        progressDialog.setCanceledOnTouchOutside(false)


        //handle click, to begin google SignIn
        binding.googleSignInBtn.setOnClickListener {
            //begin google signIn
            Log.d(TAG, "onCreate: begin Google SignIn")
             val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)


        }

        //handle click, open SignUpActivity
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        //handle click, begin login
        binding.loginBtn.setOnClickListener {

            //before logging in, validate data
            validateData()
        }

        //facebook configure
        callbackManager = CallbackManager.Factory.create()

        binding.fbloginBtn.setPermissions("email")
        binding.fbloginBtn.setOnClickListener {
            signIn()
        }

    }


    private fun printKeyHash() {
        try {
            val info = packageManager.getPackageInfo("com.example.credentials", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray())
                Log.e("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))

            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

    }


    private fun validateData() {
        //get data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Invalid email format"
        } else if (TextUtils.isEmpty(password)) {
            //no password entered
            binding.passwordEt.error = "Please enter password"
        } else {
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()

                }
                .addOnFailureListener { e ->
                    //login failed
                    progressDialog.dismiss()
                    Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
    }

    private fun CheckUser() {
        //if user is already logged in go to profile activity
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            //user is already logged in
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }


   
    //start on activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthwithGoogleAccount(account)
            } catch (e: Exception) {
                // Google Sign In failed, update UI appropriately
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }

        callbackManager.onActivityResult(requestCode,resultCode,data)

    }

    // [START auth_with_google]
    private fun firebaseAuthwithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthwithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->

                    //logIn success
                    Log.d(TAG, "firebaseAuthwithGoogleAccount: LoggedIn")

                    //get loggedIN user
                    val firebaseUser = firebaseAuth.currentUser

                    //get user info
                    val uid = firebaseUser!!.uid
                    val email = firebaseUser.email

                    Log.d(TAG, "firebaseAuthwithGoogleAccount: Uid: $uid")
                    Log.d(TAG, "firebaseAuthwithGoogleAccount: Email $email")

                    //check if user is new or existing
                    if (authResult.additionalUserInfo!!.isNewUser){
                        //user is new - Account created
                        Log.d(TAG, "firebaseAuthwithGoogleAccount: Account Created... \n$email")
                        Toast.makeText(this@LoginActivity, "Account created... \n $email", Toast.LENGTH_SHORT).show()


                    }

                    else{
                            //existing user LoggedIn
                        Log.d(TAG, "firebaseAuthwithGoogleAccount: Existing user... \n$email")
                        Toast.makeText(this@LoginActivity, "LoggedIn... \n $email", Toast.LENGTH_SHORT).show()

                    }

                    //start profile activity
                        startActivity(Intent(this@LoginActivity , ProfileActivity::class.java))
                        finish()
    }
                .addOnFailureListener { e ->
                    //login Failed
                    Log.d(TAG, "firebaseAuthwithGoogleAccount: LogIn failed due to ${e.message}")
                    Toast.makeText(this@LoginActivity, "LogIn failed due to ${e.message}", Toast.LENGTH_SHORT).show()

                }
    // [END auth_with_google]


    }


    //facebook sign in
    private fun signIn() {
        binding.fbloginBtn.setReadPermissions("email", "public_profile")
        binding.fbloginBtn.registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {
                TODO("Not yet implemented")
            }

            override fun onError(error: FacebookException?) {

            }

        })
    }



    private fun handleFacebookAccessToken(accessToken: AccessToken?) {

        //get credential
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = firebaseAuth.currentUser
                        updateUI(user)

                        startActivity(Intent(this,ProfileActivity::class.java))
                        finish()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }


    }

    private fun updateUI(user: FirebaseUser?) {

    }

}