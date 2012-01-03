package hey.hoop;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class ListDbActivity extends ListActivity {
	private static final long MAX_ENTRIES = 100;
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
		Cursor c = dbAdapter.fetchEntries(MAX_ENTRIES);
		startManagingCursor(c);
		String[] from = new String[] { HHDbAdapter.MAIN_COLUMN_ID,
				HHDbAdapter.MAIN_COLUMN_VALUE, HHDbAdapter.MAIN_COLUMN_DATE };
		int[] to = new int[] { R.id.idField, R.id.valueField, R.id.dateField };
		// String[] from = new String[] { HHDbAdapter.SIDE_COLUMN_ID,
		// HHDbAdapter.SIDE_COLUMN_VALUE };
		// int[] to = new int[] { R.id.idField, R.id.valueField };
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.entry_row, c, from, to);
		setListAdapter(sca);
	}
}
