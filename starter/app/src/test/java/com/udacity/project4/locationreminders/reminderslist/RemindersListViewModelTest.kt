package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.BaseTest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O])
class RemindersListViewModelTest : BaseTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val remindersViewModel: RemindersListViewModel by inject()

    private val dataSource: ReminderDataSource by inject()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun loadReminders_loading() {
        runBlockingTest {
            mainCoroutineRule.pauseDispatcher()
            remindersViewModel.loadReminders()
            assertThat(remindersViewModel.showLoading.getOrAwaitValue(), `is`(true))
            mainCoroutineRule.resumeDispatcher()
            assertThat(remindersViewModel.showLoading.getOrAwaitValue(), `is`(false))
        }
    }

    @Test
    fun loadReminders_successList() {
        assertThat(remindersViewModel.remindersList.value, `is`(nullValue()))
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.remindersList.value, `is`(notNullValue()))
    }

    @Test
    fun loadReminders_successValues() {
        runBlockingTest {
            dataSource.saveReminder(FakeDataSource.MOCK_REMINDER_DTO)
        }
        assertThat(remindersViewModel.remindersList.value, `is`(nullValue()))
        remindersViewModel.loadReminders()
        val reminders = listOf(FakeDataSource.MOCK_REMINDER_DTO)
        val dataList = ArrayList<ReminderDataItem>()
        dataList.addAll(reminders.map { reminder ->
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id,
                reminder.requestCode
            )
        })
        assertEquals(remindersViewModel.remindersList.value, dataList)
    }

    @Test
    fun loadReminders_error() {
        (dataSource as? FakeDataSource)?.shouldReturnError = true

        assertThat(remindersViewModel.showSnackBar.value, `is`(nullValue()))
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showSnackBar.value, not(`is`(nullValue())))
    }

    @Test
    fun invalidateShowNoData_noData() {
        (dataSource as? FakeDataSource)?.shouldReturnError = true
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun invalidateShowNoData_hasData() {
        runBlockingTest {
            dataSource.saveReminder(FakeDataSource.MOCK_REMINDER_DTO)
        }
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showNoData.value, `is`(false))
    }
}