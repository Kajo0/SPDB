package elka.pw.edu.pl.spdb;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;


public class MainActivity extends ActionBarActivity {

    private EditText sourceAddressEditText;
    private EditText targetAddressEditText;

    private FindRouteTask findRouteTask = null;
    private FindTransitTask findTransitTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceAddressEditText = (EditText) findViewById(R.id.main_address_source);
        targetAddressEditText = (EditText) findViewById(R.id.main_address_target);
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

        findRouteTask = new FindRouteTask(this);
        findRouteTask.execute(getSourceAddressEditText(), getTargetAddressEditText());
    }

    public void findTransit(View view) {
        if (!checkEmptyFields()) {
            return;
        }

        if (findTransitTask != null) {
            findTransitTask.cancel(true);
        }

        findTransitTask = new FindTransitTask(this);
        findTransitTask.execute(getSourceAddressEditText(), getTargetAddressEditText());
    }

    private void showMap(String resp) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.EXTRA_PARAM, resp);
        startActivity(intent);
    }

    class FindRouteTask extends AsyncTask<String, Void, String> {

        private Context ctx;

        public FindRouteTask(final Context ctx) {
            super();
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(final String... params) {
            return Util.requestRoute(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(final String resp) {
            String msg = null;

            if (resp == null) {
                msg = ctx.getString(R.string.error_unknown);
            } else {
                msg = resp;
            }

            final String message = msg;
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                    if (StringUtils.isNoneEmpty(resp)) {
                        showMap(resp);
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

    class FindTransitTask extends AsyncTask<String, Void, String> {

        private Context ctx;

        public FindTransitTask(final Context ctx) {
            super();
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(final String... params) {
            // TODO time
            return Util.requestTransit(params[0], params[1], System.currentTimeMillis());
        }

        @Override
        protected void onPostExecute(final String resp) {
            String msg = null;

            if (resp == null) {
                msg = ctx.getString(R.string.error_unknown);
            } else {
                msg = resp;
            }

            final String message = msg;
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                    if (StringUtils.isNoneEmpty(resp)) {
                        showMap(resp);
                    }
                }
            });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            findTransitTask = null;
        }
    }

}
