package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import hashed.app.ampassadors.Objects.PollItem;
import hashed.app.ampassadors.R;

public class PollItemsAdapter extends ArrayAdapter<PollItem> {

  private static final int TYPE_SELECTED = 1,TYPE_UNSELECTED = 2;

  private static class ViewHolder {
    EditText pollEd;
    ImageView pollAddIv;
  }


  public PollItemsAdapter(@NonNull Context context, int resource, ArrayList<PollItem> pollItems) {
    super(context, resource,pollItems);
  }


  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

    Log.d("ttt","getCount(: "+getCount());
    PollItem pollItem = getItem(position);

    ViewHolder viewHolder;

    if(convertView == null){

      viewHolder = new ViewHolder();

      convertView = LayoutInflater.from(getContext()).
              inflate(R.layout.poll_item_layout, parent, false);

      viewHolder.pollEd =  convertView.findViewById(R.id.pollEd);
      viewHolder.pollAddIv =  convertView.findViewById(R.id.pollAddIv);

      convertView.setTag(viewHolder);
    }else{
      viewHolder = (ViewHolder) convertView.getTag();
    }


    viewHolder.pollEd.setHint(pollItem.getChoice());

    if(position == getCount() - 1){
      viewHolder.pollAddIv.setVisibility(View.VISIBLE);
      viewHolder.pollAddIv.setOnClickListener(v-> addPollItem(position+1));

    }else{
      viewHolder.pollAddIv.setVisibility(View.INVISIBLE);
      viewHolder.pollAddIv.setOnClickListener(null);
    }

    return convertView;
  }



  private void addPollItem(int position){

    String pollText = "الخيار "+ (position+1);
    add(new PollItem(pollText,false));

  }
//  @Override
//  public int getItemViewType(int position) {
//
//      return getItem(position).isSelected()?TYPE_SELECTED:TYPE_SELECTED;
//
//  }
}
