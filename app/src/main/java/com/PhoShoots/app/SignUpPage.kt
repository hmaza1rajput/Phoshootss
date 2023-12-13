package com.PhoShoots.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.PhoShoots.onlinestoreapp.Controller
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignUpPage : ComponentActivity() {
    private lateinit var mypLauncher: ActivityResultLauncher<String>
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var latitude: String;
    private lateinit var longitude: String;

    fun getLatitude(): String {
        return latitude
    }

    fun getLongitude(): String {
        return longitude
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        mypLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

        }
        check_Location_Permission()

        setContent {
            SignUpPageComposable()

        }
        setupLocationRequest()
        startLocationUpdates()

    }

    private fun check_Location_Permission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
            }
            else -> {

                mypLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }



    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            maxWaitTime = 20000
        }

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {

                for (location in locationResult.locations) {

                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    fusedLocationClient.removeLocationUpdates(locationCallback)
s


                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

        }
    }


}





@Composable
fun SignUpPageComposable() {
    var txt_name by remember { mutableStateOf("") }
    var txt_phone by remember { mutableStateOf("") }
    var txt_website by remember { mutableStateOf("https://") }
    var txt_bio by remember { mutableStateOf("test bio") }

    var txt_email by remember { mutableStateOf("") }
    var txt_password by remember { mutableStateOf("") }
    var uri_profile by remember { mutableStateOf<Uri?>(null) }

    val launcher_image = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri_profile = uri
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(20.dp)
    ) {

        if (uri_profile != null) {
            Image(
                painter = rememberAsyncImagePainter(uri_profile),
                contentDescription = null,

                contentScale = ContentScale.Crop,
                modifier = Modifier.size(150.dp).padding(8.dp).clickable(
                    onClick = { launcher_image.launch("image/*")  },
                )
            )
        }
        else{
            Image(
                painter = painterResource(id = R.drawable.photographer_logo),
                contentDescription = "Logo",
                modifier = Modifier.clickable(
                    onClick = { launcher_image.launch("image/*")  },
                )
                    .size(200.dp)
                    .align(Alignment.Start)
            )
        }



        Text(
            text = "Create your account",
            fontSize = 30.sp,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.bungee))
        )

        Spacer(modifier = Modifier.height(20.dp))

        CustomTextField(value = txt_name, label = "Name", onValueChange = { txt_name = it })

        CustomTextField(value = txt_phone, label = "Phone Number", onValueChange = { txt_phone = it })

        CustomTextField(value = txt_website, label = "Portfolio Website", onValueChange = { txt_website = it })


        CustomTextField(value = txt_email, label = "Email", onValueChange = { txt_email = it })

        CustomTextField(value = txt_password, label = "Password", onValueChange = { txt_password = it }, isPassword = true)

        CustomTextField(value = txt_bio, label = "Bio", onValueChange = { txt_bio = it })


        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                Controller.show_loader(context,"Siging Up . . .")
                uri_profile?.let {
                    signUpUser(context,
                        it,txt_email,txt_password, txt_name,txt_phone,txt_website,txt_bio, (context as SignUpPage).getLatitude(),(context as SignUpPage).getLongitude())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF1994B8)
            )
        ) {
            Text("Sign Up", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))


    }
}

@Composable
fun CustomTextField(value: String, label: String, onValueChange: (String) -> Unit, isPassword: Boolean = false) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        shape = RoundedCornerShape(50.dp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color(android.graphics.Color.parseColor("#c1c1c1")),
            textColor = Color.Black,
            cursorColor = Color.Black,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
    Spacer(modifier = Modifier.height(10.dp))
}


fun signUpUser(context: Context,uri_profile:Uri, email: String, password: String, name: String, phone: String, webiste: String, bio: String, latitude: String, longitude: String) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {




                upload_profile_image(uri_profile, context, onSuccess = { downloadUri ->
                    val uid = get_value_from_presistence(context, "uid")

                    saveUserInfo(context,name, phone,webiste,bio,downloadUri.toString(), email, latitude,longitude)



                },
                    onFailure = { exception ->
                        Controller.hide_loader()
                        Toast.makeText(context, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    })



            } else {

            }
        }
}

fun saveUserInfo(context:Context,name: String, phone: String, website: String, bio: String, profile_image: String, email: String, latitude: String,longitude:String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userMap = mapOf(
        "pid" to userId,
        "name" to name,
        "phone" to phone,
        "email" to email,
        "bio" to bio,
        "profile_img" to profile_image,

        "portfolio_website" to website,
        "latitude" to latitude,
        "longitude" to longitude
    )

    FirebaseDatabase.getInstance().getReference("photographers").child(userId).setValue(userMap) .addOnCompleteListener { task ->
        if (task.isSuccessful) {


            Controller.hide_loader()
            Toast.makeText(context,"Registered Successfully!",Toast.LENGTH_LONG).show()
            if (context is SignUpPage) {
                context.finish()
            }

        } else {
            Toast.makeText(context,"Registeration Faild!",Toast.LENGTH_LONG).show()

        }
    }
}

fun upload_profile_image(uri: Uri, context: Context, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/${uri.lastPathSegment}")
    val uploadTask = imageRef.putFile(uri)

    uploadTask.addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            onSuccess(downloadUri)
        }
    }.addOnFailureListener { exception ->
        onFailure(exception)
    }
}