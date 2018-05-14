package com.example.pc.devicedisplay

import android.app.ActivityManager
import android.location.Location
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.provider.SyncStateContract
import android.util.Log;
import android.widget.Toast
import com.example.pc.devicedisplay.R.string.firebase_path
import com.example.pc.devicedisplay.R.string.transport_id

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.HashMap
import com.google.firebase.database.DataSnapshot



class DisplayActivity: AppCompatActivity(), OnMapReadyCallback {

    private val TAG = DisplayActivity::class.java.simpleName
    private val mMarkers = HashMap<String, Marker>()
    private lateinit var mMap: GoogleMap
    private lateinit var userList: MutableList<User>
    private  var latitude: String = ""
    private  var longitude: String =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        userList = mutableListOf()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("User")
        var latitudeRef = ref.child("user1").child("0")
        var cal: ActivityManager

        latitudeRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                latitude = p0?.child("latitude")?.value.toString()
                longitude = p0?.child("longitude")?.value.toString()
                userList.add(User(latitude.toDouble(), longitude.toDouble()))

            }

        })

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!;
        mMap.setMaxZoomPreference(16F);
        loginToFirebase();
    }

    private fun loginToFirebase() {
        val email = getString(R.string.firebase_email)
        val password = getString(R.string.firebase_password)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "firebase auth success")
                subscribeToUpdates()
            } else {
                Log.d(TAG, "firebase auth failed")
            }
        }
    }

    private fun subscribeToUpdates() {
        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("locations")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                setMarker(p0)
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

                setMarker(p0)
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        });
    }

    private fun setMarker(dataSnapshot: DataSnapshot?){

        val key: String = dataSnapshot!!.key
        val value: HashMap<*, *> = dataSnapshot.getValue() as HashMap<*, *>
        val location = LatLng(userList.get(0).lat, userList.get(0).long)
        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, mMap.addMarker(MarkerOptions().title(key).position(location)))
        } else {
            mMarkers.get(key)!!.setPosition(location);
        }
        val builder = LatLngBounds.Builder()
        for (marker in mMarkers.values) {
            builder.include(marker.getPosition())
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300))

    }




}
