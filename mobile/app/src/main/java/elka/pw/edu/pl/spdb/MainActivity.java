package elka.pw.edu.pl.spdb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.util.Pair;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;


public class MainActivity extends ActionBarActivity {

    private EditText sourceAddressEditText;
    private EditText targetAddressEditText;

    private FindRouteTask findRouteTask = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceAddressEditText = (EditText) findViewById(R.id.main_address_source);
        targetAddressEditText = (EditText) findViewById(R.id.main_address_target);

        sourceAddressEditText.setText("Cyprysowa 9, Warszawa");
        targetAddressEditText.setText("Politechnika, Warszawa");
    }

    protected void onStop () {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
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
            return new Pair<String, String>(Util.requestRoute(params[0], params[1]),
                    Util.requestTransit(params[0], params[1], System.currentTimeMillis()));
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
}
