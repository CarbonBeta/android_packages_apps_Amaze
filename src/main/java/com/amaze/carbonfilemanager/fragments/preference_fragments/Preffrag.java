/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 *                      Emmanuel Messulam <emmanuelbendavid@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.carbonfilemanager.fragments.preference_fragments;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.carbonfilemanager.BuildConfig;
import com.amaze.carbonfilemanager.R;
import com.amaze.carbonfilemanager.activities.AboutActivity;
import com.amaze.carbonfilemanager.activities.PreferencesActivity;
import com.amaze.carbonfilemanager.ui.views.preference.CheckBox;
import com.amaze.carbonfilemanager.utils.MainActivityHelper;
import com.amaze.carbonfilemanager.utils.PreferenceUtils;
import com.amaze.carbonfilemanager.utils.TinyDB;
import com.amaze.carbonfilemanager.utils.provider.UtilitiesProviderInterface;
import com.amaze.carbonfilemanager.utils.theme.AppTheme;

import java.util.ArrayList;

import static com.amaze.carbonfilemanager.R.string.feedback;
import static com.amaze.carbonfilemanager.fragments.preference_fragments.FoldersPref.castStringListToTrioList;

public class Preffrag extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String PREFERENCE_KEY_ABOUT = "about";
    private static final String[] PREFERENCE_KEYS =
            {"columns", "theme", "sidebar_folders_enable", "sidebar_quickaccess_enable",
                    "rootmode", "showHidden", "feedback", PREFERENCE_KEY_ABOUT, "plus_pic", "colors",
                    "sidebar_folders", "sidebar_quickaccess", "advancedsearch"};


    public static final String PREFERENCE_SHOW_SIDEBAR_FOLDERS = "show_sidebar_folders";
    public static final String PREFERENCE_SHOW_SIDEBAR_QUICKACCESSES = "show_sidebar_quickaccesses";

    public static final String PREFERENCE_SHOW_HIDDENFILES = "showHidden";

    public static final String PREFERENCE_ROOTMODE = "rootmode";

    public static final String PREFERENCE_CRYPT_MASTER_PASSWORD = "crypt_password";
    public static final String PREFERENCE_CRYPT_FINGERPRINT = "crypt_fingerprint";
    public static final String PREFERENCE_CRYPT_WARNING_REMEMBER = "crypt_remember";

    public static final String PREFERENCE_CRYPT_MASTER_PASSWORD_DEFAULT = "";
    public static final boolean PREFERENCE_CRYPT_FINGERPRINT_DEFAULT = false;
    public static final boolean PREFERENCE_CRYPT_WARNING_REMEMBER_DEFAULT = false;
    public static final String ENCRYPT_PASSWORD_FINGERPRINT = "fingerprint";
    public static final String ENCRYPT_PASSWORD_MASTER = "master";

    private UtilitiesProviderInterface utilsProvider;
    private SharedPreferences sharedPref;
    private CheckBox gplus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utilsProvider = (UtilitiesProviderInterface) getActivity();

        PreferenceUtils.reset();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        for (String PREFERENCE_KEY : PREFERENCE_KEYS) {
            findPreference(PREFERENCE_KEY).setOnPreferenceClickListener(this);
        }

        gplus = (CheckBox) findPreference("plus_pic");

        if (BuildConfig.IS_VERSION_FDROID)
            gplus.setEnabled(false);

        // crypt master password
        final EditTextPreference masterPasswordPreference = (EditTextPreference) findPreference(PREFERENCE_CRYPT_MASTER_PASSWORD);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // encryption feature not available
            masterPasswordPreference.setEnabled(false);
        }

        if (sharedPref.getBoolean(PREFERENCE_CRYPT_FINGERPRINT, false)) {
            masterPasswordPreference.setEnabled(false);
        }

        CheckBox checkBoxFingerprint = (CheckBox) findPreference(PREFERENCE_CRYPT_FINGERPRINT);

        try {

            // finger print sensor
            final FingerprintManager fingerprintManager = (FingerprintManager)
                    getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

            final KeyguardManager keyguardManager = (KeyguardManager)
                    getActivity().getSystemService(Context.KEYGUARD_SERVICE);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fingerprintManager.isHardwareDetected()) {

                checkBoxFingerprint.setEnabled(true);
            }

            checkBoxFingerprint.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.crypt_fingerprint_no_permission),
                                Toast.LENGTH_LONG).show();
                        return false;
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            !fingerprintManager.hasEnrolledFingerprints()) {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.crypt_fingerprint_not_enrolled),
                                Toast.LENGTH_LONG).show();
                        return false;
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            !keyguardManager.isKeyguardSecure()) {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.crypt_fingerprint_no_security),
                                Toast.LENGTH_LONG).show();
                        return false;
                    }

                    masterPasswordPreference.setEnabled(false);
                    return true;
                }
            });
        } catch (NoClassDefFoundError error) {
            error.printStackTrace();

            // fingerprint manager class not defined in the framework
            checkBoxFingerprint.setEnabled(false);
        }

        // Hide root preference
        Preference mRootMode = findPreference("rootmode");
        PreferenceCategory mMisc = (PreferenceCategory) findPreference("misc");
        mMisc.removePreference(mRootMode);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String[] sort;
        MaterialDialog.Builder builder;

        switch (preference.getKey()) {
            case "columns":
                sort = getResources().getStringArray(R.array.columns);
                builder = new MaterialDialog.Builder(getActivity());
                builder.theme(utilsProvider.getAppTheme().getMaterialDialogTheme());
                builder.title(R.string.gridcolumnno);
                int current = Integer.parseInt(sharedPref.getString("columns", "-1"));
                current = current == -1 ? 0 : current;
                if (current != 0) current = current - 1;
                builder.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        sharedPref.edit().putString("columns", "" + (which != 0 ? sort[which] : "" + -1)).commit();
                        dialog.dismiss();
                        return true;
                    }
                });
                builder.build().show();
                return true;
            case "theme":
                sort = getResources().getStringArray(R.array.theme);
                current = Integer.parseInt(sharedPref.getString("theme", "0"));
                builder = new MaterialDialog.Builder(getActivity());
                //builder.theme(utilsProvider.getAppTheme().getMaterialDialogTheme());
                builder.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        utilsProvider.getThemeManager()
                                .setAppTheme(AppTheme.fromIndex(which))
                                .save();

                        Log.d("theme", AppTheme.fromIndex(which).name());

                        dialog.dismiss();
                        restartPC(getActivity());
                        return true;
                    }
                });
                builder.title(R.string.theme);
                builder.build().show();
                return true;
            case "sidebar_folders_enable":
                sharedPref.edit().putBoolean(PREFERENCE_SHOW_SIDEBAR_FOLDERS,
                        !sharedPref.getBoolean(PREFERENCE_SHOW_SIDEBAR_FOLDERS, true)).apply();
                return true;
            case "sidebar_quickaccess_enable":
                sharedPref.edit().putBoolean(PREFERENCE_SHOW_SIDEBAR_QUICKACCESSES,
                        !sharedPref.getBoolean(PREFERENCE_SHOW_SIDEBAR_QUICKACCESSES, true)).apply();
                return true;
            case PREFERENCE_SHOW_HIDDENFILES:
                setEnabledShortcuts();
                return false;
            case PREFERENCE_ROOTMODE:
                setEnabledShortcuts();

                /*
                boolean b = sharedPref.getBoolean("rootmode", false);
                if (b) {
                    if (MainActivity.shellInteractive.isRunning()) {
                        rootmode.setChecked(true);

                    } else {  rootmode.setChecked(false);

                        Toast.makeText(getActivity(), getResources().getString(R.string.rootfailure), Toast.LENGTH_LONG).show();
                    }
                } else {
                    rootmode.setChecked(false);

                }
                */
                return false;
            case "feedback":
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "vishalmeham2@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback : Amaze File Manager");
                startActivity(Intent.createChooser(emailIntent, getResources().getString(feedback)));
                return false;
            case PREFERENCE_KEY_ABOUT:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return false;
            case "plus_pic":
                if(gplus.isChecked()){
                    boolean b= MainActivityHelper.checkAccountsPermission(getActivity());
                    if(!b) MainActivityHelper.requestAccountsPermission(getActivity());
                }
                return false;
            /*FROM HERE BE FRAGMENTS*/
            case "colors":
                ((PreferencesActivity) getActivity())
                        .selectItem(PreferencesActivity.COLORS_PREFERENCE);
                return true;
            case "sidebar_folders":
                ((PreferencesActivity) getActivity())
                        .selectItem(PreferencesActivity.FOLDERS_PREFERENCE);
                return true;
            case "sidebar_quickaccess":
                ((PreferencesActivity) getActivity())
                        .selectItem(PreferencesActivity.QUICKACCESS_PREFERENCE);
                return true;
            case "advancedsearch":
                ((PreferencesActivity) getActivity())
                        .selectItem(PreferencesActivity.ADVANCEDSEARCH_PREFERENCE);
                return true;
        }

        return false;
    }

    public static void restartPC(final Activity activity) {
        if (activity == null) return;

        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }

    public void invalidateGplus(){
        boolean a=MainActivityHelper.checkAccountsPermission(getActivity());
        if(!a)gplus.setChecked(false);
    }

    /**
     * Dynamically enables autodisabled shortcuts, and disables inaccessible shortcuts when user
     * changes root access and hidden files visibility preferences.
     */
    private void setEnabledShortcuts() {
        ArrayList<String> predigestedPref = TinyDB.getList(sharedPref, String.class, FoldersPref.KEY, null);
        if(predigestedPref == null) return;

        ArrayList<FoldersPref.Shortcut> currentValue = castStringListToTrioList(predigestedPref);

        for(int i = 0; i < currentValue.size(); i++) {
            if(FoldersPref.canShortcutTo(currentValue.get(i).directory, sharedPref)
                    && currentValue.get(i).autodisabled) {
                FoldersPref.Shortcut shortcut = new FoldersPref.Shortcut(currentValue.get(i).name,
                        currentValue.get(i).directory, FoldersPref.Shortcut.TRUE);
                currentValue.set(i, shortcut);
            } else if (!FoldersPref.canShortcutTo(currentValue.get(i).directory, sharedPref)
                    && currentValue.get(i).enabled) {
                FoldersPref.Shortcut shortcut = new FoldersPref.Shortcut(currentValue.get(i).name,
                        currentValue.get(i).directory, FoldersPref.Shortcut.AUTOFALSE);
                currentValue.set(i, shortcut);
            }
        }

        TinyDB.putList(sharedPref, FoldersPref.KEY, FoldersPref.castTrioListToStringList(currentValue));
    }

}
