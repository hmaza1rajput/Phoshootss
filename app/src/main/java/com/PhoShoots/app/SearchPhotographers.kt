package com.PhoShoots.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.PhoShoots.onlinestoreapp.Controller
import com.PhoShoots.onlinestoreapp.Controller.haversine
import com.PhoShoots.app.ui.theme.PhoshootsTheme

class SearchPhotographers : ComponentActivity() {

    private lateinit var mypLauncher: ActivityResultLauncher<String>
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val latitude = mutableStateOf("")
    val longitude = mutableStateOf("")




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        mypLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

        }
        check_Location_Permission()
        setContent {
            PhoshootsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SearchPhotographersPage(latitude,longitude)
                }
            }
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

                    latitude.value = location.latitude.toString()
                    longitude.value = location.longitude.toString()
                    fusedLocationClient.removeLocationUpdates(locationCallback)

                    System.out.println("Updated Latitude: ${latitude.value}, Longitude: ${longitude.value}")

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
            Controller.show_loader(this,"Fetching photographers please wait  . . .")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

        }
    }
}

@Composable
fun SearchPhotographersPage(latitude: MutableState<String>, longitude: MutableState<String>) {
    var photographers by remember { mutableStateOf(emptyList<Photographer>()) }
    var radiusSettingClicked by remember { mutableStateOf(false) }
    var radiusInput by remember { mutableStateOf("7") }
    var fetchPhotographersTrigger by remember { mutableStateOf(true) }
    var context = LocalContext.current

    LaunchedEffect(fetchPhotographersTrigger , key2 =latitude.value, key3 = longitude.value ) {
        if (fetchPhotographersTrigger && latitude.value.isNotEmpty() && longitude.value.isNotEmpty() && radiusInput.isNotEmpty()) {

            if(fetchPhotographersTrigger){
                Controller.show_loader(context,"Fetching photographers please wait  . . .")
            }

            fetchPhotographers(latitude.value, longitude.value, radiusInput) { fetchedPhotographers ->
                photographers = fetchedPhotographers
                fetchPhotographersTrigger = false
                Controller.hide_loader()
            }
        }
    }

    Column {

        TopAppBar(
            title = {
                Text(text = "Photographers")
            },
            actions = {

                IconButton(onClick = {

                    radiusSettingClicked = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Set Radius"
                    )
                }
            }
        )

        if (photographers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No photographers found in the given range")
            }        } else {
            LazyColumn {
                items(photographers) { photographer ->
                    PhotographerCard(photographer)
                }
            }
        }


    }

    if (radiusSettingClicked) {
        SetRadiusAlertDialog(
            onDismiss = { radiusSettingClicked = false },
            onSetRadius = {
                radiusSettingClicked = false
                fetchPhotographersTrigger = true
            },
            radiusInput = radiusInput,
            onRadiusInputChange = { newInput ->
                radiusInput = newInput
            }

        )

}

}

@Composable
fun PhotographerCard(photographer: Photographer) {
    var context= LocalContext.current;
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("pid", photographer.pid)

                Toast.makeText(context,photographer.pid,Toast.LENGTH_SHORT).show()
                context.startActivity(intent)


            }
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(data = photographer.profile_img),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(photographer.name, fontWeight = FontWeight.Bold)
                Text("Phone: ${photographer.phone}")
                Text("Email: ${photographer.email}")
            }
        }
    }
}

fun fetchPhotographers(currentLat: String, currentLong: String, radius: String, onResult: (List<Photographer>) -> Unit) {    val databaseRef = FirebaseDatabase.getInstance().getReference("photographers")
    databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val radiusInKm = radius.toDouble()
            val allPhotographers = snapshot.children.mapNotNull { it.getValue(Photographer::class.java) }
            val filteredPhotographers = allPhotographers.filter { photographer ->
                val distance = haversine(
                    currentLat.toDouble(),
                    currentLong.toDouble(),
                    photographer.latitude.toDouble(),
                    photographer.longitude.toDouble()
                )
                distance <= radiusInKm
            }
            onResult(filteredPhotographers)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle the error
        }
    })
}

data class Photographer(
    val pid: String = "",

    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val portfolioWebsite: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val profile_img: String = ""
)


@Composable
fun SetRadiusAlertDialog(
    onDismiss: () -> Unit,
    onSetRadius: () -> Unit,
    radiusInput: String,
    onRadiusInputChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Radius Km") },
        text = {
            OutlinedTextField(
                value = radiusInput,
                onValueChange = { newInput ->
                    if (newInput.all { it.isDigit() }) {
                        onRadiusInputChange(newInput)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),

            )
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onSetRadius()
                    }
                ) {
                    Text("Set Radius")
                }
            }
        }
    )
}


