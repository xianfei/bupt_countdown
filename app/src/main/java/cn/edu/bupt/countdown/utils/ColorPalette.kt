package cn.edu.bupt.countdown.utils

import android.graphics.Color
import androidx.palette.graphics.Palette
import cn.edu.bupt.countdown.R

class ColorPalette(var colorPalette: Palette) {
    // 取色器
    // https://developer.android.com/training/material/palette-colors

    var vibrantSwatch:Palette.Swatch?

    var vibrantSwatch2:Palette.Swatch? = null;

    init {
        vibrantSwatch = colorPalette.lightVibrantSwatch
        vibrantSwatch2 = colorPalette.mutedSwatch
        if (vibrantSwatch == null) {
            for (swatch in colorPalette.swatches) {
                if (vibrantSwatch == null) {
                    vibrantSwatch = swatch

                }else{
                    vibrantSwatch2 = swatch
                    break;
                }
            }
        }
        if(vibrantSwatch2==null)
            for (swatch in colorPalette.swatches) {
                    vibrantSwatch2 = swatch
                    break;
            }
    }

    fun getColor1BG():Int{
        val hsv = FloatArray(3)
        Color.colorToHSV(getColor1(),hsv)
        if(hsv[2]>0.7f)
            hsv[2] = 0.95f
        else if (hsv[2]>0.3f) hsv[2] = hsv[2]-0.2f
        return Color.HSVToColor(hsv);
    }

    fun getColor1():Int{
        return vibrantSwatch?.rgb ?: R.color.blue_400
    }

    fun getColor2():Int{
        return vibrantSwatch2?.rgb ?: R.color.grey
    }

    fun getBodyTextColor():Int{
        return vibrantSwatch?.bodyTextColor ?: R.color.blue_400
    }

    fun getTitleTextColor():Int{
        return vibrantSwatch?.titleTextColor ?: R.color.blue_400
    }


}