//package hashed.app.ampassadors.Utils;
//
//import android.content.Context;
//import android.graphics.Rect;
//import android.view.View;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//public class GridSpacesItemDecoration extends RecyclerView.ItemDecoration {
//
//  private final int space,spanCount;
//  private final float density;
//  public GridSpacesItemDecoration(int space,int spanCount, Context context) {
//    this.space = space;
//    density = context.getResources().getDisplayMetrics().density;
//  }
//
//  @Override
//  public void getItemOffsets(Rect outRect, @NonNull View view,
//                             RecyclerView parent, @NonNull RecyclerView.State state) {
//
//     int position = parent.getChildLayoutPosition(view);
//     int column = position % 5;
//
//      if(position%5 == 0){
//      //first item from the left
//      outRect.right = space;
//    }else{
//
//
//    }
//
//    outRect.left = column * space / spanCount; // column * ((1f / spanCount) * spacing)
//    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
//    if (position >= spanCount) {
//      outRect.top = spacing; // item top
//    }
//
//    if(position > 5){
//      outRect.top = 40;
//    }
//
//    outRect.left = space;
//
//
////    outRect.bottom = space;
////
////    // Add top margin only for the first item to avoid double space between items
////    if (parent.getChildLayoutPosition(view) == 0) {
////      outRect.top = space;
////    } else {
////      outRect.top = 0;
////    }
//  }
//}