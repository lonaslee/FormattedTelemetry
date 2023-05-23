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
    private var curClr = ""
    private var spanning = false

    private val colors = mutableListOf<String>()
    private var numclrs = 0
    private var adder: ((MutableList<String>) -> Unit)? = null

    /**
     * Add an object's string representation to the line being built, applying html
     * escapes.
     */
    fun add(obj: Any?) = apply {
        if (numclrs != 0) throw FormattedLineBuilderException("Adding objects during entry type.\n$line")
        rawAdd(Html.escapeHtml(obj.toString()))
    }

    /**
     * [add] without escaping html characters.
     */
    fun rawAdd(obj: Any?) = apply {
        if (numclrs != 0) throw FormattedLineBuilderException("Raw adding objects during entry type.\n$line")
        line.append(obj)
    }

    /**
     * Switch the current text color to a new color, given its name or hex code.
     * Not all named colors are supported.
     */
    fun clr(clr: String) = apply {
        val newclr = clr.removePrefix("0x").removePrefix("#").let {
            try {
                it.toLong(16).toString()
            } catch (e: NumberFormatException) {
                it
            }
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
     * ```
     * new FormattedLineBuilder()
     * .startData("label", "data")
     * .red()
     * .clr("00FF00")
     */
    fun startData(label: String, data: Any) = apply {
        if (numclrs != 0) throw FormattedLineBuilderException(
            "Starting data entry during another entry type.\nadder : $adder\n$line"
        )
        numclrs = 2
        val prevClr = curClr
        adder = { clrs ->
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
     * ```
     * new FormattedLineBuilder()
     * .startSlider(0, 100, 30)
     * .blue()
     * .clr("00FFFF")
     * .red()
     */
    fun startSlider(min: Double, max: Double, cur: Double) = apply {
        if (numclrs != 0) throw FormattedLineBuilderException("Starting slider entry during another entry type.\n$line")
        if (min >= max) throw FormattedLineBuilderException("Invalid slider range: $min-$max.\n$line")

        numclrs = 3
        val prevClr = curClr
        val percent = (cur + (0 - min)) / (max + (0 - min))
        val idx = (20 * percent).roundToInt()

        adder = { clrs ->
            clr(clrs[0])
            add(min)
            clr(clrs[2])
            add(" 【")
            repeat("━", idx)
            clr(clrs[1])
            add("█")
            clr(clrs[2])
            repeat("━", 20 - idx)
            add("】 ")
            clr(clrs[0])
            add(max)
            clr(clrs[1])
            add(" | ")
            add(formatDecimal(cur))
            add(" ")
            clr(prevClr)
        }
    }

    /**
     * Similar to [startSlider], except
     * it is a progress bar. Follow calls to this method with two color methods, which will be
     * used for completed and uncompleted parts of the bar, respectively.
     */
    fun startProgressBar(min: Double, max: Double, cur: Double) = apply {
        if (numclrs != 0) throw FormattedLineBuilderException("Starting progress bar entry during another entry type.\n$line")
        if (min >= max) throw FormattedLineBuilderException("Invalid progress bar range: $min-$max.\n$line")

        numclrs = 2
        val prevClr = curClr
        val percent = (cur + (0 - min)) / (max + (0 - min))
        val idx = (20 * percent).roundToInt()

        adder = { clrs ->
            clr(clrs[1])
            add("【")
            clr(clrs[0])
            repeat("█", idx)
            clr(clrs[1])
            repeat("░", 20 - idx)
            add("】 ")
            clr(clrs[0])
            add(formatDecimal(percent * 100))
            add("% ")
            clr(prevClr)
        }

    }

    /**
     * Add a spinner, automatically determining its current phase based on
     * [System.currentTimeMillis]. The length of each phase is limited by the telemetry's
     * update speed, which can be changed with the [Telemetry.setMsTransmissionInterval]
     * method. The [replacement] parameter will be put in place of the spinner if it is not null,
     * which is useful if the spinner is used to indicate something in progress.
     */
    @JvmOverloads
    fun spinner(
        phases: Array<String>, phaseLengthMillis: Int, offset: Int, replacement: String? = null
    ) = add(
        replacement
            ?: phases[((System.currentTimeMillis() / phaseLengthMillis + offset) % phases.size).toInt()]
    )

    /**
     * Default spinner with phase length of 100ms and offset of 0.
     */
    @JvmOverloads
    fun spinner(phaseLengthMillis: Int = 100, offset: Int = 0, replacement: String? = null) =
        spinner(
            arrayOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"),
            phaseLengthMillis,
            offset,
            replacement
        )

    @JvmOverloads
    fun spinnerBig(phaseLengthMillis: Int = 100, offset: Int = 0, replacement: String? = null) =
        spinner(
            arrayOf(
                // @formatter:off
                "⡀⠀", "⠄⠀", "⢂⠀", "⡂⠀", "⠅⠀", "⢃⠀", "⡃⠀", "⠍⠀", "⢋⠀", "⡋⠀", "⠍⠁", "⢋⠁", "⡋⠁", "⠍⠉",
                "⠋⠉", "⠋⠉", "⠉⠙", "⠉⠙", "⠉⠩", "⠈⢙", "⠈⡙", "⢈⠩", "⡀⢙", "⠄⡙", "⢂⠩", "⡂⢘", "⠅⡘", "⢃⠨",
                "⡃⢐", "⠍⡐", "⢋⠠", "⡋⢀", "⠍⡁", "⢋⠁", "⡋⠁", "⠍⠉", "⠋⠉", "⠋⠉", "⠉⠙", "⠉⠙", "⠉⠩",
                "⠈⢙", "⠈⡙", "⠈⠩", "⠀⢙", "⠀⡙", "⠀⠩", "⠀⢘", "⠀⡘", "⠀⠨", "⠀⢐", "⠀⡐", "⠀⠠", "⠀⢀", "⠀⡀"
                // @formatter:on
            ), phaseLengthMillis, offset, replacement
        )

    @JvmOverloads
    fun spinnerLine(phaseLengthMillis: Int = 100, offset: Int = 0, replacement: String? = null) =
        spinner(arrayOf("-", "\\", "|", "/"), phaseLengthMillis, offset, replacement)

    @JvmOverloads
    fun spinnerNoise(phaseLengthMillis: Int = 100, offset: Int = 0, replacement: String? = null) =
        spinner(arrayOf("▓", "▒", "░"), phaseLengthMillis, offset, replacement)

    /**
     * Repeat the given string a number of times.
     */
    fun repeat(obj: Any, times: Int) = apply {
        add(obj.toString().repeat(times))
    }

    /**
     * Add a newline. On some devices this might not work, and in that case
     * simply use [add] with the newline character "\n".
     */
    fun nl() = rawAdd("<br>")

    /* --------------- styles --------------- */

    private var bolded = false
    private var italicized = false
    private var underlined = false
    private var striked = false

    fun bold() = rawAdd("<b>").also { bolded = true }

    fun unbold() = rawAdd("</b>").also { bolded = false }

    fun italicize() = rawAdd("<i>").also { italicized = true }

    fun unitalicize() = rawAdd("</i>").also { italicized = false }

    fun underline() = rawAdd("<u>").also { underlined = true }

    fun ununderline() = rawAdd("</u>").also { underlined = false }

    fun strike() = rawAdd("<s>").also { striked = true }

    fun unstrike() = rawAdd("</s>").also { striked = false }

    private var size = 0

    fun big() = apply {
        if (size == -1) unsmall()
        rawAdd("<big>")
        size = 1
    }

    fun unbig() = apply {
        rawAdd("</big>")
        size = 0
    }

    fun small() = apply {
        if (size == 1) unbig()
        rawAdd("<small>")
        size = -1
    }

    fun unsmall() = apply {
        rawAdd("</small>")
        size = 0
    }

    private var script = 0

    fun superscript() = apply {
        if (script == -1) unsubscript()
        rawAdd("<sup>")
        script = 1
    }

    fun unsuperscript() = apply {
        rawAdd("</sup>")
        script = 0
    }

    fun subscript() = apply {
        if (script == 1) unsuperscript()
        rawAdd("<sub>")
        script = -1
    }

    fun unsubscript() = apply {
        rawAdd("</sub>")
        script = 0
    }

    /**
     * Clear bold, italics, underline, strikethrough, big/small, and super/subscripts, if
     * any are currently toggled.
     */
    fun clearStyles() = apply {
        if (bolded) unbold()
        if (italicized) unitalicize()
        if (underlined) ununderline()
        if (striked) unstrike()
        if (size == 1) unbig()
        if (size == -1) unsmall()
        if (script == 1) unsuperscript()
        if (script == -1) unsubscript()
    }

    /* --------------- colors --------------- */
    fun red() = clr("red")

    fun yellow() = clr("yellow")

    fun blue() = clr("blue")

    fun orange() = clr("FFA500")

    fun green() = clr("green")

    fun violet() = clr("7F00FF")

    fun lime() = clr("32CD32")

    fun cyan() = clr("cyan")

    fun purple() = clr("A020F0")

    fun magenta() = clr("FF00FF")

    fun pink() = clr("FFC0CB")

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

    class FormattedLineBuilderException(msg: String) : RuntimeException(msg)

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

        @JvmStatic
        fun formatDecimal(decimal: Double) = "%.2f".format(decimal)
    }
}
