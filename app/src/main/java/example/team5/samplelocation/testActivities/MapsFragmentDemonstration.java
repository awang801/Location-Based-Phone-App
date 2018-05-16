package example.team5.samplelocation.testActivities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import example.team5.samplelocation.R;

import static android.R.attr.id;

/**
 * Created by Philip on 4/6/2017.
 */

public class MapsFragmentDemonstration extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mapfragmentstatic);

        // TODO - DYLAN : All you need to do is define a layout object for the fragment to occupy and pass in the group-name and admin-id just like I did below
        RelativeLayout fragContainer = (RelativeLayout) findViewById(R.id.fragment_container);

        LinearLayout fragLayout = new LinearLayout(this);
        fragLayout.setOrientation(LinearLayout.HORIZONTAL);
        fragLayout.setId(R.id.fragment_container);
        getFragmentManager().beginTransaction().add(fragLayout.getId(), MapsFragment.newInstance("GabeGroup", "IBzzDdFCpYfbxSmRVitibig3OGA2"), "TAG").commit();
        fragContainer.addView(fragLayout);
    }
}
