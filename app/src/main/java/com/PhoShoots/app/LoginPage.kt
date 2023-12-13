package com.PhoShoots.app

import android.content.Context
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.PhoShoots.onlinestoreapp.Controller


class LoginPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, PGrapherPage::class.java)
            startActivity(intent)
            finish()
        }
        setContent {
            LoginPageCompostable()
            FirebaseApp.initializeApp(this)

        }
    }
}

@Composable
fun LoginPageCompostable() {

    var txt_email by remember { mutableStateOf("") }
    var txt_password by remember { mutableStateOf("") }

    val context = LocalContext.current



    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.photographer_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Start)
        )

        Text(
            text = "Hello there, Welcome Back",
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.bungee))
        )

        Text(
            text = "Sign In to continue",
            fontSize = 18.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.antic))
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = txt_email,
            onValueChange = {
                txt_email = it


            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(android.graphics.Color.parseColor("#c1c1c1")),                textColor = Color.Black,
                cursorColor = Color.Black,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent

            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = txt_password,
            onValueChange = {

                txt_password = it
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(50.dp),

            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(android.graphics.Color.parseColor("#c1c1c1")),
                textColor = Color.Black,
                cursorColor = Color.Black,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent


            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {


                      sign_in_using_auth_firebase(txt_email,txt_password,context)

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF1994B8)
            )

        ) {
            Text("Sign In", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))



        Button(
            onClick = {

                val intent = Intent(context, SignUpPage::class.java)
                context.startActivity(intent)

                      },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF1994B8)
            )

        ) {
            Text("Not have an account?", color = Color.White)
        }
    }
}

fun sign_in_using_auth_firebase(email: String, password: String, context: Context) {
    Controller.show_loader(context,"Logging in please wait . . .")
    val auth = FirebaseAuth.getInstance()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {


                fetch_info(l)
                { uid,name, phone, email, latitude, longitude ->

                    save_in_presistences_storage(context,uid, name, phone, email, latitude, longitude)

                    Toast.makeText(context, "Welcome "+name, Toast.LENGTH_SHORT).show()
                    Controller.hide_loader()
                    val intent = Intent(context, PGrapherPage::class.java)
                    context.startActivity(intent)
                }



            } else {
                Controller.hide_loader()
                Toast.makeText(context, "Login failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}


fun fetch_info(onSuccess: (String,String, String, String, String, String) -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val user_ref = FirebaseDatabase.getInstance().getReference("photographers").child(uid)

    user_ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val uid = snapshot.key ?: ""

            val name = snapshot.child("name").getValue(String::class.java) ?: ""
            val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
            val email = snapshot.child("email").getValue(String::class.java) ?: ""
            val latitude = snapshot.child("latitude").getValue(String::class.java) ?: ""
            val longitude = snapshot.child("longitude").getValue(String::class.java) ?: ""

            onSuccess.invoke(uid,name, phone, email, latitude, longitude)
        }

        override fun onCancelled(error: DatabaseError) {

        }
    })
}

fun save_in_presistences_storage(context: Context,uid: String, name: String, phone: String, email: String, latitude: String, longitude: String) {
    val sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("uid", uid)

    editor.putString("name", name)
    editor.putString("phone", phone)
    editor.putString("email", email)
    editor.putString("latitude", latitude)
    editor.putString("longitude", longitude)

    editor.apply()
}


