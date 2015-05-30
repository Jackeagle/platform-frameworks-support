/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package android.support.v17.leanback.supportleanbackshowcase;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * TODO: JavaDoc
 */
public class WizardExample1stStepFragment extends WizardExampleBaseStepFragment {

    private static final int ACTION_ID_BUY_HD = 1;
    private static final int ACTION_ID_BUY_SD = ACTION_ID_BUY_HD + 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWizardActivity().setStep(1);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(mMovie.getTitle(),
                getString(R.string.wizard_example_choose_rent_options),
                mMovie.getBreadcrump(), null);
        return guidance;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder()
                .id(ACTION_ID_BUY_HD)
                .title(getString(R.string.wizard_example_rent_hd))
                .description(mMovie.getPriceHd() + " " +
                        getString(R.string.wizard_example_watch_hd))
                .build();
        actions.add(action);
        action = new GuidedAction.Builder()
                .id(ACTION_ID_BUY_SD)
                .title(getString(R.string.wizard_example_rent_sd))
                .description(mMovie.getPriceSd() + " " +
                        getString(R.string.wizard_example_watch_sd))
                .build();
        actions.add(action);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        GuidedStepFragment fragment = new WizardExample2ndStepFragment();
        Bundle args = getArguments(); // Reuse the same arguments this fragment was given.
        args.putBoolean("hd", ACTION_ID_BUY_HD == action.getId());
        fragment.setArguments(args);
        add(getFragmentManager(), fragment);
    }
}
