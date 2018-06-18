package za.co.riggaroo.motioncamera

import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.TimeUnit

interface Music {

    class Duration (private val beatPercent: Double) {

        internal fun asPeriod(): Long {
            return (TEMPO * beatPercent).toLong()
        }

        companion object {
            private val TEMPO = TimeUnit.MILLISECONDS.toMillis(1500)
        }

    }

    class Frequency public constructor(internal val frequency: Double)

    interface Note {
        val period: Long

        val frequency: Double

        val isRest: Boolean
    }

    class FrequencyNote (private val duration: Duration, private val frequen: Frequency) : Note {

        override val period: Long
            get() = duration.asPeriod()

        override val frequency: Double
            get() = frequen.frequency

        override val isRest: Boolean
            get() = false

    }

    class RestNote (private val duration: Duration) : Note {

        override val period: Long
            get() = duration.asPeriod()

        override val frequency: Double
            get() = throw IllegalStateException("Rest notes do not have a frequency")

        override val isRest: Boolean
            get() = true
    }

    companion object {
        val EIGTH = Duration(0.125)
        val QUARTER = Duration(0.25)
        val HALF = Duration(0.5)
        val WHOLE = Duration(1.0)

        val C4 = Frequency(261.63)
        val E4 = Frequency(329.63)
        val F4 = Frequency(349.23)
        val G4 = Frequency(392.00)
        val A4 = Frequency(440.00)
        val A4_SHARP = Frequency(466.16)

        val E4_Q: Note = FrequencyNote(QUARTER, E4)
        val F4_Q: Note = FrequencyNote(QUARTER, F4)
        val G4_Q: Note = FrequencyNote(QUARTER, G4)
        val G4_H: Note = FrequencyNote(HALF, G4)
        val A4_Q: Note = FrequencyNote(QUARTER, A4)
        val A4_Q_SHARP: Note = FrequencyNote(QUARTER, A4_SHARP)
        val A4_E: Note = FrequencyNote(EIGTH, A4)
        val A4_W: Note = FrequencyNote(WHOLE, A4)
        val C4_Q: Note = FrequencyNote(QUARTER, C4)
        val R_E: Note = RestNote(EIGTH)
        val R_H: Note = RestNote(HALF)

        /**
         * http://www.musicnotes.com/sheetmusic/mtd.asp?ppn=MN0145355
         */
        val POKEMON_ANIME_THEME: List<Note> = ArrayList(Arrays.asList(
                R_E, A4_E, A4_E, A4_E, A4_Q, A4_Q,
                G4_Q, E4_Q, C4_Q, C4_Q,
                A4_Q, A4_Q, G4_Q, F4_Q,
                G4_H, R_H,
                F4_Q, A4_Q_SHARP, A4_Q_SHARP, A4_Q_SHARP,
                A4_Q, G4_Q, F4_Q, F4_Q,
                A4_Q, A4_Q, G4_Q, F4_Q,
                A4_W
        ))
    }
}