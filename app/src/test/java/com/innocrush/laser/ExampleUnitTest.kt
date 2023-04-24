package com.innocrush.laser

import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        decimalToHexV1(0, 1)
        decimalToHexV2(1000)
    }

    fun decimalToHexV1(value: Int?, bytePercision: Int): String? {
        val sb = StringBuilder()
        sb.append(Integer.toHexString(value!!))
        while (sb.length < bytePercision * 2) {
            sb.insert(0, '0') // pad with leading zero
        }
        val l = sb.length // total string length before spaces
        val r = l / 2 //num of rquired iterations
        for (i in 1 until r) {
            val x = l - 2 * i //space postion
            sb.insert(x, ' ')
        }
        println(sb.toString().uppercase(Locale.getDefault()).uppercase(Locale.getDefault()).toByteArray(
            StandardCharsets.US_ASCII).contentToString())
        return sb.toString().uppercase(Locale.getDefault())
    }


    private val sizeOfIntInHalfBytes = 4
    private val numberOfBitsInAHalfByte = 4
    private val halfByte = 0x0F
    private val hexDigits = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    )
    fun decimalToHexV2(dec: Int): String? {
        var dec = dec
        val hexBuilder: java.lang.StringBuilder = java.lang.StringBuilder(sizeOfIntInHalfBytes)
        hexBuilder.setLength(sizeOfIntInHalfBytes)
        for (i in sizeOfIntInHalfBytes - 1 downTo 0) {
            val j = dec and halfByte
            hexBuilder.setCharAt(i, hexDigits.get(j))
            dec = dec shr numberOfBitsInAHalfByte
        }
        println(hexBuilder.toString().uppercase(Locale.getDefault()).toByteArray(StandardCharsets.US_ASCII).contentToString())
        return hexBuilder.toString()
    }


}