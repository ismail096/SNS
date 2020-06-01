package ma.snrt.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ma.snrt.news.ui.EditTextRegular;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;

public class TFQuestionActivity extends AppCompatActivity {

    EditTextRegular questionEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        setContentView(R.layout.activity_tf_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        questionEdit = findViewById(R.id.tf_quest_edit);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        questionEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        questionEdit.setRawInputType(InputType.TYPE_CLASS_TEXT);

        questionEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    Intent intent = new Intent(TFQuestionActivity.this, TFQuestion2Activity.class);
                    intent.putExtra("question", questionEdit.getText().toString());
                    startActivityForResult(intent, 1);
                    return true;
                }
                return false;
            }
        });
    }

    public void onClick(View view){
        if (view.getId() == R.id.back_btn || view.getId() == R.id.close_btn) {
            finish();
        } else if (view.getId() == R.id.next_btn) {
            Intent intent = new Intent(this, TFQuestion2Activity.class);
            intent.putExtra("question", questionEdit.getText().toString());
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode == RESULT_OK)
            finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
