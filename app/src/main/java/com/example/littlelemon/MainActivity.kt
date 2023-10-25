package com.example.littlelemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.littlelemon.ui.theme.LittleLemonTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(contentType = ContentType("text", "plain"))
        }
    }

    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LittleLemonTheme {
                // Observa los elementos del menú de la base de datos local
                // add databaseMenuItems code here
                val databaseMenuItems by database.menuItemDao().getAll().observeAsState(emptyList())

                // add orderMenuItems variable here
                var orderMenuItems by remember { mutableStateOf(false) }

                // add menuItems variable here
                var menuItems = if (orderMenuItems) {
                    databaseMenuItems.sortedBy { it.title }
                } else {
                    databaseMenuItems
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "logo",
                        modifier = Modifier.padding(50.dp)
                    )

                    // Botón para ordenar por nombre
                    Button(
                        onClick = { orderMenuItems = true },
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Ordenar por Nombre")
                    }

                    // Declaración de la variable searchPhrase
                    var searchPhrase by remember { mutableStateOf("") }

                    // Barra de búsqueda
                    OutlinedTextField(
                        value = searchPhrase,
                        onValueChange = { newValue ->
                            searchPhrase = newValue
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("Ingrese un plato", color = Color.LightGray) },
                        label = { Text("Buscar", color = Color. Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 50.dp, end = 50.dp)
                    )



                    // Filtrar los elementos del menú si searchPhrase no está vacío
                    if (searchPhrase.isNotEmpty()) {
                        menuItems = menuItems.filter { menuItem ->
                           menuItem.title.contains(searchPhrase, ignoreCase = true)
                        }
                    }

                    MenuItemsList(menuItems)

                    // add Button code here

                    // add searchPhrase variable here

                    // Add OutlinedTextField

                    // add is not empty check here
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
           if (database.menuItemDao().isEmpty()) {
               val menuItemsNetwork = fetchMenu()
               saveMenuToDatabase(menuItemsNetwork)
           }
        }
    }

    private suspend fun fetchMenu(): List<MenuItemNetwork> {
        //-try {
            val dataURL = "https://raw.githubusercontent.com/Meta-Mobile-Developer-PC/Working-With-Data-API/main/littleLemonSimpleMenu.json"
            val response: HttpResponse = httpClient.get(dataURL)

           if (response.status.isSuccess()) {
               val menuNetwork = response.body<MenuNetwork>()
               return menuNetwork.menu
           } else {
              // Handle the HTTP error here, you can log it or throw an exception
              throw Exception("HTTP request failed with status code: ${response.status}")
           }
        //-} catch (e: Exception) {
           // Handle exceptions such as network errors here
        //-throw e
        //-}
    }


    private fun saveMenuToDatabase(menuItemsNetwork: List<MenuItemNetwork>) {
       val menuItemsRoom = menuItemsNetwork.map { it.toMenuItemRoom() }
       database.menuItemDao().insertAll(*menuItemsRoom.toTypedArray())
    }
}

@Composable
private fun MenuItemsList(items: List<MenuItemRoom>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 20.dp)
    ) {
        items(
            items = items,
            itemContent = { menuItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(menuItem.title)
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp),
                        textAlign = TextAlign.Right,
                        text = "%.2f".format(menuItem.price)
                    )
                }
            }
        )
    }
}
