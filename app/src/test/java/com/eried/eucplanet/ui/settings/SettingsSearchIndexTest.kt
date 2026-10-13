package com.eried.eucplanet.ui.settings

import com.eried.eucplanet.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The settings search reads its words from the code through a generated
 * index (app/settings-search-index.gradle.kts). These pin what it must find,
 * so a change to how the settings are written cannot quietly empty it.
 */
class SettingsSearchIndexTest {

    private fun entry(section: String, res: Int) =
        SettingsSearchIndex.sections.getValue(section).firstOrNull { it.res == res }

    @Test fun `every section has its texts`() {
        assertEquals(
            setOf("general", "dashboard", "display", "speed", "voice", "motor", "cloud", "alarms",
                "auto", "navigator", "location", "integration", "watch", "advanced"),
            SettingsSearchIndex.sections.keys,
        )
        SettingsSearchIndex.sections.forEach { (key, list) ->
            assertTrue("$key has nothing to search", list.size >= 3)
        }
    }

    @Test fun `every sub-heading is searchable`() {
        // The rule the hand-written lists kept missing: "Home screen widget"
        // and six older headings once shipped that nobody could search for.
        // Read off the source, because a heading is a call, not data.
        val headings = File("src/main/java/com/eried/eucplanet/ui/settings").walk()
            .filter { it.extension == "kt" }
            .flatMap { Regex("""SectionHeader\(\s*stringResource\(R\.string\.(\w+)""").findAll(it.readText()) }
            .map { it.groupValues[1] }.toSet()
        assertTrue("found no headings at all", headings.size > 15)
        val names = headings.associateWith { name -> R.string::class.java.getField(name).getInt(null) }
        val indexed = SettingsSearchIndex.sections.values.flatten().map { it.res }.toSet()
        val missing = names.filterValues { it !in indexed }.keys.sorted()
        assertTrue("These headings are not searchable: $missing", missing.isEmpty())
    }

    @Test fun `buttons are found, with the heading they sit under`() {
        val crew = entry("cloud", R.string.crews_pass_action)!!
        assertEquals(SettingsSearchIndex.Kind.BUTTON, crew.kind)
        assertEquals(R.string.section_online_stats, crew.under)
        assertEquals(SettingsSearchIndex.Kind.BUTTON, entry("integration", R.string.flic_start_scan)!!.kind)
    }

    @Test fun `rows and descriptions are told apart`() {
        assertEquals(SettingsSearchIndex.Kind.LABEL, entry("voice", R.string.voice_speech_speed)!!.kind)
        assertEquals(SettingsSearchIndex.Kind.DESCRIPTION, entry("cloud", R.string.cloud_trips_caption)!!.kind)
    }

    @Test fun `a label picked in a when is found`() {
        assertTrue(entry("voice", R.string.report_speed) != null)
    }

    @Test fun `what only shows in a dialog is left out`() {
        assertEquals(null, entry("speed", R.string.lockdown_title))
    }
}
