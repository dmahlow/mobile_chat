package dev.chungjungsoo.gptmobile.data.model

object SharedContentHolder {
    var pendingText: String? = null
    var pendingImagePaths: List<String> = emptyList()

    fun consume(): Pair<String?, List<String>> {
        val text = pendingText
        val images = pendingImagePaths
        pendingText = null
        pendingImagePaths = emptyList()
        return text to images
    }

    fun hasPending(): Boolean = pendingText != null || pendingImagePaths.isNotEmpty()
}
