package hey.hoop.custom_view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import hey.hoop.HHDbAdapter;
import hey.hoop.R;

public class WellbeingStatusView extends View {
    private FetchWellbeing fetchWellbeing;

    public WellbeingStatusView(Context context) {
        this(context, null, 0);
    }

    public WellbeingStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WellbeingStatusView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        inflate(context, R.layout.wellbeing_status, null);
    }

    public void setFetchWellbeing(FetchWellbeing fetch) {
        this.fetchWellbeing = fetch;
    }

    public void setText(CharSequence text) {
        ((TextView) findViewById(R.id.wellbeing_status_text)).setText(text);
    }

    public void setText(int resId) {
        setText(getResources().getString(resId));
    }

    public void refetch() {
        post(new Runnable() {
            @Override
            public void run() {
                if (fetchWellbeing != null) {
                    HHDbAdapter.Wellbeing wellbeing = fetchWellbeing.fetch();
                    findViewById(R.id.wellbeing_status_text).setBackgroundColor(translateWellbeing(wellbeing));
                    invalidate();
                }
            }
        });
    }

    private int translateWellbeing(HHDbAdapter.Wellbeing wellbeing) {
        switch (wellbeing) {
            case FATAL:
                return getResources().getColor(R.color.wellbeing_fatal);
            case POOR:
                return getResources().getColor(R.color.wellbeing_poor);
            case GOOD:
                return getResources().getColor(R.color.wellbeing_good);
            default:
                return Color.BLUE;
        }
    }

    public interface FetchWellbeing {
        public HHDbAdapter.Wellbeing fetch();
    }
}
