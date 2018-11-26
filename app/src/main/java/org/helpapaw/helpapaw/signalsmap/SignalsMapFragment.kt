package org.helpapaw.helpapaw.signalsmap

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.R.id.menu_item_refresh
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessPhotoRepository
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity
import org.helpapaw.helpapaw.utils.StatusUtils
import org.helpapaw.helpapaw.utils.images.ImageUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SignalsMapFragment : BaseFragment(), SignalsMapContract.View,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    companion object {
        val TAG = SignalsMapFragment::class.java.simpleName
        private const val MAP_VIEW_STATE = "mapViewSaveState"
        private const val DEFAULT_MAP_ZOOM = 14.5f
        private const val DATE_TIME_FORMAT = "yyyyMMdd_HHmmss"
        private const val PHOTO_PREFIX = "JPEG_"
        private const val PHOTO_EXTENSION = ".jpg"

        private const val LOCATION_PERMISSIONS_REQUEST = 1
        private const val REQUEST_CAMERA = 2
        private const val REQUEST_GALLERY = 3
        private const val READ_EXTERNAL_STORAGE_FOR_CAMERA = 4
        private const val READ_EXTERNAL_STORAGE_FOR_GALLERY = 5
        private const val REQUEST_SIGNAL_DETAILS = 6
        private const val REQUEST_CHECK_SETTINGS = 214
        private const val VIEW_ADD_SIGNAL = "view_add_signal"
        private const val PADDING_TOP = 190
        private const val PADDING_BOTTOM = 160
        private const val MARKER_LATITUDE = "marker_latitude"
        private const val MARKER_LONGITUDE = "marker_longitude"

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

    private var imageFileName: String = ""

    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var signalsGoogleMap: GoogleMap? = null
    private val mDisplayedSignals = ArrayList<Signal>()

    @Inject
    lateinit var mSignalMarkers:HashMap<String,Signal>

    private var mCurrentlyShownInfoWindowSignal: Signal? = null

    private var mCurrentLat: Double = 0.toDouble()
    private var mCurrentLong: Double = 0.toDouble()

    @Inject
    lateinit var signalsMapPresenter: SignalsMapPresenter

    @Inject
    lateinit var photoRepository:BackendlessPhotoRepository

    @Inject
    lateinit var infoWindowAdapter:SignalInfoWindowAdapter

    private var actionsListener: SignalsMapContract.UserActionsListener? = null

    private lateinit var binding: FragmentSignalsMapBinding
    private var optionsMenu: Menu? = null

    private var mVisibilityAddSignal = false
    private var mFocusedSignalId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSignalMarkers.clear()
        mFocusedSignalId = arguments?.getString(Signal.KEY_FOCUSED_SIGNAL_ID)
        arguments?.remove(Signal.KEY_FOCUSED_SIGNAL_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signals_map, container, false)
        val mapViewSavedInstanceState = savedInstanceState?.getBundle(MAP_VIEW_STATE)
        binding.mapSignals.onCreate(mapViewSavedInstanceState)


        mVisibilityAddSignal = savedInstanceState?.getBoolean(VIEW_ADD_SIGNAL) ?: false

        //        setAddSignalViewVisibility(mVisibilityAddSignal);
        binding.mapSignals.getMapAsync(getMapReadyCallback())
        actionsListener = signalsMapPresenter
        initLocationApi()

        setHasOptionsMenu(true)

        binding.fabAddSignal.setOnClickListener(getFabAddSignalClickListener())
        binding.viewSendSignal.setOnSignalSendClickListener(getOnSignalSendClickListener())
        binding.viewSendSignal.setOnSignalPhotoClickListener(getOnSignalPhotoClickListener())

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.mapSignals.onStart()
        googleApiClient?.connect()
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

        if (googleApiClient?.isConnected() ?: false) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            googleApiClient?.disconnect()
        }
    }

    override fun onDestroy() {
        binding.mapSignals.onDestroy()
        super.onDestroy()
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
        inflater?.inflate(R.menu.menu_signals_map, menu)

        this.optionsMenu = menu

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            menu_item_refresh -> {
                actionsListener?.onRefreshButtonClicked()
                return true
            }else->
                return super.onOptionsItemSelected(item)
        }
    }

    /* Google Maps */

    private fun getMapReadyCallback(): OnMapReadyCallback {
        return OnMapReadyCallback { googleMap ->
            signalsGoogleMap = googleMap
            actionsListener?.onInitSignalsMap()
            signalsGoogleMap?.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM)
            signalsGoogleMap?.setOnMapClickListener(mapClickListener)
            signalsGoogleMap?.setOnMarkerClickListener(mapMarkerClickListener)
            signalsGoogleMap?.setOnCameraIdleListener(mapCameraIdleListener)
        }
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
        val cameraTarget = signalsGoogleMap?.getCameraPosition()?.target
        actionsListener?.onLocationChanged(cameraTarget!!.latitude, cameraTarget.longitude)
    }

    override fun updateMapCameraPosition(latitude: Double, longitude: Double, zoom: Float?) {
        val center = CameraUpdateFactory.newLatLng(LatLng(latitude, longitude))
        signalsGoogleMap?.moveCamera(center)

        if (zoom != null) {
            val cameraZoom = CameraUpdateFactory.zoomTo(zoom)
            signalsGoogleMap?.animateCamera(cameraZoom)
        }
    }

    override fun displaySignals(signals: MutableList<Signal>, showPopup: Boolean, focusedSignalId: String) {
        mFocusedSignalId = focusedSignalId
        displaySignals(signals, showPopup)
    }

    override fun displaySignals(signals: List<Signal>, showPopup: Boolean) {

        var showingPopup = showPopup
        var signal: Signal
        var markerToFocus: Marker? = null
        var signalToFocus: Signal? = null
        var markerToReShow: Marker? = null

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

        if (signalsGoogleMap != null) {
            signalsGoogleMap?.clear()
            signalsGoogleMap?.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM)
            for (i in mDisplayedSignals.indices) {
                signal = mDisplayedSignals[i]

                val markerOptions = MarkerOptions()
                        .position(LatLng(signal.latitude, signal.longitude))
                        .title(signal.title)

                markerOptions.icon(BitmapDescriptorFactory.fromResource(StatusUtils.getPinResourceForCode(signal.status)))

                val marker = signalsGoogleMap?.addMarker(markerOptions)
                mSignalMarkers[marker!!.id] = signal

                if (mFocusedSignalId != null) {
                    if (signal.id.equals(mFocusedSignalId, ignoreCase = true)) {
                        showingPopup= true
                        markerToFocus = marker
                        signalToFocus = signal
                        mFocusedSignalId = null
                    }
                }
                // If an info window was open before signals refresh - reopen it
                if (mCurrentlyShownInfoWindowSignal != null) {
                    if (signal.id.equals(mCurrentlyShownInfoWindowSignal?.id!!, ignoreCase = true)) {
                        markerToReShow = marker
                    }
                }
            }

            signalsGoogleMap?.setInfoWindowAdapter(infoWindowAdapter)

            signalsGoogleMap?.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener { marker -> actionsListener?.onSignalInfoWindowClicked(mSignalMarkers[marker.id]!!) })

            if (showingPopup && markerToFocus != null) {
                markerToFocus.showInfoWindow()
                updateMapCameraPosition(signalToFocus!!.latitude, signalToFocus.longitude, null)
            } else markerToReShow?.showInfoWindow()
        }
    }

    /* Location API */

    private fun initLocationApi() {
        googleApiClient = GoogleApiClient.Builder(context!!)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build()

        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((30 * 1000).toLong())        // 30 seconds, in milliseconds
                .setFastestInterval((10 * 1000).toLong()) // 10 seconds, in milliseconds
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
            showPermissionDialog(activity as AppCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSIONS_REQUEST)
        } else {
            setAddSignalViewVisibility(mVisibilityAddSignal)
            signalsGoogleMap?.setMyLocationEnabled(true)

            if (!mVisibilityAddSignal) {

                val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                if (location == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
                } else {
                    handleNewLocation(location)
                }
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "Connection suspended")
        googleApiClient?.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed with error code: " + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {
        handleNewLocation(location)
    }

    private fun handleNewLocation(location: Location) {

        Log.d(TAG, location.toString())
        mCurrentLat = location.latitude
        mCurrentLong = location.longitude

        updateMapCameraPosition(mCurrentLat, mCurrentLong, DEFAULT_MAP_ZOOM)
        actionsListener?.onLocationChanged(mCurrentLat, mCurrentLong)
    }


    override fun showMessage(message: String) {
        Snackbar.make(binding.fabAddSignal, message, Snackbar.LENGTH_LONG).show()
    }

    fun getFabAddSignalClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val visibility = binding.viewSendSignal.visibility == View.VISIBLE
            actionsListener?.onAddSignalClicked(visibility)
        }
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

        binding.viewSendSignal
                .animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(binding.viewSendSignal.height * 1.2f)
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
        sendPhotoBottomSheet.listener = object : SendPhotoBottomSheet.PhotoTypeSelectListener {
            override fun onPhotoTypeSelected(@SendPhotoBottomSheet.Companion.PhotoType photoType: Int) {
                if (photoType == SendPhotoBottomSheet.CAMERA) {
                    actionsListener?.onCameraOptionSelected()
            } else if (photoType == SendPhotoBottomSheet.GALLERY) {
                    actionsListener?.onGalleryOptionSelected()
                }
            }
        }
        sendPhotoBottomSheet.show(fragmentManager!!, SendPhotoBottomSheet.TAG)
    }

    override fun openCamera() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(activity as AppCompatActivity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_FOR_CAMERA)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(context!!.packageManager) != null) {
                val timeStamp = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).format(Date())
                imageFileName = PHOTO_PREFIX + timeStamp + PHOTO_EXTENSION
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getInstance().getPhotoFileUri(context, imageFileName))
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    override fun openGallery() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(activity as AppCompatActivity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_FOR_GALLERY)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivityForResult(intent, REQUEST_GALLERY)
            }
        }
    }

    override fun openLoginScreen() {
        val intent = Intent(context, AuthenticationActivity::class.java)
        startActivity(intent)
    }

    override fun getPresenter(): Presenter<*>? {
        return signalsMapPresenter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val takenPhotoUri = ImageUtils.getInstance().getPhotoFileUri(context, imageFileName)
                actionsListener?.onSignalPhotoSelected(takenPhotoUri!!.path!!)
            }
        }

        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val photoMediaUri = data.data
            val photoFile = ImageUtils.getInstance().getFromMediaUri(context, context!!.contentResolver, photoMediaUri)
            actionsListener?.onSignalPhotoSelected(Uri.fromFile(photoFile).path!!)
        }

        if (requestCode == REQUEST_SIGNAL_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {
                val signal = data!!.getParcelableExtra<Signal>("signal")
                if (signal != null) {
                    actionsListener?.onSignalStatusUpdated(signal)
                }
            }
        }
    }

    override fun setThumbnailImage(photoUri: String?) {
        val res = resources
        val drawable = RoundedBitmapDrawableFactory.create(res, ImageUtils.getInstance().getRotatedBitmap(File(photoUri!!)))
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
            val refreshItem = optionsMenu?.findItem(R.id.menu_item_refresh)

            if (refreshItem != null) {
                if (visibility) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.toolbar_progress)
                    if (MenuItemCompat.getActionView(refreshItem) != null) {
                        val progressBar = MenuItemCompat.getActionView(refreshItem).findViewById<View>(R.id.toolbar_progress_bar) as ProgressBar
                        progressBar.indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_FOR_CAMERA -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionsListener?.onStoragePermissionForCameraGranted()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_storage_permissions_for_camera, Toast.LENGTH_SHORT)
                        .show()
            }

            READ_EXTERNAL_STORAGE_FOR_GALLERY -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                actionsListener?.onStoragePermissionForGalleryGranted()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_storage_permissions_for_gallery, Toast.LENGTH_SHORT)
                        .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun showPermissionDialog(activity: AppCompatActivity, permission: String, permissionCode: Int) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), permissionCode)
        }
    }

    /* OnClick Listeners */

    fun onBackPressed() {
        actionsListener?.onBackButtonPressed()
    }

    fun getOnSignalSendClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val description = binding.viewSendSignal.getSignalDescription()

            actionsListener?.onSendSignalClicked(description)
        }
    }

    fun getOnSignalPhotoClickListener(): View.OnClickListener {
        return View.OnClickListener { actionsListener?.onChoosePhotoIconClicked() }
    }

}