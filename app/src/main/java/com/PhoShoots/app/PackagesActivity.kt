package com.PhoShoots.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.PhoShoots.app.ui.theme.PhoshootsTheme

class PackagesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pid = intent.getStringExtra("pid") ?: "Pid"

        setContent {
            PhoshootsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PackagesContent(pid)
                }
            }
        }
    }
}

@Composable
fun PackagesContent(pid: String) {
    var packages by remember { mutableStateOf(listOf<Package>()) }

    val packagesRef = FirebaseDatabase.getInstance().getReference("photographers/$pid/packages")

    DisposableEffect(Unit) {
        val packagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedPackages = mutableListOf<Package>()
                for (packageSnapshot in snapshot.children) {
                    val packageItem = packageSnapshot.getValue(Package::class.java)?.copy(key = packageSnapshot.key ?: "")
                    packageItem?.let { updatedPackages.add(it) }
                }
                packages = updatedPackages
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        packagesRef.addValueEventListener(packagesListener)

        onDispose {
            packagesRef.removeEventListener(packagesListener)
        }
    }

    LazyColumn {
        items(packages) { packageItem ->
            PackageCard(packageDetails = packageItem, onDelete = { packageKey ->
                packagesRef.child(packageKey).removeValue().addOnSuccessListener {
                    // Handle success
                }.addOnFailureListener {
                    // Handle failure
                }
            })
        }
    }
}



@Composable
fun PackageCard(packageDetails: Package, onDelete: (String) -> Unit) {
    var context= LocalContext.current
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable {
        val intent = Intent(context, EditPackagePage::class.java)
        intent.putExtra("packageName", packageDetails.packageName)
        intent.putExtra("hourlyRate", packageDetails.hourlyRate)
        intent.putExtra("packageDescription", packageDetails.packageDescription)
        intent.putExtra("key", packageDetails.key)
        context.startActivity(intent)
    },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Package Name: ${packageDetails.packageName}")
            Text(text = "Hourly Rate: ${packageDetails.hourlyRate}")
            Text(text = "Description: ${packageDetails.packageDescription}")

            Button(onClick = { onDelete(packageDetails.key) }) {
                Text("Delete")
            }
        }
    }
}


data class Package(
    val packageName: String = "",
    val hourlyRate: String = "",
    val packageDescription: String = "",
    val key: String = ""
)
