package org.helpapaw.helpapaw.signalsmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding
import org.helpapaw.helpapaw.images.ImageUtils
import org.helpapaw.helpapaw.models.KEY_FOCUSED_SIGNAL_ID
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity
import org.helpapaw.helpapaw.viewmodels.ERROR_TYPE
import org.helpapaw.helpapaw.viewmodels.MESSAGE_TYPE
import org.helpapaw.helpapaw.viewmodels.SignalsMapResult
import org.helpapaw.helpapaw.viewmodels.SignalsMapViewModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*


const val MAP_VIEW_STATE = "mapViewSaveState"
const val DATE_TIME_FORMAT_VIEW = "yyyyMMdd_HHmmss"
const val PHOTO_PREFIX = "JPEG_"
const val PHOTO_EXTENSION = ".jpg"
const val LOCATION_PERMISSIONS_REQUEST = 1
const val REQUEST_CAMERA = 2
const val REQUEST_GALLERY = 3
const val READ_EXTERNAL_STORAGE_FOR_CAMERA = 4
const val READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY = 5
const val REQUEST_SIGNAL_DETAILS = 6
const val REQUEST_CHECK_SETTINGS = 214

class SignalsMapFragment : BaseFragment(), SignalsMapContract.View {

    private val imageUtils: ImageUtils by inject()

    val viewModel: SignalsMapViewModel by inject()

    val locationSettingsRequest: LocationSettingsRequest by inject()
    private val googleApiClient: GoogleApiClient by inject()
    lateinit var binding: FragmentSignalsMapBinding
    private var optionsMenu: Menu? = null

    //    private var mFocusedSignalId: String? = null
    private var imageFileName: String = ""

    private val connectionCallback: GoogleApiClient.ConnectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnectionSuspended(i: Int) {
            Log.i(TAG, "Connection suspended")
            googleApiClient.connect()
        }

        override fun onConnected(bundle: Bundle?) {
            val result = LocationServices.getSettingsClient(context!!).checkLocationSettings(locationSettingsRequest)
            result.addOnFailureListener { task ->
                (task as? ApiException)?.let { status ->
                    if (status.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        (status as? ResolvableApiException)?.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                    }
                }
            }

//            result.addOnCompleteListener {task->
//                try{
//                   task.getResult(ApiException::class.java)
//                }catch (exception:ApiException){
//                    when (exception.statusCode) {
//                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
//                            (exception as? ResolvableApiException)?.let{resolvableApiException ->
//                            resolvableApiException.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
//                        }
//                    }
//                }
//
//            }
            //Protection for the case when activity is destroyed (e.g. when rotating)
            //Probably there is a better fix in the actual workflow but we need a quick fix as users experience a lot of crashes
            context?.let { ctx ->
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    showPermissionDialog(activity, Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSIONS_REQUEST)
                } else {
                    viewModel.signalsGoogleMap?.let { gm ->
                        gm.isMyLocationEnabled = true
                    }
                    viewModel.setLastLocation()
                }
            } ?: run {
                Log.e(TAG, "Context is null, exiting...")
                return
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        val arguments = arguments
        if (arguments != null && arguments.containsKey(KEY_FOCUSED_SIGNAL_ID)) {
            viewModel.mFocusedSignalId = arguments.getString(KEY_FOCUSED_SIGNAL_ID)
            arguments.remove(KEY_FOCUSED_SIGNAL_ID)
        }
        googleApiClient.registerConnectionCallbacks(connectionCallback)
        googleApiClient.registerConnectionFailedListener { connectionResult ->
            Log.i(TAG, "Connection failed with error code: " + connectionResult.errorCode)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signals_map, container, false)
        val mapViewSavedInstanceState = savedInstanceState?.getBundle(MAP_VIEW_STATE)

        binding.viewModel = viewModel
        binding.mapSignals.onCreate(mapViewSavedInstanceState)

        viewModel.liveData.observe(this, Observer { signalMapResult ->
            when (signalMapResult) {
                is SignalsMapResult.ShowMessageOfType -> {
                    when (signalMapResult.type) {
                        MESSAGE_TYPE.ADD_SIGNAL -> {
                            showMessage(getString(R.string.txt_signal_added_successfully))
                        }
                    }
                }

                is SignalsMapResult.ShowMessage -> {
                    showMessage(signalMapResult.message)
                }

                is SignalsMapResult.ShowProgress -> {
                    setProgressVisibility(signalMapResult.showProgress)
                }

                is SignalsMapResult.ShowError -> {
                    when (signalMapResult.errorType) {
                        ERROR_TYPE.NO_INTERNET -> {
                            showMessage(getString(R.string.txt_no_internet))
                        }
                        ERROR_TYPE.DESCRIPTION -> {
                            showMessage(getString(R.string.txt_description_required))
                        }
                    }
                }

                is SignalsMapResult.ShowSignalMarkerInfo -> {
                    val intent = Intent(context, SignalDetailsActivity::class.java)
                    intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, viewModel.mSignalMarkers[signalMapResult.marker.id]!!)
                    startActivityForResult(intent, REQUEST_SIGNAL_DETAILS)

                }

                is SignalsMapResult.OpenLoginScreen -> {
                    openLoginScreen()
                }

                is SignalsMapResult.HideKeyboard -> {
                    hideKeyboard()
                }

            }

        })

        setHasOptionsMenu(true)

        binding.viewSendSignal.setOnSignalPhotoClickListener(View.OnClickListener {
            hideKeyboard()
            showSendPhotoBottomSheet()
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.mapSignals.onStart()
        googleApiClient.connect()
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
        if (googleApiClient.isConnected) {
            viewModel.fusedLocationProviderClient.removeLocationUpdates(viewModel.locationCallback)
            googleApiClient.disconnect()
        }
    }

    override fun onDestroy() {
        binding.mapSignals.onDestroy()
        super.onDestroy()
        viewModel.clearLocationData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //This MUST be done before saving any of your own or your base class's variables
        val mapViewSaveState = Bundle(outState)
        binding.mapSignals.onSaveInstanceState(mapViewSaveState)
        outState.putBundle(MAP_VIEW_STATE, mapViewSaveState)
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
            viewModel.getSignals(false)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showMessage(message: String) {
        Snackbar.make(binding.fabAddSignal, message, Snackbar.LENGTH_LONG).show()
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun showSendPhotoBottomSheet() {
        val sendPhotoBottomSheet = SendPhotoBottomSheet()
        sendPhotoBottomSheet.setListener(object : SendPhotoBottomSheet.PhotoTypeSelectListener {
            override fun onPhotoTypeSelected(photoType: Long) {
                if (photoType == SendPhotoBottomSheet.PhotoType.CAMERA) {
                    //actionsListener
                    openCamera()
                } else if (photoType == SendPhotoBottomSheet.PhotoType.GALLERY) {
                    //actionsListener
                    openGallery()
                }
            }
        })



//        sendPhotoBottomSheet.setListener(object : SendPhotoBottomSheet.PhotoTypeSelectListener {
//            override fun onPhotoTypeSelected(photoType: Long) {
//                if (photoType == SendPhotoBottomSheet.PhotoType.CAMERA) {
//                    //actionsListener
//                    openCamera()
//                } else if (photoType == SendPhotoBottomSheet.PhotoType.GALLERY) {
//                    //actionsListener
//                    openGallery()
//                }
//            }
//        })
        sendPhotoBottomSheet.show(fragmentManager!!, SendPhotoBottomSheet.TAG)
    }

    override fun openCamera() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(activity, Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_FOR_CAMERA)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(context!!.packageManager) != null) {
                val timeStamp = SimpleDateFormat(DATE_TIME_FORMAT_VIEW, Locale.getDefault()).format(Date())
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
                //actionsListener
                viewModel.photoUri = Uri.fromFile(photoFile).path!!
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val takenPhotoUri = imageUtils.getPhotoFileUri(context, imageFileName)
                viewModel.photoUri = takenPhotoUri!!.path!!
            }
        }

        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                saveImageFromURI(data.data)
            } else {
                // DRY!!
                val photoFile = imageUtils.getFromMediaUri(context, context!!.contentResolver, data.data)
                if (photoFile != null) {
                    viewModel.photoUri = Uri.fromFile(photoFile).path!!
                }
            }

        }

        if (requestCode == REQUEST_SIGNAL_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {
                val signal = data!!.getParcelableExtra<Signal>("signal")
                if (signal != null) {
                    for (i in viewModel.signalsList!!.indices) {
                        val currentSignal = viewModel.signalsList!![i]
                        if (currentSignal.id == signal.id) {
                            viewModel.signalsList!!.removeAt(i)
                            viewModel.signalsList!!.add(signal)
                            viewModel.displaySignals(true)
                            break
                        }
                    }
                } else {
                    return
                }
            }
        }
    }

    override fun closeSignalsMapScreen() {
        if (activity != null) {
            activity!!.finish()
        }
    }

    override fun setProgressVisibility(visibility: Boolean) {
        if (optionsMenu != null) {
            val refreshItem = optionsMenu!!.findItem(R.id.menu_item_refresh)
            if (refreshItem != null) {
                if (visibility) {
                    refreshItem.setActionView(R.layout.toolbar_progress)
                    if (refreshItem.actionView != null) {
                        val progressBar = refreshItem.actionView.findViewById(R.id.toolbar_progress_bar) as ProgressBar
                        progressBar.indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                    }
                } else {
                    refreshItem.actionView = null
                }
            }
        }
    }

    override fun isActive(): Boolean {
        return isAdded
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSIONS_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.signalsGoogleMap!!.isMyLocationEnabled = true
                viewModel.setLastLocation()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_location_permissions_for_map, Toast.LENGTH_SHORT)
                        .show()
            }
            READ_EXTERNAL_STORAGE_FOR_CAMERA -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //actionsListener
                openCamera()
            } else {
                // Permission Denied
                Toast.makeText(context, R.string.txt_storage_permissions_for_camera, Toast.LENGTH_SHORT)
                        .show()
            }

            READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //actionsListener
                openGallery()
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
        //actionsListener
        if (viewModel.addSignalVisible == View.VISIBLE) {
            viewModel.addSignalVisible = View.INVISIBLE
        } else {
            closeSignalsMapScreen()
        }
    }


    //Action listener

    companion object {
        //saction listener
        val TAG = SignalsMapFragment::class.java.simpleName

        fun newInstance(): SignalsMapFragment {
            return SignalsMapFragment()
        }

        fun newInstance(focusedSignalId: String): SignalsMapFragment {
            val signalsMapFragment = SignalsMapFragment()
            val args = Bundle()
            args.putString(KEY_FOCUSED_SIGNAL_ID, focusedSignalId)
            signalsMapFragment.arguments = args
            return signalsMapFragment
        }
    }
}// Required empty public constructor