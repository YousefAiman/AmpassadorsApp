package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import hashed.app.ampassadors.Activities.MeetingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.HeaderItem;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class HomeHeaderViewPagerAdapter extends PagerAdapter implements View.OnClickListener {

  private final ArrayList<HeaderItem> headerItems;
  private String headerTitle = "";
  private final FirebaseFirestore firestore;

  public HomeHeaderViewPagerAdapter(ArrayList<HeaderItem> headerItems) {
    this.headerItems = headerItems;
    firestore = FirebaseFirestore.getInstance();
  }

  @Override
  public int getCount() {
    return headerItems.size();
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }

  @NonNull
  @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

    View view = ((LayoutInflater) container.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.home_header_item_design, null);

    final HeaderItem data = headerItems.get(position);

    final TextView headerTv = view.findViewById(R.id.headerTv);

    final Context context = container.getContext();
    if(data.getType().equals("news")){
      headerTv.setText(data.getTitle());
    }else{

      switch (data.getType()){


//        case "news":
//          headerTitle = context.getString(R.string.News);
//          break;
        case "poll":
          headerTitle = context.getString(R.string.poll);
          break;
        case "course":
          headerTitle = context.getString(R.string.course);
          break;
        case "meeting":
          headerTitle = context.getString(R.string.meeting);
          break;
      }

      headerTv.setText(headerTitle +": "+data.getTitle());
    }


    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = null;
      switch (data.getType()){

        case "news":
          intent = new Intent(container.getContext(), PostNewsActivity.class)
                  .putExtra("postId",data.getId());
          break;
        case "poll":
          intent = new Intent(container.getContext(), PostPollActivity.class)
                  .putExtra("postId",data.getId());
          break;
          case "course":

            final Course course = (Course) data.getObject();

            if(course.isHasEnded()){
              Toast.makeText(context, "This Course has ended!", Toast.LENGTH_SHORT).show();
              return;
            }

            Course.joinCourse(course,context);

//            intent = new Intent(container.getContext(), CourseActivity.class)
//                    .putExtra("course",(Course)data.getObject());
            break;
        case "meeting":
          intent = new Intent(container.getContext(), MeetingActivity.class)
                  .putExtra("meeting",(Meeting)data.getObject());
          break;

      }

      if(intent!=null){
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        container.getContext().startActivity(intent);
      }
      }
    });

    container.addView(view);
    return view;
  }


  private void startPostActivity(String id,Context context,String type){

    firestore.collection("Posts")
            .document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        if(documentSnapshot.exists()){
          context.startActivity(new Intent(context,
                  type.equals("news")?PostNewsActivity.class:PostPollActivity.class)
                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  .putExtra("postData",documentSnapshot.toObject(PostData.class)));
        }
      }
    });

  }

  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    container.removeView((View) object);
  }

//  @Override
  public int getItemPosition(@NonNull Object object) {
    return PagerAdapter.POSITION_NONE;
  }

  @Override
  public void onClick(View view) {}
}
