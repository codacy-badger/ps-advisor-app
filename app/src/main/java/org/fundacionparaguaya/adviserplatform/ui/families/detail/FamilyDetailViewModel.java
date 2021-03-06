package org.fundacionparaguaya.adviserplatform.ui.families.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import com.instabug.library.Instabug;
import org.fundacionparaguaya.assistantadvisor.BuildConfig;
import org.fundacionparaguaya.adviserplatform.data.model.*;
import org.fundacionparaguaya.adviserplatform.data.repositories.FamilyRepository;
import org.fundacionparaguaya.adviserplatform.data.repositories.SnapshotRepository;
import org.fundacionparaguaya.adviserplatform.util.IndicatorUtilities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class FamilyDetailViewModel extends ViewModel {

    private FamilyRepository mFamilyRepository;
    private SnapshotRepository mSnapshotRespository;

    private LiveData<Family> currentFamily;

    final private MutableLiveData<Snapshot> mSelectedSnapshot = new MutableLiveData<>();

    private LiveData<List<Snapshot>> mSnapshots;
    private final MutableLiveData<LifeMapPriority> mSelectedPriority = new MutableLiveData<>();


    //Maps the selected snapshot to a list of indicators. This livedata object will notify it's observers when
    //the selected snapshot changes
    final private LiveData<Collection<IndicatorOption>> mIndicatorsForSelected = Transformations.map(mSelectedSnapshot, selected ->
    {
        if (selected == null) {
            return null;
        } else {
            return IndicatorUtilities.getResponsesAscending(selected.getIndicatorResponses().values());
        }
    });

    //Maps the selected snapshot to a list of priorities This livedata object will notify it's observers when
    //the selected snapshot changes
    final private LiveData<List<LifeMapPriority>> mPrioritiesForSelected = Transformations.map(mSelectedSnapshot, selected ->
    {
        clearSelectedPriority();

        if (selected == null) {
            return null;
        } else {
            return selected.getPriorities();
        }
    });

    public FamilyDetailViewModel(FamilyRepository familyRepository, SnapshotRepository snapshotRespository) {
        mFamilyRepository = familyRepository;
        mSnapshotRespository = snapshotRespository;

        mSelectedSnapshot.setValue(null);
        mSelectedPriority.setValue(null);
    }

    /**
     * Sets the current family for this view model and returns the LiveData representation
     *
     * @param id family id for this view model
     * @return current family selected
     */
    public LiveData<Family> setFamily(int id) {
        currentFamily = mFamilyRepository.getFamily(id);

        //return live data of snapshots that sorts descending
        mSnapshots = Transformations.map(
                Transformations.switchMap(currentFamily, currentFamily -> {
                    if (currentFamily == null) {
                        return null;
                    } else return mSnapshotRespository.getSnapshots(currentFamily);
                }),
                snapshots->
                {
                   Collections.sort(snapshots, (o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
                   return snapshots;
                });

        return currentFamily;
    }

    /**
     * Gets the current family that's been set by setFamily
     **/
    public LiveData<Family> CurrentFamily() {
        if (currentFamily == null) {
            throw new IllegalStateException("setFamily must be called in ViewModel before CurrentFamily");
        } else return currentFamily;
    }

    //TODO: Instead of returning the value currently selected snapshot, this should return the latest value from the family
    //Right now though, priorities are associated with snapshots instead of families
    public LiveData<IndicatorOption> LatestResponseForIndicator(Indicator i) {
        if (mSelectedSnapshot.getValue() != null) {
            return Transformations.map(mSelectedSnapshot, selected ->
            {
                if (selected == null) {
                    return null;
                } else {
                    return IndicatorUtilities.getResponseForIndicator(i, mSelectedSnapshot.getValue().getIndicatorResponses());
                }
            });
        } else {
            Exception e = new IllegalStateException("LatestResponseForIndicator called with no snapshot selected");

            if (BuildConfig.DEBUG) e.printStackTrace();
            else Instabug.reportException(e);

            return null;
        }
    }

    public LiveData<Snapshot> SelectedSnapshot() {
        return mSelectedSnapshot;
    }

    public void selectFirstSnapshot()
    {
        List<Snapshot> snapshots = mSnapshots.getValue();

        if(snapshots!=null && snapshots.size()>0)
        {
            setSelectedSnapshot(snapshots.get(0));
        }
    }

    public void selectLatestSnapshot()
    {
        List<Snapshot> snapshots = mSnapshots.getValue();

        if(snapshots!=null && snapshots.size()>0)
        {
            setSelectedSnapshot(snapshots.get(snapshots.size()-1));
        }
    }

    public LiveData<List<Snapshot>> Snapshots() {
        return mSnapshots;
    }

    public void setSelectedSnapshot(Snapshot s) {
        mSelectedSnapshot.setValue(s);
    }

    public LiveData<LifeMapPriority> SelectedPriority() {
        return mSelectedPriority;
    }

    public LifeMapPriority getSelectedPriority(){
        return mSelectedPriority.getValue();
    }

    public void selectFirstPriority(){
        List<LifeMapPriority> priorities = mPrioritiesForSelected.getValue();

        if(priorities !=null && priorities.size()>0)
        {
            mSelectedPriority.setValue(priorities.get(0));
        }
    }

    public void setSelectedPriority(LifeMapPriority priority) {
        mSelectedPriority.setValue(priority);
    }

    public void clearSelectedPriority(){
        mSelectedPriority.setValue(null);
    }
    /**
     * Returns the priorities for the selected snapshot. Will update when the selected snapshot is changed
     *
     * @return
     */
    public LiveData<List<LifeMapPriority>> Priorities() {
        return mPrioritiesForSelected;
    }

    /**
     * Returns the indicators for the selected snapshot. Will update when the selected snapshot is changed
     *
     * @return
     */
    public LiveData<Collection<IndicatorOption>> SelectedSnapshotIndicators() {
        return mIndicatorsForSelected;
    }

    public boolean hasImageUri() {
        Family family = CurrentFamily().getValue();
        if (family == null || family.getImageUrl() == null || family.getImageUrl().isEmpty()) {
            return false;
        }
        return true;
    }

    public Uri getImageUri() {
        Uri uri;
        Family family = CurrentFamily().getValue();
        if (family != null && family.getImageUrl() != null && !family.getImageUrl().isEmpty()) {
            uri = Uri.parse(family.getImageUrl());
        } else {
            uri = Uri.parse("https://s3.us-east-2.amazonaws.com/fp-psp-images/44-3.jpg");
        }
        return uri;
    }
}
