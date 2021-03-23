package defpackage.notestable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.dip
import java.lang.ref.WeakReference

class SwipeCallback(listener: Listener, context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val reference = WeakReference(listener)

    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)!!

    private val bgDrawable = ColorDrawable(Color.RED)

    private val iconMargin = context.dip(20)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        reference.get()?.onSwiped(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        with(viewHolder.itemView) {
            val iconTop = top + (height - icon.intrinsicHeight) / 2
            val iconBottom = iconTop + icon.intrinsicHeight
            when {
                dX > 0 -> { // swiping to the right
                    val iconLeft = left + iconMargin + icon.intrinsicWidth
                    val iconRight = left + iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    bgDrawable.setBounds(left, top, left + dX.toInt(), bottom)
                }
                dX < 0 -> { // swiping to the left
                    val iconLeft = right - iconMargin - icon.intrinsicWidth
                    val iconRight = right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    bgDrawable.setBounds(right + dX.toInt(), top, right, bottom)
                }
                else -> { // view is unswiped
                    bgDrawable.setBounds(0, 0, 0, 0)
                }
            }
            bgDrawable.draw(c)
            icon.draw(c)
        }
    }

    interface Listener {

        fun onSwiped(position: Int)
    }
}