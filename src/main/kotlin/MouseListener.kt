
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.event.MouseInputAdapter


enum class MouseClickType {
    MOUSE_MOVE,
    MOUSE_CLICK_PRIMARY_DOWN,
    MOUSE_CLICK_SECONDARY_DOWN,
    MOUSE_CLICK_PRIMARY_UP,
    MOUSE_CLICK_SECONDARY_UP,
    MOUSE_CLICK_PRIMARY_DRAG
}

data class MouseClick(val type: MouseClickType, val point: Point)

@ExperimentalCoroutinesApi
val mouseClickChannel = ConflatedBroadcastChannel<MouseClick>()

@ExperimentalCoroutinesApi
val mouseClickAdapter = object : MouseInputAdapter() {

    override fun mousePressed(e: MouseEvent?) {
        super.mousePressed(e)
        when (e?.button) {
            MouseEvent.BUTTON1 -> {
                mouseClickChannel.offer(MouseClick(MouseClickType.MOUSE_CLICK_PRIMARY_DOWN, e.point))
            }
            MouseEvent.BUTTON2 -> {
                mouseClickChannel.offer(MouseClick(MouseClickType.MOUSE_CLICK_SECONDARY_DOWN, e.point))
            }
        }
    }

    override fun mouseReleased(e: MouseEvent?) {
        super.mouseReleased(e)

        when (e?.button) {
            MouseEvent.BUTTON1 -> {
                mouseClickChannel.offer(MouseClick(MouseClickType.MOUSE_CLICK_PRIMARY_UP, e.point))
            }
            MouseEvent.BUTTON2 -> {
                mouseClickChannel.offer(MouseClick(MouseClickType.MOUSE_CLICK_SECONDARY_UP, e.point))
            }
        }
    }

    override fun mouseMoved(e: MouseEvent?) {
        super.mouseMoved(e)
        mouseClickChannel.offer(MouseClick(MouseClickType.MOUSE_MOVE, e!!.point))
    }

    override fun mouseDragged(e: MouseEvent?) {
        super.mouseDragged(e)
        mouseClickChannel.offer(MouseClick(MouseClickType.MOUSE_CLICK_PRIMARY_DRAG, e!!.point))
    }
}

