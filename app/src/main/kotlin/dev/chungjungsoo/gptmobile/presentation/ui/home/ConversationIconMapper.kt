package dev.chungjungsoo.gptmobile.presentation.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.SoupKitchen
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun mapConversationIcon(name: String): ImageVector = when (name) {
    "chat" -> Icons.Rounded.ChatBubbleOutline
    "code" -> Icons.Rounded.Code
    "science" -> Icons.Rounded.Science
    "lightbulb" -> Icons.Rounded.Lightbulb
    "school" -> Icons.Rounded.School
    "work" -> Icons.Rounded.Work
    "travel" -> Icons.Rounded.TravelExplore
    "restaurant" -> Icons.Rounded.Restaurant
    "music" -> Icons.Rounded.MusicNote
    "movie" -> Icons.Rounded.Movie
    "book" -> Icons.Rounded.Book
    "health" -> Icons.Rounded.HealthAndSafety
    "fitness" -> Icons.Rounded.FitnessCenter
    "finance" -> Icons.Rounded.AccountBalance
    "shopping" -> Icons.Rounded.LocalGroceryStore
    "home" -> Icons.Rounded.Home
    "nature" -> Icons.Rounded.Forest
    "pets" -> Icons.Rounded.Pets
    "sports" -> Icons.Rounded.SportsSoccer
    "gaming" -> Icons.Rounded.SportsEsports
    "art" -> Icons.Rounded.Palette
    "camera" -> Icons.Rounded.CameraAlt
    "phone" -> Icons.Rounded.Phone
    "email" -> Icons.Rounded.Email
    "calendar" -> Icons.Rounded.CalendarMonth
    "star" -> Icons.Rounded.Star
    "heart" -> Icons.Rounded.Favorite
    "rocket" -> Icons.Rounded.Rocket
    "globe" -> Icons.Rounded.Public
    "weather" -> Icons.Rounded.WbSunny
    "car" -> Icons.Rounded.DirectionsCar
    "plane" -> Icons.Rounded.Flight
    "train" -> Icons.Rounded.Train
    "cooking" -> Icons.Rounded.SoupKitchen
    "tools" -> Icons.Rounded.Handyman
    "psychology" -> Icons.Rounded.Psychology
    "language" -> Icons.Rounded.Language
    "math" -> Icons.Rounded.Science
    "history" -> Icons.Rounded.History
    "news" -> Icons.Rounded.Newspaper
    "question" -> Icons.AutoMirrored.Rounded.Help
    else -> Icons.Rounded.ChatBubbleOutline
}
