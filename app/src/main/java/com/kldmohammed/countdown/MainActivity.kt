package com.kldmohammed.countdown

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.kldmohammed.tb.TimerButton


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val timerButton = findViewById<TimerButton>(R.id.timer_button)


        timerButton.setOnClickListener {
            Toast.makeText(applicationContext, "timer is about to start", Toast.LENGTH_LONG).show()
            Thread.sleep(500)

            timerButton.setDuration(10000L)

            //  Start the animation
            timerButton.startAnimation()

//  Stop the animation
            // timerButton.st()

//  Reset the animation
            //  timerButton.reset()

            //     timerButton.end()

            Toast.makeText(applicationContext, "timer is about to start", Toast.LENGTH_LONG).show()
        }


    }
}
