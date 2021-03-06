package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.BaseTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.ReminderUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.qualifier.named
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest : BaseTest {

    private val remindersDao: RemindersDao by inject(named("dao"))

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        val loaded = remindersDao.getReminderById(reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.requestCode, `is`(reminder.requestCode))
    }

    @Test
    fun saveReminderAndGetReminders() = runBlockingTest {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        val loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), not(true))
    }

    @Test
    fun getRemindersWithoutData() = runBlockingTest {
        val loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        var loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), not(true))

        remindersDao.deleteAllReminders()
        loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), `is`(true))
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        var loaded = remindersDao.getReminderById(reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())

        remindersDao.deleteReminders(listOf(reminder.id))
        loaded = remindersDao.getReminderById(reminder.id)
        assertThat(loaded, nullValue())
    }

    @Test
    fun getLastRequestCode_withData() = runBlockingTest {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        val data = remindersDao.getReminders().last()
        val code = remindersDao.getLastRequestCode()
        assertThat(code, `is`(data.requestCode))
    }

    @Test
    fun getLastRequestCode_noData() = runBlockingTest {
        val code = remindersDao.getLastRequestCode()
        assertThat(code, `is`(nullValue()))
    }
}