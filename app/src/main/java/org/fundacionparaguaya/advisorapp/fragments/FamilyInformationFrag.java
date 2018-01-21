package org.fundacionparaguaya.advisorapp.fragments;

import android.app.Fragment;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.fundacionparaguaya.advisorapp.AdvisorApplication;
import org.fundacionparaguaya.advisorapp.R;
import org.fundacionparaguaya.advisorapp.models.Family;
import org.fundacionparaguaya.advisorapp.viewmodels.AllFamiliesViewModel;
import org.fundacionparaguaya.advisorapp.viewmodels.FamilyInformationViewModel;
import org.fundacionparaguaya.advisorapp.viewmodels.InjectionViewModelFactory;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Mone Elokda on 1/20/2018.
 */

public class FamilyInformationFrag extends Fragment {
    private TextView mFamilyName;
    private TextView mAddress;
    private TextView mLocation;
    private SimpleDraweeView mFamilyImage;

    Family mfamily;

    static String selected = "SELECTED_FAMILY";


    @Inject
    InjectionViewModelFactory mViewModelFactory;
    FamilyInformationViewModel mFamilyInformationViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ((AdvisorApplication) getActivity().getApplication())
                .getApplicationComponent()
                .inject(this);

        mFamilyInformationViewModel = ViewModelProviders
                .of((FragmentActivity) getActivity(), mViewModelFactory)
                .get(FamilyInformationViewModel.class);
        LiveData<Family> family = mFamilyInformationViewModel.getFamily(getId());

        int id = getId();

        Bundle args = new Bundle();
        args.putInt(selected, id );
        FamilyInformationFrag f = new FamilyInformationFrag();
        f.setArguments(args);

    }



    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.family_fragment, container, false);

        mFamilyName = (TextView) view.findViewById(R.id.family_view_name);
        mAddress = (TextView) view.findViewById(R.id.location_content);
        mLocation = (TextView) view.findViewById(R.id.description_content);
        mFamilyImage = (SimpleDraweeView) view.findViewById(R.id.family_image_2);

        final Family family = mfamily;

        mFamilyName.setText(family.getName());
        mAddress.setText(family.getAddress());
        mLocation.setText((CharSequence) family.getLocation());

        Uri uri = Uri.parse("https://bongmendoza.files.wordpress.com/2012/08/urban-poor-family.jpg");
        mFamilyImage.setImageURI(uri);

        return view;

    }

}
