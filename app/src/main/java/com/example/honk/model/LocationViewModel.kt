import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource

class LocationViewModel : ViewModel() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation

    fun initialize(context: Context) {
        if (!::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        startLocationUpdates()
        println(currentLocation)
    }

    /**
     * Gets the location once using the best available method.
     * Requires ACCESS_FINE_LOCATION permission to be granted.
     */
    @SuppressLint("MissingPermission")
    fun getLocationOnce() {
        cancellationTokenSource?.cancel() // Cancel previous request if it exists
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY, // Use high accuracy
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                _currentLocation.postValue(location)
            }
        }.addOnFailureListener { e ->
            println("fail, "+e.message)
        }
    }

    /**
     * Starts continuous location updates.
     * Requires ACCESS_FINE_LOCATION permission to be granted.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateDistanceMeters(0f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        // 2. Define the Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    _currentLocation.postValue(location)
                }
            }
        }

        // 3. Request updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            null /* Looper */
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        cancellationTokenSource?.cancel()
    }
}