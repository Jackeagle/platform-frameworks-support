/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.recyclerview.benchmark

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.benchmark.PerfStatusReporter
import androidx.recyclerview.benchmark.test.R
import androidx.recyclerview.widget.RecyclerView
import androidx.test.annotation.UiThreadTest
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    /**
     * TrivialAdapter recreated each time, since it's stateful: [TrivialAdapter.disableReuse]
     */
    private lateinit var trivialAdapter: TrivialAdapter

    @get:Rule
    val reporter = PerfStatusReporter()

    @get:Rule
    val activityRule = ActivityTestRule(RecyclerViewActivity::class.java)

    @Before
    fun setup() {
        activityRule.runOnUiThread {
            val rv = activityRule.activity.recyclerView
            trivialAdapter = TrivialAdapter()
            rv.adapter = trivialAdapter
            rv.setRecycledViewPool(ZeroSizePool())

            // offset by 50 to ensure we are 50% through the first item
            rv.scrollBy(0, 50)
        }
    }

    @UiThreadTest
    @Test
    fun offset() {
        val state = reporter.benchmarkState

        val rv = activityRule.activity.recyclerView
        var offset = 10
        while (state.keepRunning()) {
            // keep scrolling up and down - no new item should be revealed
            rv.scrollBy(0, offset)
            offset *= -1
        }
    }

    @UiThreadTest
    @Test
    fun bindOffset() {
        val state = reporter.benchmarkState

        val rv = activityRule.activity.recyclerView
        while (state.keepRunning()) {
            // each scroll should reveal a new item
            rv.scrollBy(0, 100)
        }
    }

    @UiThreadTest
    @Test
    fun createBindOffset() {
        val state = reporter.benchmarkState
        trivialAdapter.disableReuse = true
        trivialAdapter.inflater = {
            val view = View(it.context)
            view.layoutParams = RecyclerView.LayoutParams(100, 100)
            view
        }

        val rv = activityRule.activity.recyclerView
        while (state.keepRunning()) {
            // each scroll should reveal a new item that must be inflated
            rv.scrollBy(0, 100)
        }
    }

    @UiThreadTest
    @Test
    fun inflateBindOffset() {
        val state = reporter.benchmarkState
        trivialAdapter.disableReuse = true

        val rv = activityRule.activity.recyclerView
        while (state.keepRunning()) {
            // each scroll should reveal a new item that must be inflated
            rv.scrollBy(0, 100)
        }
    }
}

private class ZeroSizePool : RecyclerView.RecycledViewPool() {
    override fun putRecycledView(scrap: RecyclerView.ViewHolder) {
        // drop on floor, we won't be coming back
    }
}

private class TrivialViewHolder(view: View) : RecyclerView.ViewHolder(view)

/**
 * Displays *many* items, each 100px tall, with minimal inflation/bind work.
 */
private class TrivialAdapter : RecyclerView.Adapter<TrivialViewHolder>() {
    var disableReuse = false
    var createInCode = false

    var inflater: (ViewGroup) -> View = {
        LayoutInflater.from(it.context).inflate(
                R.layout.item_view, it, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrivialViewHolder {
        return TrivialViewHolder(inflater(parent))
    }

    override fun onBindViewHolder(holder: TrivialViewHolder, position: Int) {
        val color = position % 256
        holder.itemView.setBackgroundColor(Color.rgb(color, color, color))
    }

    override fun getItemViewType(position: Int): Int {
        return if (disableReuse) position else 0
    }

    override fun getItemCount() = Integer.MAX_VALUE
}
