package fr.coppernic.demos.seos.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.coppernic.demos.seos.R;
import fr.coppernic.demos.seos.model.PacsData;
import fr.coppernic.demos.seos.presenter.PacsDataAdapter;
import fr.coppernic.demos.seos.presenter.RfidPresenter;
import fr.coppernic.demos.seos.presenter.RfidPresenterImpl;
import fr.coppernic.sdk.utils.helpers.CpcOs;

public class MainActivity extends AppCompatActivity implements MainView {

    private RfidPresenter rfidPresenter;
    private PacsDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rfidPresenter = new RfidPresenterImpl(this);

        adapter = new PacsDataAdapter(this, R.layout.pacs_data_row, new ArrayList<PacsData>());
        ListView lvTagsRead = (ListView)findViewById(R.id.lvTagsRead);
        lvTagsRead.setAdapter(adapter);
        lvTagsRead.setEmptyView(findViewById(R.id.empty));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rfidPresenter.readTags(!rfidPresenter.isReading());
            }
        });

        showVersion();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            clearReadTags();
            return true;
        } else if (id == R.id.action_settings) {
            showSettings();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showTag(final PacsData tag, final boolean updateList) {
        // Displays data on screen
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView tvFc = (TextView)findViewById(R.id.tvFacilityCodeValue);
                TextView tvCn = (TextView)findViewById(R.id.tvCardNumberValue);

                tvFc.setText(Integer.toString(tag.getFc()));
                tvCn.setText(Integer.toString(tag.getCardNumber()));

                if (updateList) {
                    adapter.add(tag);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void showPlay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_24dp));
            }
        });

        displayStatus(Status.off);
    }

    @Override
    public void showWaitingForCard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_24dp));
                    }
                });
                displayStatus(Status.waiting);
            }
        });
    }

    @Override
    public void showReadingCard() {
        displayStatus(Status.reading);
    }

    @Override
    public void showNbReads(final int nbReads) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvNbReadsValue = (TextView)findViewById(R.id.tvNbReadsValue);
                tvNbReadsValue.setText(Integer.toString(nbReads));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView tvDeviceValue= (TextView)findViewById(R.id.tvDeviceValue);
        tvDeviceValue.setText(rfidPresenter.getCurrentDevice());
    }

    @Override
    protected void onStop() {
        super.onStop();
        rfidPresenter.readTags(false);
    }

    private enum Status {
        off,
        waiting,
        reading
    }

    private void displayStatus(Status state) {

        int imageId = 0;
        int stringId = 0;

        switch (state) {
            case off:
                imageId = R.drawable.ic_close_24dp;
                stringId = R.string.status_off;
                break;
            case waiting:
                imageId = R.drawable.ic_hourglass_full_black_24dp;
                stringId = R.string.status_waiting_for_card;
                break;
            case reading:
                imageId = R.drawable.ic_phonelink_ring_black_24dp;
                stringId = R.string.status_reading;
                break;
        }

        final int imageIdFinal = imageId;
        final int stringIdFinal = stringId;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvStatusValue = (TextView)findViewById(R.id.tvStatusValue);
                tvStatusValue.setText(stringIdFinal);

                ImageView ivStatus = (ImageView)findViewById(R.id.ivStatus);
                ivStatus.setImageDrawable(getResources().getDrawable(imageIdFinal));
            }
        });
    }

    private void clearReadTags () {
        rfidPresenter.resetTagsRead();
        adapter.clear();
        adapter.notifyDataSetChanged();
        TextView tvNbReads = (TextView)findViewById(R.id.tvNbReadsValue);
        tvNbReads.setText("0");
        TextView tvFc = (TextView)findViewById(R.id.tvFacilityCodeValue);
        TextView tvCn = (TextView)findViewById(R.id.tvCardNumberValue);

        tvFc.setText(R.string.no_card_read_yet);
        tvCn.setText(R.string.no_card_read_yet);
    }

    private static final String TAG = "MainActivity";

    private void showVersion() {
        try {
            this.setTitle(getString(R.string.app_name) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            Log.d(TAG, CpcOs.getSystemServicePackage(this));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showSettings() {
        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }
}
