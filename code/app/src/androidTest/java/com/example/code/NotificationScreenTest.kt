package com.example.code

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.screens.NotificationsScreen
import com.example.code.ui.theme.PichusTheme
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the notifications screen (US 01.03.01, US 01.03.02).
 */
@RunWith(AndroidJUnit4::class)
class NotificationsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Makes a notification sent by the app to user 1
    private fun makeNotification(type: NotificationType, text: String): Notification {
        return Notification(Message(NotificationService.SYSTEM_ID, 1, text, Timestamp.now()), type)
    }

    @Test
    fun showsLotteryWonNotification() {
        val won = makeNotification(NotificationType.LOTTERY_WON, "You won the lottery!")
        composeRule.setContent {
            PichusTheme { NotificationsScreen(notifications = listOf(won)) }
        }
        composeRule.onNodeWithText("Lottery Results: You Won!").assertIsDisplayed()
        composeRule.onNodeWithText("You won the lottery!").assertIsDisplayed()
    }

    @Test
    fun showsLotteryLostNotification() {
        val lost = makeNotification(NotificationType.LOTTERY_LOST, "You were not picked in the lottery this time.")
        composeRule.setContent {
            PichusTheme { NotificationsScreen(notifications = listOf(lost)) }
        }
        composeRule.onNodeWithText("Lottery Results: Not Selected").assertIsDisplayed()
        composeRule.onNodeWithText("You were not picked in the lottery this time.").assertIsDisplayed()
    }

    @Test
    fun showsEmptyMessageWhenNoNotifications() {
        composeRule.setContent {
            PichusTheme { NotificationsScreen(notifications = emptyList()) }
        }
        composeRule.onNodeWithText("No notifications yet.").assertIsDisplayed()
    }
}