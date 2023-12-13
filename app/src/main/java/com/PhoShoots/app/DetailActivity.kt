package com.PhoShoots.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*


import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.PhoShoots.app.ui.theme.PhoshootsTheme

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pid = intent.getStringExtra("pid") ?: "Pid"

        setContent {
            PhoshootsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    PhotographerDetailPage(pid)

                    /*  val photographerId = intent.getStringExtra("photographerId")
                      photographerId?.let { id ->
                          System.out.println("Response"+"Yes")

                      }*/
                }
            }
        }
    }
}

@Composable
fun PhotographerDetailPage(photographerId: String) {
    var photographer by remember { mutableStateOf<PhotographerModelClass?>(null) }

    LaunchedEffect(photographerId) {
        fetchPhotographerDetails(photographerId) { fetchedPhotographer ->
            photographer = fetchedPhotographer
        }
    }

    photographer?.let {
        DetailContent(photographer = it)
    }
}

@Composable
fun DetailContent(photographer: PhotographerModelClass) {
    var context= LocalContext.current;
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Display profile image at the top
        Image(
            painter = rememberImagePainter(data = photographer.profile_img),
            contentDescription = "Profile Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        )     {
            Text(
                text = photographer.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,

            modifier = Modifier.fillMaxWidth().clickable {
                val phoneNumber = "tel:${photographer.phone}"
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
                context.startActivity(dialIntent)
                }

        ) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = photographer.phone,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Display email



        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable {

                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${photographer.email}")
                    putExtra(Intent.EXTRA_SUBJECT, "Subject")
                }

                // Verify that there's an activity to handle the intent
                if (context.packageManager.resolveActivity(emailIntent, 0) != null) {
                    context.startActivity(emailIntent)
                } else {

                }

            }
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = photographer.email,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }


        Spacer(modifier = Modifier.height(8.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val url = photographer.portfolio_website
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                }
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = photographer.portfolio_website,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = photographer.bio,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    val intent = Intent(context, PackagesActivity::class.java)
                    intent.putExtra("pid", photographer.pid)

                    context.startActivity(intent)
                }
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_assessment_24),
                contentDescription = "Directions Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Click here to see packages",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openGoogleMapsForNavigation(context, photographer.latitude, photographer.longitude)
                }
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_directions),
                contentDescription = "Directions Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Click here to get directions",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)            )
        }


        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "Portfolio",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(5.dp))


        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(photographer.portfolioImages.values.toList()) { imageUrl ->
                PortfolioImageCardDesign(imageUrl = imageUrl)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
fun PortfolioImageCardDesign(imageUrl: String) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(200.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Image(
            painter = rememberImagePainter(data = imageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
        )
    }
}

fun fetchPhotographerDetails(photographerId: String, onResult: (PhotographerModelClass) -> Unit) {

    val databaseRef = FirebaseDatabase.getInstance().getReference("photographers/$photographerId")
    databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val photographer = snapshot.getValue(PhotographerModelClass::class.java)
            photographer?.let {
                onResult(it)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error if needed
        }
    })
}

data class PhotographerModelClass(
    val pid: String = "",

    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val portfolio_website: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val profile_img: String = "",
    val portfolioImages: Map<String, String> = emptyMap()
)




private fun openGoogleMapsForNavigation(context: Context, latitude: String, longitude: String) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)


}