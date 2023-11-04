package com.amaurypm.travels

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.amaurypm.travels.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser

import com.facebook.AccessTokenTracker
import com.facebook.GraphRequest
import org.json.JSONException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var callbackManager: CallbackManager
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var authStateListener: AuthStateListener

    private lateinit var accessTokenTracker: AccessTokenTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callbackManager = CallbackManager.Factory.create()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setPermissions("email", "public_profile", "user_friends")

        binding.loginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al ingresar. Por favor instala Facebook e inicia sesión desde ahí",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al ingresar. Por favor instala Facebook e inicia sesión desde ahí",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onSuccess(result: LoginResult) {
                    manejaTokenAcceso(result.accessToken)
                }
            })

        authStateListener = AuthStateListener { firebaseAuth ->
            var user = firebaseAuth.currentUser
            if (user != null) {
                actualizaUI(user)
            } else {
                actualizaUI(null)
            }
        }

        accessTokenTracker = object: AccessTokenTracker(){
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if(currentAccessToken == null)
                    firebaseAuth.signOut()
            }

        }



    }

    private fun manejaTokenAcceso(accessToken: AccessToken) {
        //Para el registro en Firebase
        val authCredential = accessToken.let { accessToken ->
            FacebookAuthProvider.getCredential(accessToken.token)
        }

        firebaseAuth.signInWithCredential(authCredential).addOnSuccessListener { result ->
            val user = firebaseAuth.currentUser
            actualizaUI(user)
        }.addOnFailureListener {
            //Manejamos el error
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private fun actualizaUI(user: FirebaseUser?) {
        if (user != null) {
            binding.tvNombrePerfil.text = user.displayName
            if (user.photoUrl != null) {
                var photoUrl = user.photoUrl.toString()
                //photoUrl = "$photoUrl?access_token=${AccessToken.getCurrentAccessToken()?.token}&type=large"
                photoUrl = "$photoUrl?type=large"
                binding.ivTravel.visibility = View.INVISIBLE
                Glide.with(this).load(photoUrl).into(binding.ivImagenPerfil)
            }
        } else {
            binding.tvNombrePerfil.text = ""
            binding.ivImagenPerfil.setImageResource(0)
            binding.ivTravel.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}

