package example.team5.samplelocation.main;

import android.graphics.drawable.Drawable;

import example.team5.samplelocation.R;

/**
 * Created by johnw on 4/10/2017.
 */

public class Utils {

    // Returns the Res ID for the given avatarID
    // Note that this doesn't return a drawable, but it does return the drawables Res ID
    // To set an image view to the appropriate avatar programmatically, use something like this:
    // "iv_avatar.setImageResource( Utils.getAvatarResIDFromAvatarID(m_avatarID) );"
    public static int getAvatarResIDFromAvatarID(int avatarID ) {
        switch( avatarID ) {
            case 0:
                return R.drawable.avatar_bluebird;
            default:
                return R.drawable.avatar_bluebird;
        }
    }

    // Returns the color that is to be associated with a given avatar
    public static int getAvatarColorFromAvatarID(int avatarID ) {
        switch( avatarID ) {
            case 0:
                return R.color.avatar_bluebird_color;
            default:
                return R.color.avatar_bluebird_color;
        }
    }
}
