package com.liulkovich.florapoint.presentation.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.presentation.screens.guide.numberInString
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
){
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Title(
                    modifier = Modifier
                        .padding(horizontal = 24.dp),
                    text = state.species?.name ?: "Неизвестно"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding
        ) {

            item {
                state.species?.let { species ->
                    InformCard(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        textImage = species.imageName,
                        textHabitat = species.habitat,
                        textDescription = species.description,
                        textLookAlikes = species.lookAlikes,
                        startMonth = species.startMonth,
                        endMonth = species.endMonth
                    )
                }
            }
        }
    }
}

@Composable
private fun Title(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 24.sp,
        fontWeight = Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}
@Composable
fun InformCard(
    modifier: Modifier = Modifier,
    textImage: String,
    textHabitat: String,
    textDescription: String,
    textLookAlikes: String,
    startMonth: Int,
    endMonth: Int,
){
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier(
        textImage,
        "drawable",
        context.packageName
    )
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                painter = if (imageId != 0) {
                    painterResource(id = imageId)
                } else {
                    painterResource(id = R.drawable.ic_launcher_background)
                },
                contentDescription = "Фото гриба/ягоды/растения/ореха",
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                modifier = Modifier
                    .padding( start = 10.dp ),
                text = textHabitat,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                modifier = Modifier
                    .padding( start = 10.dp ),
                text = textDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                modifier = Modifier
                    .padding( start = 10.dp ),
                text = textLookAlikes,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                modifier = Modifier
                    .padding( start = 10.dp ),
                text = "Период: c ${numberInString(startMonth).first} по ${numberInString(endMonth).second}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview(){
    FloraPointTheme {

    }
}