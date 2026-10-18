package com.eried.eucplanet.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Real LK13709 Service Mode notifications and legacy WheelLog frame layouts. */
class VeteranLegacyFrameTest {
    private fun bytes(hex: String): ByteArray =
        hex.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private val firstHead = bytes("dc5a5c2025590000907200139072001300000e91")
    private val firstTail = bytes("0b8e000001f8024604520002ffdd0009")
    private val firstFrame = firstHead + firstTail

    private fun telemetry(adapter: VeteranAdapter, bytes: ByteArray) =
        adapter.onRawNotification(bytes).filterIsInstance<DecodeResult.Telemetry>().map { it.data }

    @Test fun `LK13709 split frame decodes actual measurements`() {
        val adapter = VeteranAdapter()
        adapter.notifyConnectingTo("LK13709")
        assertTrue(telemetry(adapter, firstHead).isEmpty())
        val data = telemetry(adapter, firstTail).single()
        assertEquals(95.61f, data.voltage, 0.01f)
        assertEquals(0f, data.speed, 0.01f)
        assertEquals(0f, data.phaseCurrent, 0.01f)
        assertEquals(37.29f, data.maxTemperature, 0.01f)
        assertEquals(1282.162f, data.tripDistance, 0.001f)
        assertEquals(1282.162f, data.totalDistance, 0.001f)
        assertEquals(50.4f, data.wheelAlarmSpeedKmh, 0.01f)
        assertEquals(58.2f, data.wheelMaxSpeedKmh, 0.01f)
        assertTrue(data.batteryPercent > 0)
        assertEquals(0, firstFrame[22].toInt())
        assertEquals(0, firstFrame[23].toInt())
        assertEquals(0, firstFrame[30].toInt())
    }

    @Test fun `moving LK13709 frames update speed then distance`() {
        val adapter = VeteranAdapter()
        adapter.notifyConnectingTo("LK13709")
        val moving = bytes("dc5a5c2025540018907200139072001300500e96" +
            "0e0f000001f8024604520002ffc70161")
        val later = bytes("dc5a5c2025530019907d0013907d0013003f0ea4" +
            "0e0f000001f8024604520002ffe700b4")
        val atStart = telemetry(adapter, firstFrame).single()
        val inMotion = telemetry(adapter, moving).single()
        val afterTravel = telemetry(adapter, later).single()
        assertEquals(2.4f, inMotion.speed, 0.01f)
        assertEquals(atStart.tripDistance, inMotion.tripDistance, 0.001f)
        assertEquals(1282.173f, afterTravel.tripDistance, 0.001f)
        assertEquals(1282.173f, afterTravel.totalDistance, 0.001f)
    }

    @Test fun `notifications can cross short frame boundaries`() {
        val adapter = VeteranAdapter()
        adapter.notifyConnectingTo("LK13709")
        // Service Mode log lines 129–134: 20 + 20 + 20 + 20 + 20 + 8 bytes.
        val chunks = listOf(
            "dc5a5c2025590000907200139072001300000e94",
            "0b84000001f8024604520002ffe30008dc5a5c20",
            "255900009072001390720013fffd0e940b830000",
            "01f8024604520002ffe60010dc5a5c2025590000",
            "907200139072001300000e940b83000001f80246",
            "04520002ffe30009"
        )
        assertEquals(listOf(0, 1, 0, 1, 0, 1),
            chunks.map { telemetry(adapter, bytes(it)).size })
    }

    @Test fun `legacy frames survive other splits and concatenation`() {
        val oldBoard = bytes("DC5A5C2025D600003BF500003BF50000FFDE1399" +
            "0DEF0000024602460000000000000000")
        val newBoard = bytes("DC5A5C20238A0112121A00004D450005064611F2" +
            "0E1000000AF00AF0041B000300000000")
        val parser = VeteranAdapter()
        parser.notifyConnectingTo("Sherman-Max")
        assertEquals(96.86f, telemetry(parser, oldBoard).single().voltage, 0.01f)
        val newData = telemetry(parser, newBoard.copyOfRange(0, 7)) +
            telemetry(parser, newBoard.copyOfRange(7, 21)) +
            telemetry(parser, newBoard.copyOfRange(21, newBoard.size))
        assertEquals(27.4f, newData.single().speed, 0.01f)
        assertEquals(45.94f, newData.single().maxTemperature, 0.01f)
        assertEquals(347.461f, newData.single().totalDistance, 0.001f)
        assertEquals(2, telemetry(VeteranAdapter(), oldBoard + newBoard).size)
    }

    @Test fun `Sherman S captured frame decodes`() {
        val adapter = VeteranAdapter()
        val head = bytes("DC5A5C22266200000084000017A2000000000C38")
        val tail = bytes("0B03000000C600E40BBD0003188B0000006F")
        assertTrue(telemetry(adapter, head).isEmpty())
        val data = telemetry(adapter, tail).single()
        assertEquals(98.26f, data.voltage, 0.01f)
        assertEquals(0.132f, data.tripDistance, 0.001f)
    }

    @Test fun `truncated and malformed short candidates resync without false telemetry`() {
        val adapter = VeteranAdapter()
        assertTrue(telemetry(adapter, firstFrame.copyOfRange(0, 35)).isEmpty())
        assertEquals(1, telemetry(adapter, firstFrame.copyOfRange(35, 36)).size)
        val invalidLen = bytes("dc5a5c0f")
        val invalidReserved = firstFrame.copyOf().also { it[22] = 1 }
        val invalidCharge = firstFrame.copyOf().also { it[23] = 2 }
        val invalidMode = firstFrame.copyOf().also { it[30] = 3 }
        for (invalid in listOf(invalidLen, invalidReserved, invalidCharge, invalidMode)) {
            val fresh = VeteranAdapter()
            assertEquals(1, telemetry(fresh, invalid + firstFrame).size)
        }
    }

    @Test fun `minimum payload uses model hint when version is absent`() {
        val adapter = VeteranAdapter()
        adapter.notifyConnectingTo("Sherman-Max")
        val short = firstFrame.copyOfRange(0, 20).also { it[3] = 0x10 }
        val data = telemetry(adapter, short).single()
        assertEquals(95.61f, data.voltage, 0.01f)
        assertEquals(37.29f, data.maxTemperature, 0.01f)
        assertTrue(data.batteryPercent > 0)
        assertEquals(-1f, data.wheelAlarmSpeedKmh, 0f)
    }
}
