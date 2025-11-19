package com.example.honk

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddReminderUITest {

    @Rule @JvmField
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addReminder_showsReminderInList() {
        // Press FAB to open the dialog
        onView(withId(R.id.fab_add_reminder)).perform(click())

        // Enter the reminder text
        onView(withId(R.id.reminderText))
            .perform(typeText("UI Test Reminder"), closeSoftKeyboard())

        // Click the Add button
        onView(withId(R.id.addReminderButton)).perform(click())

        // Check that the reminder appears on the screen
        onView(withText(containsString("UI Test Reminder")))
            .check(matches(isDisplayed()))
    }
}
