package net.mvla.mvhs.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import net.mvla.mvhs.R;

public class FeedbackDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        builder.title(R.string.feedback_type)
                .items(R.array.bugs)
                .itemsCallbackMultiChoice(new Integer[]{0}, (dialog, which, text) -> {
                    Intent send = new Intent(Intent.ACTION_SENDTO);

                    boolean bugs = false;
                    boolean generalFeedback = false;

                    for (int index : which) {
                        if (index == 0) {
                            generalFeedback = true;
                        } else {
                            bugs = true;
                        }
                    }


                    send.setData(Uri.parse("mailto:"));
                    String generalFeedbackString = generalFeedback ? getString(R.string.general_feedback) : "";
                    String bugsString = bugs ? getString(R.string.bug_report) : "";
                    send.putExtra(Intent.EXTRA_SUBJECT, "[MVHS App] " + generalFeedbackString + (bugsString.isEmpty() ? "" : ", ") + bugsString);
                    send.putExtra(Intent.EXTRA_TEXT, bugs ? "Type:\n" + TextUtils.join("\n", text) : "");
                    send.putExtra(Intent.EXTRA_EMAIL, new String[]{"pluscubed@gmail.com", "ly.nguyen@mvla.net"});
                    startActivity(send);

                    return true;
                })
                .positiveText(android.R.string.ok);
        return builder.build();
    }

}
