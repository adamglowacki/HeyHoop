package hey.hoop;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class ListDbActivity extends ListActivity {
	HHDbAdapter dbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.db_list);
		dbAdapter = new HHDbAdapter(this);
		dbAdapter.open(false);
		fillData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
	}

	private void fillData() {
		Cursor c = dbAdapter.fetchWalk();
		startManagingCursor(c);
		String[] from = new String[] { HHDbAdapter.WALK_COLUMN_ID,
				HHDbAdapter.WALK_COLUMN_VALUE, HHDbAdapter.WALK_COLUMN_DATE};
		int[] to = new int[] { R.id.idField, R.id.valueField, R.id.dateField };
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.entry_row, c, from, to);
		setListAdapter(sca);
	}
}
