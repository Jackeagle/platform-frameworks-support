/*
 * Copyright 2017 The Android Open Source Project
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

package androidx.recyclerview.selection;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.support.annotation.RestrictTo;
import android.support.v7.widget.RecyclerView;

import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails;

/**
 * Override methods in this class to connect specialized behaviors of the selection
 * code to the application environment.
 *
 * @param <K> Selection key type. Usually String or Long.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class FocusCallbacks<K> {

    static final <K> FocusCallbacks<K> dummy() {
        return new FocusCallbacks<K>() {
            @Override
            public void focusItem(ItemDetails<K> item) {
            }

            @Override
            public boolean hasFocusedItem() {
                return false;
            }

            @Override
            public int getFocusedPosition() {
                return RecyclerView.NO_POSITION;
            }

            @Override
            public void clearFocus() {
            }
        };
    }

    /**
     * If environment supports focus, focus {@code item}.
     */
    public abstract void focusItem(ItemDetails<K> item);

    /**
     * @return true if there is a focused item.
     */
    public abstract boolean hasFocusedItem();

    /**
     * @return the position of the currently focused item, if any.
     */
    public abstract int getFocusedPosition();

    /**
     * If the environment supports focus and something is focused, unfocus it.
     */
    public abstract void clearFocus();
}