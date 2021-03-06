package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    companion object {
        val MOCK_REMINDER_DTO =
            ReminderDTO("title", "description", "location", 0.0, 0.0, "uuid", 10)
        val MOCK_REMINDER =
            ReminderDataItem("title", "description", "location", 0.0, 0.0, "uuid", 10)
    }

    private val reminderList = mutableListOf<ReminderDTO>()

    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (!shouldReturnError) {
            Result.Success(reminderList)
        } else {
            Result.Error("error")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (!shouldReturnError) {
            Result.Success(reminderList.first { it.id == id })
        } else {
            Result.Error("error")
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }

    override suspend fun deleteReminders(id: String) {
        reminderList.removeIf { reminder ->
            id.contains(reminder.id)
        }
    }

    override suspend fun getLastRequestCode(): Result<Int> {
        return if (!shouldReturnError) {
            Result.Success(reminderList.lastOrNull()?.requestCode ?: 0)
        } else {
            Result.Error("error")
        }
    }


}