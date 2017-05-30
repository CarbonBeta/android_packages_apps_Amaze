package com.amaze.carbonfilemanager.utils.provider;

import com.amaze.carbonfilemanager.utils.files.Futils;
import com.amaze.carbonfilemanager.utils.color.ColorPreference;
import com.amaze.carbonfilemanager.utils.theme.AppTheme;
import com.amaze.carbonfilemanager.utils.theme.AppThemeManagerInterface;

/**
 * Created by Rémi Piotaix <remi.piotaix@gmail.com> on 2016-10-17.
 */
public interface UtilitiesProviderInterface {
    Futils getFutils();

    ColorPreference getColorPreference();

    AppTheme getAppTheme();

    AppThemeManagerInterface getThemeManager();
}
