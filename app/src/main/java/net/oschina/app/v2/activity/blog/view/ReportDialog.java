package net.oschina.app.v2.activity.blog.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.model.Report;
import net.oschina.app.v2.ui.dialog.CommonDialog;
import net.oschina.app.v2.utils.TDevice;

//import com.afollestad.materialdialogs.MaterialDialog;

public class ReportDialog extends CommonDialog implements
		View.OnClickListener {

	private static final int MAX_CONTENT_LENGTH = 250;
	private TextView mTvReason;
	private TextView mTvLink;
	private EditText mEtContent;
	private String[] reasons;
	private String mLink;
	private int mReportId;

	public ReportDialog(Context context, String link, int reportId) {
		this(context, R.style.dialog_common, link, reportId);
	}

	private ReportDialog(Context context, int defStyle, String link,
			int reportId) {
		super(context, defStyle);
		mLink = link;
		mReportId = reportId;
		initViews(context);
	}

	private ReportDialog(Context context, boolean flag,
			OnCancelListener listener) {
		super(context, flag, listener);
	}

	@SuppressLint("InflateParams")
	private void initViews(Context context) {
		reasons = getContext().getResources().getStringArray(
				R.array.report_reason);

		View view = getLayoutInflater()
				.inflate(R.layout.v2_dialog_report, null);
		setContent(view, 0);

		mTvReason = (TextView) view.findViewById(R.id.tv_reason);
		mTvReason.setOnClickListener(this);

		mTvReason.setText(reasons[0]);

		mTvLink = (TextView) view.findViewById(R.id.tv_link);
		mTvLink.setText(mLink);

		mEtContent = (EditText) view.findViewById(R.id.et_content);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.tv_reason) {
			selectReason();
		}
	}

	private void selectReason() {
		String reason = mTvReason.getText().toString();
		int idx = 0;
		for (int i = 0; i < reasons.length; i++) {
			if (reasons[i].equals(reason)) {
				idx = i;
				break;
			}
		}
//		final CommonDialog dialog = DialogHelper
//				.getPinterestDialogCancelable(getContext());
//		dialog.setTitle(R.string.report_reson);
//		dialog.setItems(reasons, idx, new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				dialog.dismiss();
//				mTvReason.setText(reasons[position]);
//			}
//		});
//		dialog.setNegativeButton(R.string.cancle, null);
//		dialog.show();

//        new MaterialDialog.Builder(getContext())
//                .title(R.string.report_reson)
//                .items(reasons)
//                .itemsCallbackSingleChoice(idx,new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        dialog.dismiss();
//                        mTvReason.setText(reasons[which]);
//                    }
//                })
//                .show();
        AlertDialog dialog = new AlertDialog.Builder(getContext(),
                R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(R.string.report_reson)
                .setCancelable(true)
                .setSingleChoiceItems(reasons,idx,new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mTvReason.setText(reasons[which]);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
	}

	public Report getReport() {
		String text = mEtContent.getText().toString();
		if (!TextUtils.isEmpty(text)) {
			if (text.length() > MAX_CONTENT_LENGTH) {
				AppContext
						.showToastShort(R.string.tip_report_other_reason_too_long);
				return null;
			}
		}
		TDevice.hideSoftKeyboard(mEtContent);
		Report report = new Report();
		report.setReportId(mReportId);
		report.setLinkAddress(mLink);
		report.setReason(mTvReason.getText().toString());
		report.setOtherReason(text);
		return report;
	}
}
