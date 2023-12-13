package com.PhoShoots.app


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class ModeSelectionPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModeSelectionPageContent()
        }
    }
}


@Composable
fun ModeSelectionPageContent() {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.photographer_logo),
                contentDescription = "Photographer Logo Image"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(context, SearchPhotographers::class.java)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text("User", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    val intent = Intent(context, LoginPage::class.java)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text("Photographer", color = Color.White)
            }



        }
    }
}
