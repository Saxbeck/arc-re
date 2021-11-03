package com.example.arcoresceneviewer

import android.content.Context
import android.graphics.Color.LTGRAY
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import com.example.arcoresceneviewer.databinding.FragmentSceneBinding
import com.google.ar.sceneform.rendering.Color
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.lang.Math.*


private val quaternion = Quaternion()
private val rotateVector = Vector3.up()

private fun getRotationQuaternion(deltaYAxisAngle: Float): Quaternion {
    val lastDeltaYAxisAngle = deltaYAxisAngle
    return quaternion.apply {
        val arc = toRadians(deltaYAxisAngle.toDouble())
        val axis = kotlin.math.sin(arc / 2.0)
        x = (rotateVector.x * axis).toFloat()
        y = (rotateVector.y * axis).toFloat()
        z = (rotateVector.z * axis).toFloat()
        w = kotlin.math.cos(arc / 2.0).toFloat()
        normalize()
    }
}

abstract class ModelProperty(name: String) : FloatPropertyCompat<Node>(name)

private val rotationProperty: ModelProperty = object : ModelProperty("rotation") {
    override fun setValue(card: Node, value: Float) {
        //implement setValue
    }
    override fun getValue(card: Node): Float = card.localRotation.y
}

//View starts here

class SceneFragment : Fragment() {
    private var _binding: FragmentSceneBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sceneView: SceneView
    private lateinit var centerNode:AnchorNode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSceneBinding.inflate(inflater, container, false)

        sceneView = binding.SceneView
        sceneView.renderer!!.setClearColor(Color(LTGRAY))

        loadModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.SceneView.resume()
        }   catch (e:CameraNotAvailableException){
            e.message
        }
    }

    override fun onPause() {
        super.onPause()
        binding.SceneView.pause()
    }


    // load models
    private fun loadModel(){
        ModelRenderable.builder()
            .setSource(context, RenderableSource.builder()
                    .setSource(context,
                        Uri.parse("genshin_impact_ayaka/scene.gltf"),
                        RenderableSource.SourceType.GLTF2).setRecenterMode(RenderableSource.RecenterMode.CENTER).build())
            .build()
            .thenAccept { addToScene(it) }
            .exceptionally { _ ->
                val toast = Toast.makeText(context, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.show()
                return@exceptionally null
            }
    }

    private fun addToScene(renderable: ModelRenderable){
        val camera: Camera = sceneView.scene.camera
            camera.localPosition = Vector3(0.0f, 0.50f, 1.05f)
            camera.localRotation = Quaternion.axisAngle(Vector3.left(), 5.5f)

        centerNode = AnchorNode()
        with(centerNode){
            centerNode.setParent(sceneView.scene)
            centerNode.renderable = renderable
        }
        centerNode.localRotation = Quaternion.axisAngle(Vector3.left(), -15.0f)
    }
}

