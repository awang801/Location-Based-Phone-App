package example.team5.samplelocation.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import example.team5.samplelocation.R;

/**
 * Created by johnw on 4/8/2017.
 */

public class GroupBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private String m_name;
    private String m_description;
    private int m_avatarID;
    private boolean m_isAdmin = false;

    final String TAG = "GroupBottomSheetDialogFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set private vars based on arguments grabbed from Dillon's groups ListView
        m_name = getArguments().getString("name");
        m_description = getArguments().getString("description");
        m_avatarID = getArguments().getInt("avatarID");

        if( getArguments().getInt("rank") == 1 ) {
            m_isAdmin = true;
        }
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View rootView = View.inflate(getContext(), R.layout.fragment_group_bottom_sheet, null);
        dialog.setContentView(rootView);

        // Set name, description, and avatar based on info from the groups tab
        TextView tv_name = (TextView) rootView.findViewById(R.id.tv_name);
        tv_name.setText(m_name);
        TextView tv_description = (TextView) rootView.findViewById(R.id.tv_description);
        tv_description.setText(m_description);
        ImageView iv_avatar = (ImageView) rootView.findViewById(R.id.iv_avatar);
        iv_avatar.setImageResource( Utils.getAvatarResIDFromAvatarID(m_avatarID) );

        // Set the colored top tab based on the avatar color
        LinearLayout layout_coloredTitle = (LinearLayout) rootView.findViewById(R.id.layout_coloredTitle);
        layout_coloredTitle.setBackgroundResource( Utils.getAvatarColorFromAvatarID(m_avatarID) );

        // Code to circularize avatars in case we need it in the future
//        Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_bluebird);
//        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), avatar);
//        roundDrawable.setCircular(true);
//        iv_avatar.setImageDrawable(roundDrawable);

        // Hide views meant only for admins if the user is not an admin
        if( !m_isAdmin ) {
            hideAdminViews(rootView);
        }

        // Set the create message button to spawn a message dialog
        LinearLayout layout_createMessage = (LinearLayout) rootView.findViewById(R.id.layout_createMessage);
        layout_createMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new MessageDialog();
                dialog.show(getActivity().getSupportFragmentManager(), "MessageDialog");
            }
        });


        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) rootView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    // This method hides views that we don't want to be displayed for members
    public void hideAdminViews(View rootView) {
        rootView.findViewById(R.id.card_admin).setVisibility(View.GONE);
    }

    public static class MessageDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Build the EditText to be used for input
            final LinearLayout inputLayout = (LinearLayout) View.inflate(getContext(), R.layout.dialog_message_input, null);
            final EditText et_input = (EditText) inputLayout.findViewById(R.id.et_message);

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Send a message to your group's feed:")
                    .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User selected positive button
                            String message = et_input.getText().toString();

                            // TODO: INSERT CONNECTION TO BACKEND TO SEND MESSAGE HERE AND REMOVE TOAST
                            Toast.makeText(getContext(), "Message: " + message, Toast.LENGTH_LONG).show();
                        }
                    });

            builder.setView(inputLayout);

            // Create and return the dialog
            return builder.create();
        }
    }
}
