package com.example.honk

import LocationViewModel
import android.Manifest
import android.content.ContentValues.TAG
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.honk.databinding.ActivityMainBinding
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import kotlin.getValue
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.honk.data.firebase.FirebaseModule.auth
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay
// testing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.honk.notifications.NotificationHelper
import com.example.honk.repository.UserRepository
import kotlinx.coroutines.flow.first
import android.app.AlarmManager
import android.content.pm.ApplicationInfo
import android.provider.Settings
import com.example.honk.data.entities.UserEntity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore

import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val locationViewModel: LocationViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                println("loc perm granted")
            } else {
                println("loc perm NOT granted")
            }
        }
    private lateinit var binding: ActivityMainBinding

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                println("notification perm granted")
            } else {
                println("notification perm NOT granted")
            }
        }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val alarmManager = getSystemService(AlarmManager::class.java)
            val canSchedule = alarmManager.canScheduleExactAlarms()
            if (!canSchedule) {
                // Открываем системный экран, где юзер включает "Allow exact alarms" для нашего приложения
                val intent = android.content.Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        // testing
//         val auth1 = FirebaseAuth.getInstance()
//         val db1 = FirebaseFirestore.getInstance()

        val db = Firebase.firestore

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.calendarFragment,
                R.id.notesFragment,
                R.id.categoriesFragment,
                R.id.gooseFragment,
                R.id.folderDetailsFragment
            )
        )

        val sharedPreferences = getSharedPreferences("app_theme", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(nightMode)

        binding.navView.setupWithNavController(navController)

        locationViewModel.initialize(applicationContext)

        checkAndRequestPermission(::startLocationUpdates)

        // Create notification channels (Android 8+)
        NotificationHelper.createNotificationChannels(this)

        // Ask for notification permission on Android 13+
        checkAndRequestNotificationPermission()

        checkAndRequestExactAlarmPermission()

        //sample for demo
        locationViewModel.currentLocation.observe(this, Observer { location ->
            println(location)
        })

        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData.getString("WEB_CLIENT_ID")

        lifecycleScope.launch {
            val nonce = "yGf0bNrjI1BxdZ6JQM2gIsePGlUUgHpuRVo7JC7LrMQgwbxlOj"
            val webClientID = getString(R.string.default_web_client_id)
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientID)
                .setAutoSelectEnabled(true)
                // nonce string to use when generating a Google ID token
                .setNonce(nonce)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val e = signIn(request, applicationContext)
            if (e is NoCredentialException) {
                val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientID)
                    .setNonce(nonce)
                    .build()

                val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOptionFalse)
                    .build()

                signIn(requestFalse, applicationContext)
            }
        }
}

private fun checkAndRequestPermission(actionIfGranted: () -> Unit) {
if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
) {
    actionIfGranted()
} else {
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
}
}

private fun checkAndRequestNotificationPermission() {
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
    val hasPermission = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
}

private fun startLocationUpdates() {
locationViewModel.startLocationUpdates()
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
val credentialManager = CredentialManager.create(context)
val failureMessage = "Sign in failed!"
var e: Exception? = null
delay(250)
try {
    val result = credentialManager.getCredential(
        request = request,
        context = context,
    )

    println("(☞ﾟヮﾟ)☞  Sign in Successful!  ☜(ﾟヮﾟ☜)")

    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)

} catch (e: GetCredentialException) {
    println("$failureMessage: Failure getting credentials")

} catch (e: GoogleIdTokenParsingException) {
    println("$failureMessage: Issue with parsing received GoogleIdToken")

} catch (e: NoCredentialException) {
    println("$failureMessage: No credentials found")
    return e

} catch (e: GetCredentialCustomException) {
    println("$failureMessage: Issue with custom credential request")

} catch (e: GetCredentialCancellationException) {
    println("$failureMessage: Sign-in was cancelled")
}
return e
}

private fun firebaseAuthWithGoogle(idToken: String) {
val credential = GoogleAuthProvider.getCredential(idToken, null)
auth.signInWithCredential(credential)
    .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInWithCredential:success")

            //create user profile in firestore
            val firebaseUser = auth.currentUser
            lifecycleScope.launch {
            val fetchedUser = UserRepository().getById(firebaseUser!!.uid).first()
            if (fetchedUser?.id != firebaseUser.uid) {
                val userProfile = UserEntity(
                    id = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    authProvider = "google",
                    googleId = firebaseUser.uid,
                    storagePreference = "default",
                    defaultSoundId = "default"
                )
                 UserRepository().save(firebaseUser.uid, userProfile) }
                Log.d(TAG, "signUpWithCredential:success")
            }
        } else {
            // If sign in fails, display a message to the user
            Log.w(TAG, "signInWithCredential:failure", task.exception)
        }
    }
}
}
