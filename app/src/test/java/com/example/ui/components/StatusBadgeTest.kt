package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.LeadSource
import com.example.data.model.LeadStatus
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class StatusBadgeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun everyStatusAndSourceShowsItsLabel() {
    composeTestRule.setContent { MyApplicationTheme { AllBadges() } }

    LeadStatus.entries.forEach { composeTestRule.onNodeWithText(it.label).assertIsDisplayed() }
    LeadSource.entries.forEach { composeTestRule.onNodeWithText(it.label).assertIsDisplayed() }
  }

  @Test
  fun badges_lightTheme_screenshot() {
    composeTestRule.setContent { MyApplicationTheme(darkTheme = false) { AllBadges() } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/badges_light.png")
  }

  @Test
  @Config(qualifiers = "+night")
  fun badges_darkTheme_screenshot() {
    composeTestRule.setContent { MyApplicationTheme(darkTheme = true) { AllBadges() } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/badges_dark.png")
  }
}

@androidx.compose.runtime.Composable
private fun AllBadges() {
  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    LeadStatus.entries.forEach { StatusBadge(status = it) }
    LeadSource.entries.forEach { SourceBadge(source = it) }
  }
}
