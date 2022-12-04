package pt.isec.a2020116565_2020116988.mathgame.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import android.view.View
import android.widget.ImageView
import java.io.*
import kotlin.math.max
import kotlin.math.min

fun encodeTobase64(context: Context, uri: Uri): String {
    val input: InputStream? = context.contentResolver.openInputStream(uri)
    val image: Bitmap = BitmapFactory.decodeStream(input)
    input?.close()

    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val b: ByteArray = baos.toByteArray()

    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun updatePic(view: View, encodedImage: String){
    val targetW = view.width
    val targetH = view.height
    if (targetH < 1 || targetW < 1)
        return
    val bmpOptions = BitmapFactory.Options()
    bmpOptions.inJustDecodeBounds = true
    val b: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(b, 0, b.size, bmpOptions)
    val photoW = bmpOptions.outWidth
    val photoH = bmpOptions.outHeight
    val scale = max(1,min(photoW / targetW, photoH / targetH))
    bmpOptions.inSampleSize = scale
    bmpOptions.inJustDecodeBounds = false
    val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size, bmpOptions)
    when {
        view is ImageView -> (view as ImageView).setImageBitmap(bitmap)
        else -> view.background = BitmapDrawable(view.resources,bitmap)
    }
}
