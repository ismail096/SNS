package ma.snrt.news;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonObject;

import ma.snrt.news.network.ApiCall;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TFQuestion2Activity extends AppCompatActivity {
    AppCompatEditText nameEditText, emailEditText;
    String question = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        setContentView(R.layout.activity_tf_question2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        nameEditText = findViewById(R.id.tf_name_edit);
        emailEditText = findViewById(R.id.tf_email_edit);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

    }

    private void sendQuestion(){
        String email = emailEditText.getText().toString();
        String name = nameEditText.getText().toString();
        ApiCall.sendQuestion(question, email, name, new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful() && response.body() != null){
                    Toast.makeText(TFQuestion2Activity.this, getString(R.string.quest_send_success), Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(TFQuestion2Activity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(TFQuestion2Activity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick(View view){
        if(view.getId() == R.id.back_btn)
            finish();
        else if(view.getId() == R.id.close_btn){
            setResult(RESULT_OK);
            finish();
        }
        else if(view.getId() == R.id.submit_btn)
        {
            sendQuestion();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
