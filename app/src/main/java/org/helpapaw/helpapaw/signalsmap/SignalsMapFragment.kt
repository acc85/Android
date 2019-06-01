package org.helpapaw.helpapaw.signalsmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding
import org.helpapaw.helpapaw.images.ImageUtils
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.ISettingsRepository
import org.helpapaw.helpapaw.repository.PushNotificationsRepository
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.utils.StatusUtils
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SignalsMapFragment : BaseFragment(), SignalsMapContract.View {

    val userManager: UserManager by inject()
    val pushNotificationsRepository: PushNotificationsRepository by inject()
    val imageUtils:ImageUtils by inject()

    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var signalsGoogleMap: GoogleMap? = null
    private val mDisplayedSignals = ArrayList<Signal>()
    private val mSignalMarkers = HashMap<String, Signal>()
    private var mCurrentlyShownInfoWindowSignal: Signal? = null

    private var mCurrentLat: Double = 0.toDouble()
    private var mCurrentLong: Double = 0.toDouble()
    private var mZoom: Float = 0.toFloat()

    private var actionsListener: SignalsMapContract.UserActionsListener? = null

    lateinit var binding: FragmentSignalsMapBinding
    private var optionsMenu: Menu? = null

    private var mVisibilityAddSignal = false
    private var mFocusedSignalId: String? = null

    val settingsRepository: ISettingsRepository by inject()

    val signalsMapPresenter by inject<SignalsMapPresenter> { parametersOf(this) }

    val locationListener:LocationListener = getLocationChangeListener()

    val connectionCallback:GoogleApiClient.ConnectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnectionSuspended(i: Int) {
            Log.i(TAG, "Connection suspended")
            googleApiClient!!.connect()
        }

        override fun onConnected(bundle: Bundle?) {
            val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationRequest())

            val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

            result.setResultCallback { locationSettingsResult ->
                val status = locationSettingsResult.status
                val states = locationSettingsResult.locationSettingsStates
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                        } catch (e: Exception) {
                            // Ignore the error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }// All location settings are satisfied. The client can
                // initialize location requests here.
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
            }
            val cont = context
            //Protection for the case when activity is destroyed (e.g. when rotating)
            //Probably there is a better fix in the actual workflow but we need a quick fix as users experience a lot of crashes
            if (cont == null) {
                Log.e(TAG, "Context is null, exiting...")
                return
            }
            if (ContextCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog(activity, Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSIONS_REQUEST)
            } else {
                setAddSignalViewVisibility(mVisibilityAddSignal)
                if (signalsGoogleMap != null) {
                    signalsGoogleMap!!.isMyLocationEnabled = true
                }
                setLastLocation()
            }
        }
    }

    val connectionFailedListener = GoogleApiClient.OnConnectionFailedListener {
        connectionResult -> Log.i(TAG, "Connection failed with error code: " + connectionResult.errorCode)
    }

    lateinit var imageFileName: String

    /* Google Maps */

    private val mapReadyCallback: OnMapReadyCallback
        get() = OnMapReadyCallback { googleMap ->
            signalsGoogleMap = googleMap
            actionsListener!!.onInitSignalsMap()
            signalsGoogleMap!!.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM)
            signalsGoogleMap!!.setOnMapClickListener(mapClickListener)
            signalsGoogleMap!!.setOnMarkerClickListener(mapMarkerClickListener)
            signalsGoogleMap!!.setOnCameraIdleListener(mapCameraIdleListener)
        }

    private val mapClickListener = GoogleMap.OnMapClickListener {
        // Clicking on the map closes any open info window
        mCurrentlyShownInfoWindowSignal = null
    }

    private val mapMarkerClickListener = GoogleMap.OnMarkerClickListener { marker ->
        // Save the signal for the currently shown info window in case it should be reopen
        mCurrentlyShownInfoWindowSignal = mSignalMarkers[marker.id]
        false
    }

    private val mapCameraIdleListener = GoogleMap.OnCameraIdleListener {
        val cameraPosition = signalsGoogleMap!!.cameraPosition
        val cameraTarget = cameraPosition.target
        mCurrentLong = cameraTarget.longitude
        mCurrentLat = cameraTarget.latitude
        mZoom = cameraPosition.zoom
        val radius = calculateZoomToMeters()
        actionsListener!!.onLocationChanged(cameraTarget.latitude, cameraTarget.longitude, radius, settingsRepository!!.getTimeout())
    }

    val fabAddSignalClickListener: View.OnClickListener
        get() = View.OnClickListener {
            val visibility = binding.viewSendSignal.visibility == View.VISIBLE
            actionsListener!!.onAddSignalClicked(visibility)
        }


    val onSignalSendClickListener: View.OnClickListener
        get() = View.OnClickListener {
            val description = binding.viewSendSignal.signalDescription

            actionsListener!!.onSendSignalClicked(description)
        }

    val onSignalPhotoClickListener: View.OnClickListener
        get() = View.OnClickListener { actionsListener!!.onChoosePhotoIconClicked() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        val arguments = arguments
        if (arguments != null && arguments.containsKey(Signal.KEY_FOCUSED_SIGNAL_ID)) {

            mFocusedSignalId = arguments.getString(Signal.KEY_FOCUSED_SIGNAL_ID)
            arguments.remove(Signal.KEY_FOCUSED_SIGNAL_ID)
        }
        initLocationApi()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signals_map, container, false)
        val mapViewSavedInstanceState = savedInstanceState?.getBundle(MAP_VIEW_STATE)
        binding.mapSignals.onCreate(mapViewSavedInstanceState)


        mVisibilityAddSignal = savedInstanceState?.getBoolean(VIEW_ADD_SIGNAL) ?: false

        //        setAddSignalViewVisibility(mVisibilityAddSignal);
        if (binding.mapSignals != null) {
            binding.mapSignals.getMapAsync(mapReadyCallback)
        }

//        if (savedInstanceState == null) {
//            signalsMapPresenter = SignalsMapPresenter(this)
//        } else {
//            signalsMapPresenter = PresenterManager.getInstance().getPresenter(getScreenId())
//            signalsMapPresenter!!.view = this
//        }

        if (savedInstanceState != null) {
            signalsMapPresenter.view = this
        }

        actionsListener = signalsMapPresenter
        setHasOptionsMenu(true)

        binding.fabAddSignal.setOnClickListener(fabAddSignalClickListener)
        binding.viewSendSignal.setOnSignalSendClickListener(onSignalSendClickListener)
        binding.viewSendSignal.setOnSignalPhotoClickListener(onSignalPhotoClickListener)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.mapSignals.onStart()
        googleApiClient!!.connect()
    }

    override fun onResume() {
        super.onResume()
        binding.mapSignals.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapSignals.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapSignals.onStop()

        if (googleApiClient!!.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
            googleApiClient!!.disconnect()
        }
    }

    override fun onDestroy() {
        binding.mapSignals.onDestroy()
        super.onDestroy()
        settingsRepository!!.clearLocationData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //This MUST be done before saving any of your own or your base class's variables
        val mapViewSaveState = Bundle(outState)
        binding.mapSignals.onSaveInstanceState(mapViewSaveState)
        outState.putBundle(MAP_VIEW_STATE, mapViewSaveState)
        outState.putBoolean(VIEW_ADD_SIGNAL, mVisibilityAddSignal)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapSignals.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_signals_map, menu)

        this.optionsMenu = menu

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menu_item_refresh) {
            actionsListener!!.onRefreshButtonClicked()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateMapCameraPosition(latitude: Double, longitude: Double, zoom: Float?) {
        val latLng = LatLng(latitude, longitude)
        val cameraUpdate: CameraUpdate

        if (zoom != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        }
        signalsGoogleMap!!.animateCamera(cameraUpdate)
    }

    override fun displaySignals(signals: List<Signal>, showPopup: Boolean, focusedSignalId: String) {
        mFocusedSignalId = focusedSignalId
        displaySignals(signals, showPopup)
    }

    override fun displaySignals(signals: List<Signal>?, showPopup: Boolean) {
        var showPopup = showPopup

        var signal: Signal
        var markerToFocus: Marker? = null
        var signalToFocus: Signal? = null
        var markerToReShow: Marker? = null

        signals?.let {
            // Add new signals to the currently displayed ones
            for (newSignal in signals) {
                var alreadyPresent: Signal? = null
                for (presentSignal in mDisplayedSignals) {
                    if (newSignal.id == presentSignal.id) {
                        alreadyPresent = presentSignal
                        break
                    }
                }

                if (alreadyPresent != null) {
                    mDisplayedSignals.remove(alreadyPresent)
                }
                mDisplayedSignals.add(newSignal)
            }
        }

        if (signalsGoogleMap != null) {
            signalsGoogleMap!!.clear()
            signalsGoogleMap!!.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM)
            for (i in mDisplayedSignals.indices) {
                signal = mDisplayedSignals[i]

                val markerOptions = MarkerOptions()
                        .position(LatLng(signal.latitude, signal.longitude))
                        .title(signal.title)

                markerOptions.icon(BitmapDescriptorFactory.fromResource(StatusUtils.getPinResourceForCode(signal.status)))

                val marker = signalsGoogleMap!!.addMarker(markerOptions)
                mSignalMarkers[marker.id] = signal

                if (mFocusedSignalId != null) {
                    if (signal.id.equals(mFocusedSignalId!!, ignoreCase = true)) {
                        showPopup = true
                        markerToFocus = marker
                        signalToFocus = signal
                        mFocusedSignalId = null
                    }
                }
                // If an info window was open before signals refresh - reopen it
                if (mCurrentlyShownInfoWindowSignal != null) {
                    if (signal.id.equals(mCurrentlyShownInfoWindowSignal!!.id, ignoreCase = true)) {
                        markerToReShow = marker
                    }
                }
            }

//            val infoWindowAdapter = SignalInfoWindowAdapter(mSignalMarkers, activity!!.layoutInflater)
            val infoWindowAdapter: SignalInfoWindowAdapter by inject {
                parametersOf(mSignalMarkers, activity!!.layoutInflater)
            }
            signalsGoogleMap!!.setInfoWindowAdapter(infoWindowAdapter)

            signalsGoogleMap!!.setOnInfoWindowClickListener { marker -> actionsListener!!.onSignalInfoWindowClicked(mSignalMarkers[marker.id]) }

            if (showPopup && markerToFocus != null) {
                markerToFocus.showInfoWindow()
                updateMapCameraPosition(signalToFocus!!.latitude, signalToFocus.longitude, null)
            } else markerToReShow?.showInfoWindow()
        }
    }

    /* Location API */

    fun getLocationChangeListener():LocationListener{
        return LocationListener { location -> handleNewLocation(location) }
    }

    private fun initLocationApi() {
        googleApiClient = GoogleApiClient.Builder(context!!)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build()

        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((30 * 1000).toLong())        // 30 seconds, in milliseconds
                .setFastestInterval((10 * 1000).toLong()) // 10 seconds, in milliseconds
    }


    @SuppressLint("MissingPermission")
    private fun setLastLocation() {
        if (!mVisibilityAddSignal) {
            val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            location?.let { handleNewLocation(it) }
                    ?: LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener)
        }
    }

    private fun handleNewLocation(location: Location) {
        val longitude = settingsRepository!!.getLastShownLongitude()
        val latitude = settingsRepository!!.getLastShownLatitude()
        val newZoom = settingsRepository!!.getLastShownZoom()

        mCurrentLat = if (latitude == 0.0) location.latitude else latitude
        mCurrentLong = if (longitude == 0.0) location.longitude else longitude
        val zoom = if (newZoom == 0f) calculateMetersToZoom() else newZoom
        updateMapCameraPosition(mCurrentLat, mCurrentLong, zoom)
        actionsListener!!.onLocationChanged(mCurrentLat, mCurrentLong, settingsRepository!!.getRadius(), settingsRepository!!.getTimeout())
        pushNotificationsRepository.saveNewDeviceLocation(location)
    }


    private fun calculateZoomToMeters(): Int {
        val visibleRegion = signalsGoogleMap!!.projection.visibleRegion
        val distanceWidth = FloatArray(1)
        val distanceHeight = FloatArray(1)

        val farRight = visibleRegion.farRight
        val farLeft = visibleRegion.farLeft
        val nearRight = visibleRegion.nearRight
        val nearLeft = visibleRegion.nearLeft

        //calculate the distance width (left <-> right of map on screen)
        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth)

        //calculate the distance height (top <-> bottom of map on screen)
        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight)

        //visible radius is (smaller distance) / 2:
        val radius = if (distanceWidth[0] < distanceHeight[0]) distanceWidth[0] / 2 else distanceHeight[0] / 2
        return radius.toInt()
    }

    private fun calculateMetersToZoom(): Float {
        val radius = (settingsRepository!!.getRadius() * 1000).toDouble()
        val scale = radius / 500
        val zoomLevel = (16 - Math.log(scale) / Math.log(2.0)).toFloat()
        return zoomLevel - 0.5f
    }

    override fun showMessage(message: String) {
        Snackbar.make(binding.fabAddSignal, message, Snackbar.LENGTH_LONG).show()
    }

    override fun setAddSignalViewVisibility(visibility: Boolean) {

        mVisibilityAddSignal = visibility

        if (visibility) {
            showAddSignalView()
            showAddSignalPin()

            binding.fabAddSignal.setImageResource(R.drawable.ic_close)
        } else {
            hideAddSignalView()
            hideAddSignalPin()

            binding.fabAddSignal.setImageResource(R.drawable.fab_add)
        }
    }

    private fun showAddSignalView() {
        binding.viewSendSignal.visibility = View.VISIBLE
        binding.viewSendSignal.alpha = 0.0f

        val height = (activity as SignalsMapActivity).supportActionBar?.height?.toFloat() ?: 0f
        binding.viewSendSignal
                .animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(binding.viewSendSignal.height + height)
                .alpha(1.0f)
    }

    private fun hideAddSignalView() {
        binding.viewSendSignal
                .animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(-(binding.viewSendSignal.height * 1.2f))
                .withEndAction { binding.viewSendSignal.visibility = View.INVISIBLE }
    }

    private fun showAddSignalPin() {
        binding.addSignalPin.visibility = View.VISIBLE
        binding.addSignalPin.alpha = 0.0f

        binding.addSignalPin
                .animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(200)
                .translationY(0f)
                .alpha(1.0f)
    }

    private fun hideAddSignalPin() {

        binding.addSignalPin
                .animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(200)
                .alpha(0.0f)
                .withEndAction { binding.addSignalPin.visibility = View.INVISIBLE }
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun showSendPhotoBottomSheet() {
        val sendPhotoBottomSheet = SendPhotoBottomSheet()
        sendPhotoBottomSheet.setListener(object : SendPhotoBottomSheet.PhotoTypeSelectListener {
            override fun onPhotoTypeSelected(photoType: Long) {
                if (photoType == SendPhotoBottomSheet.PhotoType.CAMERA) {
                    actionsListener!!.onCameraOptionSelected()
                } else if (photoType == SendPhotoBottomSheet.PhotoType.GALLERY) {
                    actionsListener!!.onGalleryOptionSelected()
                }
            }
        })
        sendPhotoBottomSheet.show(fragmentManager!!, SendPhotoBottomSheet.TAG)
    }

    override fun openCamera() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(activity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_FOR_CAMERA)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(context!!.packageManager) != null) {
                val timeStamp = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).format(Date())
                imageFileName = PHOTO_PREFIX + timeStamp + PHOTO_EXTENSION
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUtils.getPhotoFileUri(context, imageFileName))
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    override fun openGallery() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivityForResult(intent, REQUEST_GALLERY)
            }
        }
    }

    override fun saveImageFromURI(photoUri: Uri?) {

        // This segment works once the permission is handled
        try {
            val path: String
            val parcelFileDesc = activity!!.contentResolver.openFileDescriptor(photoUri!!, "r")
            val fileDesc = parcelFileDesc!!.fileDescriptor
            val photo = BitmapFactory.decodeFileDescriptor(fileDesc)
            path = MediaStore.Images.Media.insertImage(context!!.contentResolver, photo, "temp", null)
            val photoFile = imageUtils.getFromMediaUri(context, context!!.contentResolver, Uri.parse(path))

            if (photoFile != null) {
                actionsListener!!.onSignalPhotoSelected(Uri.fromFile(photoFile).path)
            }

            parcelFileDesc.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun openLoginScreen() {
        val intent = Intent(context, AuthenticationActivity::class.java)
        startActivity(intent)
    }

//    override fun getPresenter(): Presenter<*>? {
//        return signalsMapPresenter
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val takenPhotoUri = imageUtils.getPhotoFileUri(context, imageFileName)
                actionsListener!!.onSignalPhotoSelected(takenPhotoUri!!.path)
            }
        }

        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                saveImageFromURI(data.data)
            } else {
                // DRY!!
                val photoFile = imageUtils.getFromMediaUri(context, context!!.contentResolver, data.data)
                if (photoFile != null) {
                    actionsListener!!.onSignalPhotoSelected(Uri.fromFile(photoFile).path)
                }
            }

        }

        if (requestCode == REQUEST_SIGNAL_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {
                val signal = data!!.getParcelableExtra<Signal>("signal")
                if (signal != null) {
                    actionsListener!!.onSignalStatusUpdated(signal)
                }
            }
        }
    }

    override fun setThumbnailImage(photoUri: String) {
        val res = resources
        val drawable = RoundedBitmapDrawableFactory.create(res, imageUtils.getRotatedBitmap(File(photoUri)))
        drawable.cornerRadius = 10f
        binding.viewSendSignal.setSignalPhoto(drawable)
    }

    override fun clearSignalViewData() {
        binding.viewSendSignal.clearData()
    }

    override fun setSignalViewProgressVisibility(visibility: Boolean) {
        binding.viewSendSignal.setProgressVisibility(visibility)
    }

    override fun openSignalDetailsScreen(signal: Signal) {
        val intent = Intent(context, SignalDetailsActivity::class.java)
        intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, signal)
        startActivityForResult(intent, REQUEST_SIGNAL_DETAILS)

        settingsRepository!!.setLastShownLatitude(mCurrentLat)
        settingsRepository!!.setLastShownLongitude(mCurrentLong)
        settingsRepository!!.setLastShownZoom(mZoom)
    }

    override fun closeSignalsMapScreen() {
        if (activity != null) {
            activity!!.finish()
        }
    }

    override fun showDescriptionErrorMessage() {
        showMessage(getString(R.string.txt_description_required))
    }

    override fun showAddedSignalMessage() {
        showMessage(getString(R.string.txt_signal_added_successfully))
    }

    override fun showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet))
    }

    override fun setProgressVisibility(visibility: Boolean) {
        if (optionsMenu != null) {
            val refreshItem = optionsMenu!!.findItem(R.id.menu_item_refresh)

            if (refreshItem != null) {
                if (visibility) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.toolbar_progress)
                    if (refreshItem.actionView != null) {
                        val progressBar = refreshItem.actionView.findViewById(R.id.toolbar_progress_bar) as ProgressBar
                        progressBar?.indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                    }
                } else {

                    MenuItemCompat.setActionView(refreshItem, null)
                }
            }
        }
    }

    override fun isActive(): Boolean {
        return isAdded
    }

    override fun onLogoutSuccess() {
        Snackbar.make(binding.root.findViewById(R.id.fab_add_signal), R.string.txt_logout_succeeded, Snackbar.LENGTH_LONG).show()
    }

    override fun onLogoutFailure(message: String) {
        AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), message, true, this.fragmentManager)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSIONS_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                signalsGoogleMap!!.isMyLocationEnabled = true
                setLastLocation()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_location_permissions_for_map, Toast.LENGTH_SHORT)
                        .show()
            }
            READ_EXTERNAL_STORAGE_FOR_CAMERA -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionsListener!!.onStoragePermissionForCameraGranted()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_storage_permissions_for_camera, Toast.LENGTH_SHORT)
                        .show()
            }

            READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionsListener!!.onStoragePermissionForGalleryGranted()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_storage_permissions_for_gallery, Toast.LENGTH_SHORT)
                        .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun showPermissionDialog(activity: Activity?, permission: String, permissionCode: Int) {
        if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), permissionCode)
        }
    }

    /* OnClick Listeners */

    fun onBackPressed() {
        actionsListener!!.onBackButtonPressed()
    }

    companion object {

        val TAG = SignalsMapFragment::class.java.simpleName
        private val MAP_VIEW_STATE = "mapViewSaveState"
        private val DATE_TIME_FORMAT = "yyyyMMdd_HHmmss"
        private val PHOTO_PREFIX = "JPEG_"
        private val PHOTO_EXTENSION = ".jpg"

        private val LOCATION_PERMISSIONS_REQUEST = 1
        private val REQUEST_CAMERA = 2
        private val REQUEST_GALLERY = 3
        private val READ_EXTERNAL_STORAGE_FOR_CAMERA = 4
        private val READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY = 5
        private val REQUEST_SIGNAL_DETAILS = 6
        private val REQUEST_CHECK_SETTINGS = 214
        private val VIEW_ADD_SIGNAL = "view_add_signal"
        private val PADDING_TOP = 190
        private val PADDING_BOTTOM = 160
        private val MARKER_LATITUDE = "marker_latitude"
        private val MARKER_LONGITUDE = "marker_longitude"

        fun newInstance(): SignalsMapFragment {
            return SignalsMapFragment()
        }

        fun newInstance(focusedSignalId: String): SignalsMapFragment {
            val signalsMapFragment = SignalsMapFragment()
            val args = Bundle()
            args.putString(Signal.KEY_FOCUSED_SIGNAL_ID, focusedSignalId)
            signalsMapFragment.arguments = args
            return signalsMapFragment
        }
    }
}// Required empty public constructor