package com.lonaslee.formattedtelemetry

import org.junit.Test
import kotlin.system.measureNanoTime

class FormattedLineBuilderTest {
    @Test
    fun styles() {
        val lb = FormattedLineBuilder().magenta()
            .purple()
            .add("hello ")
            .bold()
            .add("world")
            .add(": still bold.")
            .nl()
            .unbold()
            .add("supersubs: ")
            .superscript()
            .add("super!!!")
            .subscript()
            .add("sub!!!!!")
            .unsubscript()
            .add(" regular text")
            .nl()
            .bold()
            .italicize()
            .underline()
            .strike()
            .add("WOWZERS THE EMPHASIS")
            .clearStyles()
            .add(" and back to normal")
            .nl()
            .big()
            .add("BIG BOI")
            .nl()
            .small()
            .add("Smol boi")
            .unsmall()
            .add(" the norm")
        println(lb.toString())
    }

    @Test
    fun benchmark() {
        val time = measureNanoTime {
            slider()
            progressBar()
            colors()
        }
        println(1 / (time / 1e9f))
    }

    @Test
    fun slider() {
        val lb = FormattedLineBuilder().startSlider(-90.0, 90.0, -50.0)
            .red()
            .magenta()
            .pink()
            .add("<br>")
            .startSlider(-90.0, 90.0, 50.0)
            .red()
            .magenta()
            .pink()
            .add("<br>")
            .startSlider(0.0, 500.0, 500.0)
            .red()
            .magenta()
            .pink()
            .add("<br>")
            .startSlider(0.0, 500.0, 0.0)
            .red()
            .magenta()
            .pink()
        println(lb.toString())
    }

    @Test
    fun progressBar() {
        val lb = FormattedLineBuilder().startProgressBar(-90.0, 90.0, 50.0)
            .blue()
            .cyan()
            .green()
            .add("<br>")
            .startProgressBar(-90.0, 90.0, -90.0)
            .blue()
            .cyan()
            .green()
            .add("<br>")
            .startProgressBar(-90.0, 90.0, 90.0)
            .blue()
            .cyan()
            .green()
            .add("<br>")
        println(lb.toString())
    }

    @Test
    fun colors() {
        val lb = FormattedLineBuilder().red()
            .add("red ")
            .yellow()
            .add("yellow ")
            .blue()
            .add("blue ")
            .orange()
            .add("orange ")
            .green()
            .add("green ")
            .violet()
            .add("violet ")
            .lime()
            .add("lime ")
            .cyan()
            .add("cyan ")
            .purple()
            .add("purple ")
            .magenta()
            .add("magenta ")
            .pink()
            .add("pink ")
            .lightGray()
            .add("lightgray ")
            .darkGray()
            .add("darkgray ")
            .black()
            .add("black ")
            .white()
            .add("white ")
            .add("<br>")
        println(lb.toString())
    }
}
