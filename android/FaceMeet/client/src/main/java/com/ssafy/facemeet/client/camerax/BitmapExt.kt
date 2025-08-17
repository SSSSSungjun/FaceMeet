import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun Bitmap.toMultipartBodyPart(partName: String): MultipartBody.Part {
    // 임시 파일 생성
    val file = File.createTempFile(partName, ".jpg")
    file.outputStream().use {
        this.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }

    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
}
