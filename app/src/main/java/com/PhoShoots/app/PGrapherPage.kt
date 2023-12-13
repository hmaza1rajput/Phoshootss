package com.PhoShoots.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.PhoShoots.onlinestoreapp.Controller
import com.google.firebase.auth.FirebaseAuth

class PGrapherPage : ComponentActivity() {

    private lateinit var image_Picker_Launcher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        image_Picker_Launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

                if (uri != null) {

                        Controller.show_loader(this,"Uploading image to your portfolio . . .")
                        upload_image_to_firebase_storage(uri, this, onSuccess = { downloadUri ->
                            val uid = get_value_from_presistence(this, "uid")

                            if (uid != null) {
                                saveImageToPhotographerPortfolio(this,uid,downloadUri.toString())
                            }

                        },
                            onFailure = { exception ->
                            Controller.hide_loader()
                            Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        })


                }

            }
        setContent {
            PhotographerPageContent(imageUri = null,image_Picker_Launcher)
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhotographerPageContent(imageUri: Uri?, imagePickerLauncher: ActivityResultLauncher<String>) {
    var  context= LocalContext.current;

    var hourlyRate by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var packageDescription by remember { mutableStateOf("") }
    var portfolioImages by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(key1 = true) {
        val uid = get_value_from_presistence(context, "uid")

        val databaseRef = FirebaseDatabase.getInstance().getReference("photographers/$uid/portfolioImages")
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                val imageUrl = dataSnapshot.getValue(String::class.java)
                imageUrl?.let { portfolioImages = portfolioImages + it }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        }

           databaseRef.addChildEventListener(childEventListener)


    }






    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Spacer(modifier = Modifier.height(6.dp))

                Image(
                    painter = painterResource(id = R.drawable.photographer_logo),
                    contentDescription = "Photographer Image",
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))
                val name = get_value_from_presistence(context, "name")
                val phone = get_value_from_presistence(context, "phone")
                val email = get_value_from_presistence(context, "email")

                Text("Name: $name")
                Spacer(modifier = Modifier.height(24.dp))

                Text("Phone:$phone ")

                Spacer(modifier = Modifier.height(24.dp))

                Text("Email: $email")

                Spacer(modifier = Modifier.height(24.dp))


                LazyRow(
                    modifier = Modifier
                        .height(200.dp)
                ) {
                    items(portfolioImages) { imageUrl ->
                        PortfolioImageCard(imageUrl = imageUrl)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))


                Text("Add previous work images", color = Color.Blue)

                Button(onClick = {

                    imagePickerLauncher.launch("image/*")

                }) {
                    Text("Add Images")
                }

                Spacer(modifier = Modifier.height(16.dp))


                Button(onClick = {

                    val intent = Intent(context, PackagesActivity::class.java)
                    intent.putExtra("pid",get_value_from_presistence(context, "uid"))
                    context.startActivity(intent)

                }) {
                    Text("Check Your Packages")
                }








                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Hourly Rate") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                // Text field for entering package description
                OutlinedTextField(
                    value = packageDescription,
                    onValueChange = { packageDescription = it },
                    label = { Text("Package Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )


                Spacer(modifier = Modifier.height(6.dp))

                Button(onClick = {

                    get_value_from_presistence(context, "uid")?.let {
                        save_package(
                            context = context,
                            userId = it,
                            packageName = packageName,
                            hourlyRate = hourlyRate,
                            packageDescription = packageDescription
                        )
                    }

                }) {
                    Text("Add Package")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(context, LoginPage::class.java)
                    context.startActivity(intent)

                    if (context is Activity) {
                        context.finish()
                    }


                }) {
                    Text("Logout")
                }


            }
        }
    }

}

fun upload_image_to_firebase_storage(uri: Uri, context: Context, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
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

fun saveImageToPhotographerPortfolio(context: Context, userId: String, imageUrl: String) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("photographers/$userId/portfolioImages")

    val imageId = databaseRef.push().key
    imageId?.let {
        databaseRef.child(it).setValue(imageUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Controller.hide_loader()
                Toast.makeText(context, "Image added to portfolio successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to add image to portfolio", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun PortfolioImageCard(imageUrl: String) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(180.dp)
            .height(180.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = 7.dp
    ) {
        Image(
            painter = rememberImagePainter(data = imageUrl),
            contentDescription = "Portfolio Image",
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    }
}




fun save_package(
    context: Context,
    userId: String,
    packageName: String,
    hourlyRate: String,
    packageDescription: String
) {
    val databaseRef =
        FirebaseDatabase.getInstance().getReference("photographers/$userId/packages")

    val packageId = databaseRef.push().key
    packageId?.let {
        val packageDetailsMap = mapOf(
            "packageName" to packageName,
            "hourlyRate" to hourlyRate,
            "packageDescription" to packageDescription
        )

        databaseRef.child(it).setValue(packageDetailsMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Controller.hide_loader()
                    Toast.makeText(context, "Package added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add package", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

fun get_value_from_presistence(context: Context, key: String): String? {
    val sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, null)
}



