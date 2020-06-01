import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class GameMaster (private val hexMap: HexMap){

    init {
        GlobalScope.launch {
            mouseClickChannel.asFlow().onEach {click ->

                when (click.type) {
                    MouseClickType.MOUSE_CLICK_PRIMARY_DOWN -> {
                        hexMap.getHexAtClick(click.point.x, click.point.y)?.apply {
                            this.setSelected(!this.isSelected())
                            hexMap.publishBufferedImage()
                        }
                    }

                    else -> {

                    }
                }

            }.launchIn(this)
        }
    }


}