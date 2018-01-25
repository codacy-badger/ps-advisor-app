package org.fundacionparaguaya.advisorapp.activities;

import android.animation.ObjectAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.fundacionparaguaya.advisorapp.AdvisorApplication;
import org.fundacionparaguaya.advisorapp.R;
import org.fundacionparaguaya.advisorapp.fragments.AbstractSurveyFragment;
import org.fundacionparaguaya.advisorapp.fragments.BackgroundQuestionsFrag;
import org.fundacionparaguaya.advisorapp.fragments.SurveyIntroFragment;
import org.fundacionparaguaya.advisorapp.models.Family;
import org.fundacionparaguaya.advisorapp.viewmodels.InjectionViewModelFactory;
import org.fundacionparaguaya.advisorapp.viewmodels.SharedSurveyViewModel;
import org.fundacionparaguaya.advisorapp.viewmodels.SharedSurveyViewModel.*;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;

/**
 * Activity for surveying a family's situation. Displays the fragments that record background info and allows
 * the family to select indicators
 */

public class SurveyActivity extends AbstractFragSwitcherActivity
{
    static String FAMILY_ID_KEY = "FAMILY_ID";

    TextView mTvTitle;
    TextView mTvQuestionsLeft;
    TextView mTvNextUp;

    ProgressBar mProgressBar;
    ObjectAnimator mProgressAnimator;

    @Inject
    InjectionViewModelFactory mViewModelFactory;

    SharedSurveyViewModel mSurveyViewModel;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        ((AdvisorApplication) this.getApplication())
                .getApplicationComponent()
                .inject(this);

        mSurveyViewModel = ViewModelProviders
                .of(this, mViewModelFactory)
                .get(SharedSurveyViewModel.class);

        mTvTitle = findViewById(R.id.tv_surveyactivity_title);
        mTvNextUp = findViewById(R.id.tv_surveyactivity_nextup);
        mTvQuestionsLeft = findViewById(R.id.tv_surveyactivity_questionsleft);

        mProgressBar = findViewById(R.id.progressbar_surveyactivity);

        setFragmentContainer(R.id.survey_activity_fragment_container);

        initViewModel();
    }


    public void initViewModel()
    {

        //familyId can never equal -1 if retrieved from the database, so it is used as the default value
        int familyId = getIntent().getIntExtra(FAMILY_ID_KEY, -1);

        if(familyId == -1)
        {
            throw new IllegalArgumentException(this.getLocalClassName() + ": Found family id of -1. Family id is either not set" +
                    "or has been set innappropriately. To launch this activity with the family id properly set, use the " +
                    "build(int) function");
        }

        mSurveyViewModel.setFamily(familyId);

        //observe changes for family, when it has a value then show intro.
        mSurveyViewModel.getCurrentFamily().observe(this, (family ->
        {
            if(mSurveyViewModel.getSurveyState().getValue().equals(SurveyState.NONE))
            {
                mSurveyViewModel.getSurveyState().setValue(SurveyState.INTRO);
            }
        }));

        mSurveyViewModel.getProgress().observe(this, surveyProgress -> {

            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(mProgressBar,
                    "progress", mProgressBar.getProgress(), surveyProgress.getPercentageComplete());

            progressAnimator.setDuration(400);
            progressAnimator.start();

            mProgressBar.setProgress(surveyProgress.getPercentageComplete());
            mTvQuestionsLeft.setText(surveyProgress.getDescription());
        });

        mSurveyViewModel.getSurveyState().observe(this, surveyState -> {
            switch (surveyState)
            {
                case INTRO:

                    switchToFrag(SurveyIntroFragment.class);

                    break;

                case BACKGROUND_QUESTIONS:

                    switchToFrag(BackgroundQuestionsFrag.class);

                    break;

              //  case INDICATORS:

                /* * etc * */
            };
        });
    }

    @Override
    public void switchToFrag(Class fragmentClass)
    {
        super.switchToFrag(fragmentClass);

        AbstractSurveyFragment fragment = (AbstractSurveyFragment)getFragment(fragmentClass);
        this.mTvTitle.setText(fragment.getTitle());
    }

    public void setTitle(String title) {

    }

    //Returns and intent to open this activity, with an extra for the family's Id.
    public static Intent build(Context c, Family family)
    {
        Intent intent = new Intent(c, SurveyActivity.class);
        intent.putExtra(FAMILY_ID_KEY, family.getId());

        return intent;
    }
}