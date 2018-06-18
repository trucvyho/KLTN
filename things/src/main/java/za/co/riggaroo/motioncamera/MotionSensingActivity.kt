package za.co.riggaroo.motioncamera

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import com.google.android.gms.tasks.Task
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.Pwm
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import za.co.riggaroo.motioncamera.camera.CustomCamera
import com.google.firebase.auth.FirebaseUser

import java.io.IOException
import java.util.ArrayDeque

import za.co.riggaroo.motioncamera.Music

class MotionSensingActivity : AppCompatActivity(), MotionSensor.MotionListener {

    private lateinit var ledMotionIndicatorGpio: Gpio
    // private lateinit var ledArmedIndicatorGpio: Gpio
    private lateinit var camera: CustomCamera
    private lateinit var motionImageView: ImageView
    private lateinit var buttonArmSystem: Button
    private lateinit var motionViewModel: MotionSensingViewModel
    private lateinit var motionSensor: MotionSensor

    //buzzer
    private var bus: Pwm? = null
    private var buzzerSongHandler: Handler? = null

    private val playSong = object : Runnable {
        override fun run() {
            if (SONG.isEmpty()) {
                return
            }

            val note = SONG.poll()

            if (note.isRest) {
                SystemClock.sleep(note.period)
            } else {
                try {
                    bus!!.setPwmFrequencyHz(note.frequency)
                    bus!!.setEnabled(true)
                    SystemClock.sleep(note.period)
                    bus!!.setEnabled(false)
                } catch (e: IOException) {
                    throw IllegalStateException("$BUZZER_PIN bus cannot play note.", e)
                }

            }
            buzzerSongHandler!!.post(this)
        }
    }

    //firebase
    private var mAuth: FirebaseAuth? = null

    //Auth

    private fun signInAnonymously() {
        //mAuth?.signInAnonymously()?.addOnSuccessListener(this) { Log.e(ACT_TAG, "Sign in Successfully!!") }
        //      ?.addOnFailureListener(this) { exception -> Log.e(ACT_TAG, "signInAnonymously:FAILURE", exception)
        mAuth?.signInAnonymously()
                ?.addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        val user = mAuth?.currentUser
                        Toast.makeText(this, "Authentication successful. user id ${user?.uid}",
                                Toast.LENGTH_SHORT).show()

                    } else {
                        // If the sign in fails displays a message to the user.
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()

                    }
                })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion_sensing)
        setTitle(R.string.app_name)
        setupViewModel()
        setupCamera()
        setupActuators()
        setupSensors()
        setupUIElements()
        // [START initialize_auth]
        val mAuth = FirebaseAuth.getInstance()
        //buzzer
        val service = PeripheralManager.getInstance()
        try {
            bus = service.openPwm(BUZZER_PIN)
        } catch (e: IOException) {
            throw IllegalStateException("$BUZZER_PIN bus cannot be opened.", e)
        }
        try {
            bus?.setPwmDutyCycle(10.0)
        } catch (e: IOException) {
            throw IllegalStateException("$BUZZER_PIN bus cannot be configured.", e)
        }


        val handlerThread = HandlerThread("BackgroundThread")
        handlerThread.start()
        buzzerSongHandler = Handler(handlerThread.looper)

    }

    override fun onStart() {
        super.onStart()
        val user = mAuth?.currentUser
        if (user != null) {
            Log.e(ACT_TAG, "There is no user")
        } else {
            signInAnonymously()
        }

    }

    override fun onComplete(task: Task<AuthResult>) = if (task.isSuccessful) {
        //log in success
        updateUi(mAuth?.currentUser!!)
    } else {
        //log in failed
        Toast.makeText(this, "Unable to sign in", Toast.LENGTH_LONG).show()
    }

    private fun updateUi(user: FirebaseUser) {
        Toast.makeText(this, "Successfully logged in, updating UI", Toast.LENGTH_LONG).show()
        //Do some updating of the ui based on the user that is logged in
    }

    private fun setupViewModel() {
        Log.e("setupViewModel", "setupViewModel");
        motionViewModel = ViewModelProviders.of(this).get(MotionSensingViewModel::class.java)
    }

    private fun setupSensors() {
        motionSensor = MotionSensor(this, MOTION_SENSOR_GPIO_PIN)
        lifecycle.addObserver(motionSensor)
    }


    private fun setupActuators() {
        val peripheralManagerService = PeripheralManager.getInstance()
        try {
            ledMotionIndicatorGpio = peripheralManagerService.openGpio(LED_GPIO_PIN)
            ledMotionIndicatorGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            //  ledArmedIndicatorGpio = peripheralManagerService.openGpio(LED_ARMED_INDICATOR_PIN)
            //  ledArmedIndicatorGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }catch (e:Exception){ e.printStackTrace()        }

    }

    override fun onDestroy() {
        try {
            bus!!.close()
        } catch (e: IOException) {
            Log.e("TUT", "${MotionSensingActivity.BUZZER_PIN} bus cannot be closed, you may experience errors on next launch.", e)
        }
        super.onDestroy()
        // ledArmedIndicatorGpio.close()
        ledMotionIndicatorGpio.close()
    }

    private fun setupUIElements() {
        //Firebase
        //  mAuth = FirebaseAuth.getInstance()
        motionImageView = findViewById(R.id.image_view_motion)

        buttonArmSystem = findViewById(R.id.button_arm_disarm)
        buttonArmSystem.setOnClickListener {
            motionViewModel.toggleSystemArmedStatus()
        }
        Log.e("setupUIElements", "setupUIElements")
        motionViewModel.armed.observe(this, Observer { armed ->
            armed?.let {

                buttonArmSystem.text = if (armed) {
                    getString(R.string.disarm_system)
                } else {
                    getString(R.string.arm_system)
                }
                Log.e("setupUIElements", armed.toString()+" setupUIElements")
                //       ledArmedIndicatorGpio.value = armed
            }

        })
    }

    private fun setupCamera() {
        camera = CustomCamera.getInstance()
        camera.initializeCamera(this, Handler(), imageAvailableListener)
    }

    private val imageAvailableListener = object : CustomCamera.ImageCapturedListener {
        override fun onImageCaptured(bitmap: Bitmap) {
            motionImageView.setImageBitmap(bitmap)
            motionViewModel.uploadMotionImage(bitmap)
        }
    }

    override fun onMotionDetected() {
        Log.d(ACT_TAG, "onMotionDetected")

        ledMotionIndicatorGpio.value = true
        camera.takePicture()
        SONG.addAll(Music.POKEMON_ANIME_THEME)
        buzzerSongHandler?.post(playSong)
    }


    override fun onMotionStopped() {
        Log.d(ACT_TAG, "onMotionStopped")
        ledMotionIndicatorGpio.value = false
    }
    override fun onStop() {
        buzzerSongHandler!!.removeCallbacks(playSong)
        super.onStop()
    }

    companion object {
        //    val LED_ARMED_INDICATOR_PIN: String = "BCM21"//GPIO6_IO15
        val ACT_TAG: String = "MotionSensingActivity"
        val LED_GPIO_PIN = "BCM21"//GPIO6_IO14
        val MOTION_SENSOR_GPIO_PIN = "BCM27" //GPIO2_IO03
        val BUZZER_PIN = "PWM1"//BCM13
        val SONG = ArrayDeque<Music.Note>()
    }
}

private operator fun Boolean.invoke() {

}
