package com.example

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.screens.OnboardingScreen
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
class OnboardingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun onboarding_screenshot() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          OnboardingScreen(onFinished = {})
        }
      }
    }

    // Advance past the entry delay and the fade/slide-in animation so the
    // content behind AnimatedVisibility is fully drawn before capture.
    composeTestRule.mainClock.advanceTimeBy(2000)

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/onboarding.png")
  }
}
