package ru.megazlo.apnea.frag;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;

import ru.megazlo.apnea.R;

/** Created by iGurkin on 17.01.2017. */
@EFragment(R.layout.donate_layout)
public class DonateFragment extends Fragment implements FabClickListener {

	@StringRes(R.string.don_link)
	String donLink;

	@StringArrayRes(R.array.apnea_bros)
	String[] arrayBros;

	@ViewById(R.id.list_bros)
	ListView listBros;

	@AfterViews
	void init() {
		ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(), R.layout.donator_item, R.id.donator_name, arrayBros);
		listBros.setAdapter(adp);
	}

	@Click(R.id.btn_donate_paypal)
	void donatePaypal() {
		//"https://www.paypal.me/paradoxfm/"
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(donLink)));
	}

	@Override
	public void clickByContext(View view) {
	}

	@Override
	public void modifyToContext(View view) {
		view.setVisibility(View.GONE);
	}

	@Override
	public boolean backPressed() {
		return true;
	}
}
