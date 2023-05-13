package com.lonaslee.formattedtelemetry

import android.text.Html
import org.firstinspires.ftc.robotcore.external.Telemetry
import kotlin.math.roundToInt

/**
 * Builds a telemetry line with colored text and other elements. The telemetry object needs
 * to be initialized with the [FormattedLineBuilder.initTelemetry] method.
 */
class FormattedLineBuilder {
    private val line = StringBuilder()
    private var curClr: String = ""
    private var spanning = false

    private val colors = mutableListOf<String>()
    private var numclrs = 0
    private var adder: ((MutableList<String>) -> Unit)? = null

    /**
     * Add an object's string representation to the line being built, applying html
     * escapes.
     */
    fun add(obj: Any?) = apply {
        if (adder != null) throw FormattedLineBuilderException("Adding elements during entry type.\n$line")
        rawAdd(Html.escapeHtml(obj.toString()))
    }

    /**
     * Add without escaping html characters.
     */
    fun rawAdd(obj: Any?) = apply {
        if (adder != null) throw FormattedLineBuilderException("Adding raw elements during entry type.\n$line")
        line.append(obj)
    }

    /**
     * Switch the current text color to a new color.
     *
     * @param clr Name or hex code of the color. Some of them don't work on phone
     * displays.
     * @return this
     */
    fun clr(clr: String) = apply {
        val newclr = try {
            clr.removePrefix("0x").removePrefix("#").also { it.toLong(16) }
        } catch (e: NumberFormatException) {
            clr
        }

        if (numclrs != 0) {
            colors.add(newclr)
            if (colors.size == numclrs) {
                numclrs = 0
                adder!!(colors)
                colors.clear()
                adder = null
            }
        } else {
            endIfSpanning()
            spanning = true
            curClr = newclr
            rawAdd("<span style=\"color:$curClr\">")
        }
    }

    /**
     * End any current formatting. The building methods will
     * automatically take care of this.
     */
    fun end() = apply {
        if (!spanning) throw FormattedLineBuilderException("End called with no current span.")
        spanning = false
        rawAdd("</span>")
    }

    private fun endIfSpanning() = apply {
        if (spanning) end()
    }

    /**
     * Add formatted data, in the form of "label : data."
     * Follow the call to this building method with two color methods, which will be
     * used for the label and the data, respectively.
     * <pre>
     * new FormattedLineBuilder()
     * .startData("label", "data")
     * .[FormattedLineBuilder.red]()
     * .[FormattedLineBuilder.clr]("00FF00")</pre>
     */
    fun startData(label: String, data: Any) = apply {
        if (adder != null) throw FormattedLineBuilderException(
            "Starting data entry during another entry type.\nadder : $adder\n$line"
        )
        numclrs = 2
        val prevClr = curClr
        adder = { clrs ->
            for (i in clrs.indices) if (clrs[i].isBlank()) clrs[i] = "white"
            clr(clrs[0])
            add(label)
            black()
            add(" : ")
            clr(clrs[1])
            add(data.toString())
            clr(prevClr)
        }
    }

    /**
     * Add a slider, ranging from the given min and max, with the bar at the given current position.
     * Follow the call to this building method with three color methods, which will be used for
     * the min/max labels, the bar, and the slider range, respectively.
     * <pre>
     * new FormattedLineBuilder()
     * .startSlider(0, 100, 30)
     * .[FormattedLineBuilder.blue]()
     * .[FormattedLineBuilder.clr]("00FFFF")
     * .[FormattedLineBuilder.red]()</pre>
     */
    fun startSlider(min: Double, max: Double, cur: Double) = apply {
        if (adder != null) throw FormattedLineBuilderException(
            "Starting slider entry during another entry type.\n$line"
        )
        numclrs = 3
        val prevClr = curClr
        val percent = cur / (max + 1e-6 - min)
        val idx = (20 * percent).roundToInt()

        adder = { clrs ->
            for (i in clrs.indices) if (clrs[i].isBlank()) clrs[i] = "white"
            clr(clrs[0])
            add(min)
            clr(clrs[2])
            add(" [")
            repeat("━", idx)
            clr(clrs[1])
            add("█")
            clr(clrs[2])
            repeat("━", 20 - idx - 1)
            add("] ")
            clr(clrs[0])
            add(max)
            clr(clrs[1])
            add(" | ")
            add(cur)
            add(" ")
            clr(prevClr)
        }
    }

    /**
     * Similar to [FormattedLineBuilder.startSlider], except
     * it is a progress bar. Follow calls to this method with two color methods, which will be
     * used for completed and uncompleted parts of the bar, respectively.
     */
    fun startProgressBar(min: Double, max: Double, cur: Double) = apply {
        if (adder != null) throw FormattedLineBuilderException(
            "Starting progress bar entry during another entry type.\n$line"
        )
        numclrs = 3
        val prevClr = curClr
        val percent = cur / (max + 1e-6 - min)
        val idx = (20 * percent).roundToInt()

        adder = { clrs ->
            for (i in clrs.indices) if (clrs[i].isBlank()) clrs[i] = "white"
            clr(clrs[1])
            add("[")
            clr(clrs[0])
            repeat("█", idx + 1)
            clr(clrs[1])
            repeat("━", 20 - idx - 1)
            add("] ")
            clr(clrs[0])
            add((percent * 100).toInt())
            add("% ")
            clr(prevClr)
        }
    }

    /**
     * Add a spinner, automatically determining its current phase based on
     * [System.currentTimeMillis].
     */
    fun spinner(phases: Array<String>, phaseLengthMillis: Int, offset: Int) =
        add(phases[((System.currentTimeMillis() / phaseLengthMillis + offset) % phases.size).toInt()])

    /**
     * Default spinner with phase length of 100ms and offset of 0.
     */
    @JvmOverloads
    fun spinner(phaseLengthMillis: Int = 100, offset: Int = 0) = spinner(
        arrayOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"), phaseLengthMillis, offset
    )

    @JvmOverloads
    fun spinnerBig(phaseLengthMillis: Int = 100, offset: Int = 0) = spinner(
        arrayOf(
            "⡀⠀",
            "⠄⠀",
            "⢂⠀",
            "⡂⠀",
            "⠅⠀",
            "⢃⠀",
            "⡃⠀",
            "⠍⠀",
            "⢋⠀",
            "⡋⠀",
            "⠍⠁",
            "⢋⠁",
            "⡋⠁",
            "⠍⠉",
            "⠋⠉",
            "⠋⠉",
            "⠉⠙",
            "⠉⠙",
            "⠉⠩",
            "⠈⢙",
            "⠈⡙",
            "⢈⠩",
            "⡀⢙",
            "⠄⡙",
            "⢂⠩",
            "⡂⢘",
            "⠅⡘",
            "⢃⠨",
            "⡃⢐",
            "⠍⡐",
            "⢋⠠",
            "⡋⢀",
            "⠍⡁",
            "⢋⠁",
            "⡋⠁",
            "⠍⠉",
            "⠋⠉",
            "⠋⠉",
            "⠉⠙",
            "⠉⠙",
            "⠉⠩",
            "⠈⢙",
            "⠈⡙",
            "⠈⠩",
            "⠀⢙",
            "⠀⡙",
            "⠀⠩",
            "⠀⢘",
            "⠀⡘",
            "⠀⠨",
            "⠀⢐",
            "⠀⡐",
            "⠀⠠",
            "⠀⢀",
            "⠀⡀"
        ), phaseLengthMillis, offset
    )

    @JvmOverloads
    fun spinnerLine(phaseLengthMillis: Int = 100, offset: Int = 0) =
        spinner(arrayOf("-", "\\", "|", "/"), phaseLengthMillis, offset)

    @JvmOverloads
    fun spinnerNoise(phaseLengthMillis: Int = 100, offset: Int = 0) =
        spinner(arrayOf("▓", "▒", "░"), phaseLengthMillis, offset)

    /**
     * Repeat the given string a number of times.
     */
    fun repeat(obj: Any, times: Int): FormattedLineBuilder {
        repeat(times) { add(obj) }
        return this
    }

    /**
     * Add a newline.
     */
    fun nl() = add("\n")

    /* --------------- colors --------------- */
    fun red() = clr("red")

    fun yellow() = clr("yellow")

    fun blue() = clr("blue")

    fun orange() = clr("EA5D00")

    fun green() = clr("green")

    fun violet() = clr("violet")

    fun lime() = clr("69FF00")

    fun cyan() = clr("00E5E5")

    fun purple() = clr("5A00E2")

    fun magenta() = clr("BE00FF")

    fun pink() = clr("FF3ADC")

    fun lightGray() = clr("808080")

    fun darkGray() = clr("404040")

    fun black() = clr("000000")

    fun white() = clr("FFFFFF")

    /**
     * Get the formatted line that was built.
     */
    override fun toString(): String {
        endIfSpanning()
        return line.toString()
    }

    class FormattedLineBuilderException(msg: String?) : RuntimeException(msg)

    companion object {
        /**
         * Set up a telemetry object to work with html formatting. This method needs to be called
         * for formatting to work.
         */
        @JvmStatic
        @JvmOverloads
        fun initTelemetry(telemetry: Telemetry, msRefreshRate: Int = 250) = telemetry.apply {
            setDisplayFormat(Telemetry.DisplayFormat.HTML)
            msTransmissionInterval = msRefreshRate
            update()
        }
    }
}
