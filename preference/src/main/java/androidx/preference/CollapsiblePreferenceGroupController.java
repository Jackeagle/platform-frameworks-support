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

package androidx.preference;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A controller to handle child visibility logic when dealing with expandable preferences.
 */
final class CollapsiblePreferenceGroupController {

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    final PreferenceGroupAdapter mPreferenceGroupAdapter;
    private final Context mContext;

    /**
     * Whether there is a child {@link PreferenceGroup} that has an expandable preference. This
     * is used to avoid unnecessary preference tree rebuilds when no such group exists.
     */
    private boolean mHasExpandablePreference = false;

    CollapsiblePreferenceGroupController(PreferenceGroup preferenceGroup,
            PreferenceGroupAdapter preferenceGroupAdapter) {
        mPreferenceGroupAdapter = preferenceGroupAdapter;
        mContext = preferenceGroup.getContext();
    }

    /**
     * Generates the visible section of the {@link PreferenceGroup}.
     *
     * @param group The root preference group to be processed
     * @return The flattened and visible section of the preference group
     */
    public List<Preference> createVisiblePreferencesList(PreferenceGroup group) {
        return createInnerVisiblePreferencesList(group);
    }

    private List<Preference> createInnerVisiblePreferencesList(PreferenceGroup group) {
        mHasExpandablePreference = false;
        int visiblePreferenceCount = 0;
        final boolean hasExpandablePreference =
                group.getInitialExpandedChildrenCount() != Integer.MAX_VALUE;
        final List<Preference> visiblePreferences = new ArrayList<>();
        final List<Preference> collapsedPreferences = new ArrayList<>();

        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);

            if (!preference.isVisible()) {
                continue;
            }

            if (!hasExpandablePreference
                    || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                visiblePreferences.add(preference);
            } else {
                collapsedPreferences.add(preference);
            }

            // PreferenceGroups do not count towards the maximal number of preferences to show
            if (!(preference instanceof PreferenceGroup)) {
                visiblePreferenceCount++;
                continue;
            }

            PreferenceGroup innerGroup = (PreferenceGroup) preference;
            if (!innerGroup.isOnSameScreenAsChildren()) {
                continue;
            }

            // Recursively generate nested list of visible preferences
            final List<Preference> innerList = createInnerVisiblePreferencesList(innerGroup);
            if (hasExpandablePreference && mHasExpandablePreference) {
                throw new IllegalArgumentException("Nested expand buttons are not supported!");
            }

            for (Preference inner : innerList) {
                if (!hasExpandablePreference
                        || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                    visiblePreferences.add(inner);
                } else {
                    collapsedPreferences.add(inner);
                }
                visiblePreferenceCount++;
            }
        }

        // If there are any visible preferences being hidden, add an expand button to show the rest
        // of the preferences. Clicking the expand button will show all the visible preferences.
        if (hasExpandablePreference
                && visiblePreferenceCount > group.getInitialExpandedChildrenCount()) {
            final ExpandButton expandButton = createExpandButton(group, collapsedPreferences);
            visiblePreferences.add(expandButton);
        }
        mHasExpandablePreference |= hasExpandablePreference;
        return visiblePreferences;
    }

    /**
     * Called when a preference has changed its visibility.
     *
     * @param preference The preference whose visibility has changed
     * @return {@code true} if view update has been handled by this controller
     */
    public boolean onPreferenceVisibilityChange(Preference preference) {
        if (preference instanceof PreferenceGroup || mHasExpandablePreference) {
            // Changing the visibility of a group determines the visibility of its children, so
            // to be safe we want to fully rebuild the hierarchy if the visibility of a group
            // changes.

            // If there is an expand button, since we only want to show up to the
            // maximal number of preferences, preference visibility change can result in the
            // expand button being added/removed, as well as changes to its summary. Rebuild to
            // ensure that the correct data is shown.
            mPreferenceGroupAdapter.onPreferenceHierarchyChange(preference);
            return true;
        }
        return false;
    }

    private ExpandButton createExpandButton(final PreferenceGroup group,
            List<Preference> collapsedPreferences) {
        final ExpandButton preference = new ExpandButton(mContext, collapsedPreferences,
                group.getId());
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                group.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
                mPreferenceGroupAdapter.onPreferenceHierarchyChange(preference);
                final PreferenceGroup.OnExpandButtonClickListener listener =
                        group.getOnExpandButtonClickListener();
                if (listener != null) {
                    listener.onExpandButtonClick();
                }
                return true;
            }
        });
        return preference;
    }

    /**
     * A {@link Preference} that expands the collapsed items in the {@link PreferenceGroup}.
     */
    static class ExpandButton extends Preference {
        private long mId;

        ExpandButton(Context context, List<Preference> collapsedPreferences, long parentId) {
            super(context);
            initLayout();
            setSummary(collapsedPreferences);
            // Since IDs are unique, using the parentId as a reference ensures that this expand
            // button will have a unique ID and hence transitions will be correctly animated by
            // RecyclerView when there are multiple expand buttons.
            mId = parentId + 1000000;
        }

        private void initLayout() {
            setLayoutResource(R.layout.expand_button);
            setIcon(R.drawable.ic_arrow_down_24dp);
            setTitle(R.string.expand_button_title);
            // Sets a high order so that the expand button will be placed at the bottom of the group
            setOrder(999);
        }

        /*
         * Sets the summary of the expand button to a list containing the titles of the collapsed
         * preferences. If there are any nested groups with titles, only add the group's title
         * and not the titles of the group's children.
         */
        private void setSummary(List<Preference> collapsedPreferences) {
            CharSequence summary = null;
            final List<PreferenceGroup> parents = new ArrayList<>();

            for (Preference preference : collapsedPreferences) {
                final CharSequence title = preference.getTitle();
                if (preference instanceof PreferenceGroup && !TextUtils.isEmpty(title)) {
                    parents.add((PreferenceGroup) preference);
                }
                if (parents.contains(preference.getParent())) {
                    if (preference instanceof PreferenceGroup) {
                        parents.add((PreferenceGroup) preference);
                    }
                    continue;
                }
                if (!TextUtils.isEmpty(title)) {
                    if (summary == null) {
                        summary = title;
                    } else {
                        summary = getContext().getString(
                                R.string.summary_collapsed_preference_list, summary, title);
                    }
                }
            }
            setSummary(summary);
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            holder.setDividerAllowedAbove(false);
        }

        @Override
        public long getId() {
            return mId;
        }
    }

}
