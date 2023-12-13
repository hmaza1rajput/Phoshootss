package com.PhoShoots.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.PhoShoots.app.ui.theme.PhoshootsTheme

class PackagesActivityUser : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pid = intent.getStringExtra("pid") ?: "Pid"

        setContent {
            PhoshootsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PackagesContentUser(pid)
                }
            }
        }
    }
}
@Composable
fun PackagesContentUser(pid:String) {
    var packages by remember { mutableStateOf(listOf<Map<String, String>>()) }

    DisposableEffect(Unit) {
        val packagesRef = FirebaseDatabase.getInstance()
            .getReference("photographers/$pid/packages")
        val packagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedPackages = mutableListOf<Map<String, String>>()
                for (packageSnapshot in snapshot.children) {
                    val packageDetails = packageSnapshot.value as Map<*, *>
                    updatedPackages.add(packageDetails as Map<String, String>)
                }
                packages = updatedPackages
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        packagesRef.addValueEventListener(packagesListener)

        onDispose {
            packagesRef.removeEventListener(packagesListener)
        }
    }

    LazyColumn {
        items(packages) { _package ->
            PackageCardUser(_package)
        }
    }
}


@Composable
fun PackageCardUser(_package: Map<String, String>) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Package Name: ${_package["packageName"]}")
            Text(text = "Hourly Rate: ${_package["hourlyRate"]}")
            Text(text = "Description: ${_package["packageDescription"]}")
        }
    }
}