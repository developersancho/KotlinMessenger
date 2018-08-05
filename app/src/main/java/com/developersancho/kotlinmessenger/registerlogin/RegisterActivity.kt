package com.developersancho.kotlinmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.developersancho.kotlinmessenger.R
import com.developersancho.kotlinmessenger.messages.LatestMessagesActivity
import com.developersancho.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        val TAG = "RegisterActivity"
    }

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_textview_register.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        select_photo_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    // startActivityForResult(intent, 0) metodundan sonra çalışır. Seçilen photo buton üzerine set edilir
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // procedd and check what the selected image was...
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            select_photo_imageview_register.setImageBitmap(bitmap)
            select_photo_button_register.alpha = 0f

            /*val bitmapDrawable = BitmapDrawable(bitmap)
            select_photo_button_register.background = bitmapDrawable*/
        }

    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Email is " + email)
        Log.d(TAG, "Password is $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    uploadImageToFirebaseStroage()
                    Log.d(TAG, "Successfuly created user with uid: ${it.result.user.uid}")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to create user ${it.message}")
                    Toast.makeText(this, "Failed to create user ${it.message}", Toast.LENGTH_SHORT).show()
                }
        return
    }

    private fun uploadImageToFirebaseStroage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "File Location $it")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to upload image to storage: ${it.message}")
                }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/usersmsg/$uid")
        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Finally we saved the user to Firebase to Database")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    // clear for this activity
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to set value to database: ${it.message}")
                }
    }


}

