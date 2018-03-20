/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.preference.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for assigning a parent in {@link androidx.preference.PreferenceGroup}.
 */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class PreferenceParentGroupTest {

    private Context mContext;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    /**
     * Tests that the parent PreferenceGroup is correctly assigned and removed when creating
     * preferences from code.
     */
    @Test
    @UiThreadTest
    public void parentAddRemoveTest() {
        PreferenceManager manager = new PreferenceManager(mContext);

        PreferenceScreen screen = manager.createPreferenceScreen(mContext);
        assertNull(screen.getParent());

        PreferenceCategory category = new PreferenceCategory(mContext);
        assertNull(category.getParent());

        CheckBoxPreference preference = new CheckBoxPreference(mContext);
        assertNull(preference.getParent());

        screen.addPreference(category);
        assertEquals(screen, category.getParent());

        category.addPreference(preference);
        assertEquals(category, preference.getParent());

        screen.removePreference(category);
        assertNull(category.getParent());

        category.removePreference(preference);
        assertNull(preference.getParent());
    }

    /**
     * Tests that the parent PreferenceGroup is correctly set during reassignment.
     */
    @Test
    @UiThreadTest
    public void parentReassignTest() {
        PreferenceManager manager = new PreferenceManager(mContext);

        PreferenceScreen screen = manager.createPreferenceScreen(mContext);

        PreferenceCategory category1 = new PreferenceCategory(mContext);
        screen.addPreference(category1);
        PreferenceCategory category2 = new PreferenceCategory(mContext);
        screen.addPreference(category2);

        CheckBoxPreference preference = new CheckBoxPreference(mContext);
        assertNull(preference.getParent());

        category1.addPreference(preference);
        assertEquals(category1, preference.getParent());

        category1.removePreference(preference);
        category2.addPreference(preference);
        assertEquals(category2, preference.getParent());
    }

    /**
     * Tests that the most recent parent PreferenceGroup is set when adding a preference to two
     * different groups without removing it first. This is maybe not something we want to support
     * in the future but this makes this behavior visible.
     */
    @Test
    @UiThreadTest
    public void parentDoubleAddTest() {
        PreferenceManager manager = new PreferenceManager(mContext);

        PreferenceScreen screen = manager.createPreferenceScreen(mContext);

        PreferenceCategory category1 = new PreferenceCategory(mContext);
        screen.addPreference(category1);
        PreferenceCategory category2 = new PreferenceCategory(mContext);
        screen.addPreference(category2);

        CheckBoxPreference preference = new CheckBoxPreference(mContext);
        assertNull(preference.getParent());

        category1.addPreference(preference);
        category2.addPreference(preference);

        assertEquals(category2, preference.getParent());
    }
}
