package com.tosspayments.paymentsdk.interfaces

import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class MainThreadReader(
    private val timeoutMillis: Long,
    private val postToMainThread: (Runnable) -> Unit
) {
    fun <T : Any> read(value: () -> T): T? {
        val task = FutureTask(value)

        postToMainThread(task)

        return try {
            task.get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            null
        }
    }
}
