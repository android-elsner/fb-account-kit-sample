package com.elsner.fbaccountkit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static int APP_REQUEST_CODE_PHONE = 23;
    public static int APP_REQUEST_CODE_MAIL = 93;


    private static final int TINT_SEEKBAR_ADJUSTMENT = 55;

    @BindView(R.id.seekTintIntensity)
    SeekBar seekTintIntensity;
    @BindView(R.id.tvSeekTintIntensityValue)
    TextView tvSeekTintIntensityValue;
    @BindView(R.id.rgSkin)
    RadioGroup rgSkin;
    @BindView(R.id.rgTintMode)
    RadioGroup rgTintMode;
    @BindView(R.id.llAccountKitAnonymous)
    LinearLayout llAccountKitAnonymous;
    @BindView(R.id.tvAccessToken)
    TextView tvAccessToken;
    @BindView(R.id.btnLogOut)
    Button btnLogOut;
    @BindView(R.id.llAccountKitLoggedIn)
    LinearLayout llAccountKitLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        if (accessToken != null) {
            //Handle Returning User
            llAccountKitLoggedIn.setVisibility(View.VISIBLE);
            llAccountKitAnonymous.setVisibility(View.GONE);
            String message = String.format(
                    "AccountID :%s...", accessToken.getAccountId());
            tvAccessToken.setText(message);
        } else {
            //Handle new or logged out User
            llAccountKitLoggedIn.setVisibility(View.GONE);
            llAccountKitAnonymous.setVisibility(View.VISIBLE);
        }

        seekTintIntensity.setProgress(0);
        tvSeekTintIntensityValue.setText((seekTintIntensity.getProgress() + TINT_SEEKBAR_ADJUSTMENT) + "%");

        seekTintIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvSeekTintIntensityValue.setText((seekBar.getProgress() + TINT_SEEKBAR_ADJUSTMENT) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @OnClick({R.id.btnPhoneLogin, R.id.btnEmailLogin, R.id.btnLogOut})
    public void onViewClicked(View view) {
        Intent intent;
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder;
        UIManager uiManager = getUIManager();
        switch (view.getId()) {
            case R.id.btnPhoneLogin:
                intent = new Intent(MainActivity.this, AccountKitActivity.class);
                configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE, AccountKitActivity.ResponseType.CODE);
                configurationBuilder.setUIManager(uiManager);
                intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                        configurationBuilder.build());
                startActivityForResult(intent, APP_REQUEST_CODE_PHONE);
                break;
            case R.id.btnEmailLogin:
                intent = new Intent(MainActivity.this, AccountKitActivity.class);
                configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.EMAIL, AccountKitActivity.ResponseType.CODE);
                configurationBuilder.setUIManager(uiManager);
                intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                        configurationBuilder.build());
                startActivityForResult(intent, APP_REQUEST_CODE_MAIL);
                break;
            case R.id.btnLogOut:
                AccountKit.logOut();
                llAccountKitLoggedIn.setVisibility(View.GONE);
                llAccountKitAnonymous.setVisibility(View.VISIBLE);
                break;
        }
    }


    private UIManager getUIManager() {

        SkinManager.Skin skin = null;
        switch (rgSkin.getCheckedRadioButtonId()) {
            case R.id.skinNone:
                skin = SkinManager.Skin.NONE;
                break;
            case R.id.skinClassic:
                skin = SkinManager.Skin.CLASSIC;
                break;
            case R.id.skinContemporary:
                skin = SkinManager.Skin.CONTEMPORARY;
                break;
            case R.id.skinTranslucent:
                skin = SkinManager.Skin.TRANSLUCENT;
                break;
        }

        SkinManager.Tint tint = null;
        switch (rgTintMode.getCheckedRadioButtonId()) {
            case R.id.tintBlack:
                tint = SkinManager.Tint.BLACK;
                break;
            case R.id.tintWhite:
                tint = SkinManager.Tint.WHITE;
                break;
        }


        return new SkinManager(
                skin,
                getResources().getColor(R.color.colorPrimaryDark),
                R.drawable.dog_back,
                tint,
                (double) (seekTintIntensity.getProgress() + TINT_SEEKBAR_ADJUSTMENT) / 100);
    }


    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE_PHONE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = String.format(
                            "AccountID :%s...",
                            loginResult.getAccessToken().getAccountId());
                } else {
                    toastMessage = String.format(
                            "AuthCode :%s...",
                            loginResult.getAuthorizationCode());
                }

                Toast.makeText(
                        this,
                        "Success" + " (Phone)",
                        Toast.LENGTH_LONG)
                        .show();


                llAccountKitLoggedIn.setVisibility(View.VISIBLE);
                llAccountKitAnonymous.setVisibility(View.GONE);
                tvAccessToken.setText(toastMessage);
            }
        } else if (requestCode == APP_REQUEST_CODE_MAIL) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = String.format(
                            "AccountID :%s...",
                            loginResult.getAccessToken().getAccountId());
                } else {
                    toastMessage = String.format(
                            "AuthCode :%s...",
                            loginResult.getAuthorizationCode());
                }

                Toast.makeText(
                        this,
                        "Success" + " (Email)",
                        Toast.LENGTH_LONG)
                        .show();


                llAccountKitLoggedIn.setVisibility(View.VISIBLE);
                llAccountKitAnonymous.setVisibility(View.GONE);
                tvAccessToken.setText(toastMessage);
            }

        }
    }




}
