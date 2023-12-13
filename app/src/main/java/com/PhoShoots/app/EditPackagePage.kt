package com.PhoShoots.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.PhoShoots.app.ui.theme.PhoshootsTheme
import com.google.firebase.database.FirebaseDatabase

class EditPackagePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra("packageName") ?: ""
        val hourlyRate = intent.getStringExtra("hourlyRate") ?: ""
        val packageDescription = intent.getStringExtra("packageDescription") ?: ""
        val key = intent.getStringExtra("key") ?: ""
        Toast.makeText(this,hourlyRate,Toast.LENGTH_LONG).show()
        val pid = get_value_from_presistence(this, "uid")?:""

        setContent {
            PhoshootsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    EditPackageScreen(
                        packageName = packageName,
                        hourlyRate = hourlyRate,
                        packageDescription = packageDescription,
                        key = key,
                        onPackageUpdate = { updatedKey, updatedName, updatedRate, updatedDesc ->
                            updatePackage(this,pid, updatedKey, updatedName, updatedRate, updatedDesc)
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun EditPackageScreen(
        packageName: String,
        hourlyRate: String,
        packageDescription: String,
        key: String,
        onPackageUpdate: (String, String, String, String) -> Unit
    ) {
        var editedPackageName by remember { mutableStateOf(packageName) }
        var editedHourlyRate by remember { mutableStateOf(hourlyRate) }
        var editedPackageDescription by remember { mutableStateOf(packageDescription) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {


            Image(
                painter = painterResource(id = R.drawable.photographer_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Start)
            )

            Text(
                text = "Edit Package",
                fontSize = 30.sp,
                color = Color.Black,

            )

            TextField(
                value = editedPackageName,
                onValueChange = { editedPackageName = it },
                label = { Text("Package Name") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = editedHourlyRate,
                onValueChange = { editedHourlyRate = it },
                label = { Text("Hourly Rate") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = editedPackageDescription,
                onValueChange = { editedPackageDescription = it },
                label = { Text("Package Description") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onPackageUpdate(key, editedPackageName, editedHourlyRate, editedPackageDescription)
                }
            ) {
                Text("Update Package")
            }
        }
    }


        fun updatePackage(context: Context, pid:String, key: String, packageName: String, hourlyRate: String, packageDescription: String) {
            val packageUpdate = mapOf(
                "packageName" to packageName,
                "hourlyRate" to hourlyRate,
                "packageDescription" to packageDescription
            )

            FirebaseDatabase.getInstance().getReference("photographers/$pid/packages")
                .child(key)
                .updateChildren(packageUpdate)
                .addOnSuccessListener {
                    Toast.makeText(context,"Updated Success!",Toast.LENGTH_LONG).show()
                    if(context is Activity){
                        context.finish()
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(context,"Updated Failed!",Toast.LENGTH_LONG).show()
                }
        }


}

