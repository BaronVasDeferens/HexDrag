import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GameMaster (private val hexMap: HexMap){

    init {
        GlobalScope.launch {
            mouseClickChannel.asFlow().onEach {click ->

                when (click.type) {
                    MouseClickType.MOUSE_CLICK_PRIMARY_DOWN -> {
                        hexMap.getHexAtClick(click.point.x, click.point.y)?.apply {
                            println(this)
                        }
                    }

                    else -> {

                    }
                }

            }.launchIn(this)
        }
    }


}