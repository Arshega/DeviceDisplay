package com.example.pc.devicedisplay

import android.location.Location
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*

class DisplayActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = DisplayActivity::class.java.simpleName
    private val mMarkers = HashMap<String, Marker>()
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path))
        ref.addChildEventListener(object : ChildEventListener{
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
        val lat: Double = (value.get("latitude").toString()).toDouble()
        val lng: Double = (value.get("longitude").toString()).toDouble()
        val location = LatLng(lat,lng)
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
