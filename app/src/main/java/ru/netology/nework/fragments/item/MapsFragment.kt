package ru.netology.nework.fragments.item

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapsBinding
import ru.netology.nework.util.AppConst

class MapsFragment : Fragment(), UserLocationObjectListener {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    // Используем nullable вместо lateinit
    private var mapView: MapView? = null
    private var userLocation: UserLocationLayer? = null
    private var placeMark: PlacemarkMapObject? = null

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        mapView = binding.map
        MapKitFactory.initialize(requireContext())

        mapView?.let { map ->
            userLocation = MapKitFactory.getInstance()
                .createUserLocationLayer(map.mapWindow)
                .apply {
                    isVisible = true
                    setObjectListener(this@MapsFragment)
                }

            val imageProvider = ImageProvider.fromResource(
                requireContext(),
                R.drawable.ic_location_on_24
            )

            val inputListener = object : InputListener {
                override fun onMapTap(map: Map, point: Point) = Unit

                override fun onMapLongTap(map: Map, point: Point) {
                    if (placeMark == null) {
                        placeMark = binding.map.mapWindow.map.mapObjects.addPlacemark()
                    }
                    placeMark?.apply {
                        geometry = point
                        setIcon(imageProvider)
                        isVisible = true
                    }
                }
            }

            map.mapWindow.map.addInputListener(inputListener)
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save -> {
                    setFragmentResult(
                        AppConst.MAPS_FRAGMENT_RESULT,
                        bundleOf(AppConst.MAP_POINT to gson.toJson(placeMark?.geometry))
                    )
                    findNavController().navigateUp()
                    true
                }
                else -> false
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Зануляем все ссылки на View-объекты
        userLocation?.setObjectListener(null)
        userLocation = null
        placeMark = null
        mapView = null
        _binding = null
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        mapView?.let { map ->
            userLocation?.setAnchor(
                PointF((map.width * 0.5).toFloat(), (map.height * 0.5).toFloat()),
                PointF((map.width * 0.5).toFloat(), (map.height * 0.83).toFloat())
            )
            map.mapWindow.map.move(
                CameraPosition(userLocationView.arrow.geometry, 17f, 0f, 0f)
            )
        }
    }

    override fun onObjectRemoved(p0: UserLocationView) = Unit

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) = Unit
}