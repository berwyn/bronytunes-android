package com.bronytunes.app.ui.logs;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bronytunes.app.R;
import com.bronytunes.app.data.LumberYard;
import com.bronytunes.app.ui.misc.BindableAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class LogAdapter extends BindableAdapter<LumberYard.Entry> implements Action1<LumberYard.Entry> {
  private List<LumberYard.Entry> logs;

  public LogAdapter(Context context) {
    super(context);
    logs = Collections.emptyList();
  }

  public void setLogs(List<LumberYard.Entry> logs) {
    this.logs = new ArrayList<>(logs);
  }

  @Override
  public void call(LumberYard.Entry entry) {
    logs.add(entry);
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return logs.size();
  }

  @Override
  public LumberYard.Entry getItem(int i) {
    return logs.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View newView(LayoutInflater inflater, int position, ViewGroup container) {
    View view = inflater.inflate(R.layout.debug_logs_list_item, container, false);
    LogItemViewHolder viewHolder = new LogItemViewHolder(view);
    view.setTag(viewHolder);
    return view;
  }

  @Override public void bindView(LumberYard.Entry item, int position, View view) {
    LogItemViewHolder viewHolder = (LogItemViewHolder) view.getTag();
    viewHolder.setEntry(item);
  }

  static final class LogItemViewHolder {
    private final View rootView;
    @Bind(R.id.debug_log_level) TextView levelView;
    @Bind(R.id.debug_log_tag) TextView tagView;
    @Bind(R.id.debug_log_message) TextView messageView;

    public LogItemViewHolder(View rootView) {
      this.rootView = rootView;
      ButterKnife.bind(this, rootView);
    }

    public void setEntry(LumberYard.Entry entry) {
      rootView.setBackgroundResource(backgroundForLevel(entry.level));
      levelView.setText(entry.displayLevel());
      tagView.setText(entry.tag);
      messageView.setText(entry.message);
    }
  }

  public static @DrawableRes int backgroundForLevel(int level) {
    switch (level) {
      case Log.VERBOSE:
      case Log.DEBUG:
        return R.color.debug_log_accent_debug;
      case Log.INFO:
        return R.color.debug_log_accent_info;
      case Log.WARN:
        return R.color.debug_log_accent_warn;
      case Log.ERROR:
      case Log.ASSERT:
        return R.color.debug_log_accent_error;
      default:
        return R.color.debug_log_accent_unknown;
    }
  }
}
