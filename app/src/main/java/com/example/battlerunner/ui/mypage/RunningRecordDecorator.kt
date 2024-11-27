import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import com.example.battlerunner.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class RunningRecordDecorator(
    private val context: Context,
    private val dates: Set<CalendarDay> // 기록이 있는 날짜 리스트
) : DayViewDecorator {

    private val drawable: Drawable = ContextCompat.getDrawable(context, (R.drawable.dot))!!

    // 해당 날짜를 데코레이트할지 여부
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    // 날짜(배경)에 점을 추가
    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
    }
}
