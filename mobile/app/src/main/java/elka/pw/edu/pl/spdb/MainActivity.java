package elka.pw.edu.pl.spdb;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.internal.ge;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private AutoCompleteTextView sourceAddressEditText;
    private AutoCompleteTextView targetAddressEditText;

    private FindRouteTask findRouteTask = null;
    private ProgressDialog progressDialog;

    private SimpleDateFormat dateFormat;
    private Date date;

    private EditText dateEditView;
    private CheckBox arrivalTimeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceAddressEditText = (AutoCompleteTextView) findViewById(R.id.main_address_source);
        targetAddressEditText = (AutoCompleteTextView) findViewById(R.id.main_address_target);
        dateEditView = (EditText) findViewById(R.id.dateTimeText);
        arrivalTimeCheckBox = (CheckBox) findViewById(R.id.arrivalTimeCheckbox);

        PlacesAutoCompleteAdapter adapter = new PlacesAutoCompleteAdapter(this, R.layout.list_item, 3);
        sourceAddressEditText.setAdapter(adapter);
        targetAddressEditText.setAdapter(adapter);

        dateFormat = new SimpleDateFormat("d/MM/y k:mm");
        date = new Date();

        dateEditView.setText(dateFormat.format(date));
        /*sourceAddressEditText.setText("Cyprysowa 9, Warszawa");
        targetAddressEditText.setText("Politechnika, Warszawa");*/
    }

    protected void onStop () {
        super.onStop();
        dismissProgressDialog();
    }

    private String getSourceAddressEditText() {
        return sourceAddressEditText.getText().toString();
    }

    private String getTargetAddressEditText() {
        return targetAddressEditText.getText().toString();
    }

    private boolean checkEmptyFields() {
        return !StringUtils.isAnyEmpty(getSourceAddressEditText(), getTargetAddressEditText());
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void findRoute(View view) {
        if (!checkEmptyFields()) {
            return;
        }

        if (findRouteTask != null) {
            findRouteTask.cancel(true);
        }

        progressDialog = ProgressDialog.show(this, getString(R.string.finding_route),
                getString(R.string.please_wait), true);
        findRouteTask = new FindRouteTask(this);
        findRouteTask.execute(getSourceAddressEditText(), getTargetAddressEditText());
    }

    private void showMap(Pair<String, String> resp) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.DRIVE_PARAM, resp.first);
        intent.putExtra(MapsActivity.TRANSIT_PARAM, resp.second);
        intent.putExtra(MapsActivity.FROM_PARAM, getSourceAddressEditText());
        intent.putExtra(MapsActivity.TO_PARAM, getTargetAddressEditText());
        startActivity(intent);
    }

    class FindRouteTask extends AsyncTask<String, Void, Pair<String, String>> {

        private Context ctx;

        public FindRouteTask(final Context ctx) {
            super();
            this.ctx = ctx;
        }

        @Override
        protected Pair<String, String> doInBackground(final String... params) {
            return new Pair<String, String>(Util.requestRoute(true, params[0], params[1], date.getTime(), arrivalTimeCheckBox.isChecked()),
                    Util.requestRoute(false, params[0], params[1], date.getTime(), arrivalTimeCheckBox.isChecked()));
        }

        @Override
        protected void onPostExecute(final Pair<String, String> resp) {
            final String msg = ctx.getString(R.string.error_unknown);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (StringUtils.isNoneEmpty(resp.first) && StringUtils.isNoneEmpty(resp.second)) {
                        showMap(resp);
                    }
                    else {
                        dismissProgressDialog();
                        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            findRouteTask = null;
        }
    }

    public void onDateTimeClicked(View view) {
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        final DatePicker datePicker = new DatePicker(this);
        datePicker.setCalendarViewShown(false);
        final TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(true);

        layout.addView(datePicker);
        layout.addView(timePicker);

        datePicker.updateDate(date.getYear() + 1900, date.getMonth(), date.getDate());
        timePicker.setCurrentHour(date.getHours());
        timePicker.setCurrentMinute(date.getMinutes());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setTitle(getString(R.string.date_time_title))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        date.setDate(datePicker.getDayOfMonth());
                        date.setMonth(datePicker.getMonth());
                        date.setYear(datePicker.getYear() - 1900);
                        date.setMinutes(timePicker.getCurrentMinute());
                        date.setHours(timePicker.getCurrentHour());
                        dateEditView.setText(dateFormat.format(date));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
