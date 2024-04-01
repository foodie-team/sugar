package com.github.foodiestudio.sugar

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class FlowTest {

    @Test
    fun `sharedFlow missing events`() = runTest {
        val sharedFlow = MutableSharedFlow<Int>()
        val received = mutableListOf<Int>()
        val job = launch(start = CoroutineStart.DEFAULT) {
            sharedFlow.collect {
                received.add(it)
                println("Received value: $it")
            }
        }
        flowOf(1, 2, 3, 4).collect {
            sharedFlow.emit(it)
        }
//        delay(3000) // 这个时间随意，不影响结果
        job.cancel()
        assertEquals(0, received.size)
    }

    @Test
    fun `sharedFlow missing events fixed`() = runTest {
        val sharedFlow = MutableSharedFlow<Int>()
        val received = mutableListOf<Int>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            sharedFlow.collect {
                received.add(it)
                println("Received value: $it")
            }
        }
        flowOf(1, 2, 3, 4).collect {
            sharedFlow.emit(it)
        }
//        delay(3000) // 这个时间随意，不影响结果
        job.cancel()
        assertEquals(4, received.size)
    }
}