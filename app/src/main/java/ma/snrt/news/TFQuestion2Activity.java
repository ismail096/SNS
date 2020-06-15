package ma.snrt.news;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;

import ma.snrt.news.network.ApiCall;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TFQuestion2Activity extends AppCompatActivity {
    AppCompatEditText nameEditText, emailEditText;
    String question = "";
    boolean isSendError, isMsgLayoutVisible;
    RelativeLayout msgLayout;
    ImageView msgImageView;
    TextViewBold msgTextView, msgBtn;

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

        msgLayout = findViewById(R.id.msg_layout);
        msgImageView = findViewById(R.id.msg_imageview);
        msgTextView = findViewById(R.id.msg_textview);
        msgBtn = findViewById(R.id.msg_btn);

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
                    //Toast.makeText(TFQuestion2Activity.this, getString(R.string.quest_send_success), Toast.LENGTH_SHORT).show();
                    isSendError = false;
                }
                else {
                    //Toast.makeText(TFQuestion2Activity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                    isSendError = true;
                }
                showMsgLayout();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(TFQuestion2Activity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                isSendError = true;
                showMsgLayout();
            }
        });
    }

    public void showMsgLayout(){
        msgLayout.setVisibility(View.VISIBLE);
        if(isSendError){
            msgImageView.setImageResource(R.drawable.close);
            msgTextView.setText(getString(R.string.tf_error_send));
            msgBtn.setText(getString(R.string.retry));
            msgBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        else{
            msgImageView.setImageResource(R.drawable.tick);
            msgTextView.setText(getString(R.string.tf_success_send));
            msgBtn.setText(getString(R.string.ok));
            msgBtn.setTextColor(ContextCompat.getColor(this, R.color.true_fake_color));
        }
        isMsgLayoutVisible = true;
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
        else if(view.getId() == R.id.msg_btn){
            if(isSendError)
            {
                msgLayout.setVisibility(View.GONE);
                isMsgLayoutVisible = false;
            }
            else {
                setResult(RESULT_OK);
                finish();
            }
        }
        else if(view.getId() == R.id.msg_layout){
            msgLayout.setVisibility(View.GONE);
            isMsgLayoutVisible = false;
        }
    }

    @Override
    public void onBackPressed() {
        if(isMsgLayoutVisible)
        {
            msgLayout.setVisibility(View.GONE);
            isMsgLayoutVisible = false;
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
